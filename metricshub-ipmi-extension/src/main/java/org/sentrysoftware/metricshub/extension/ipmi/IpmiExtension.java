package org.sentrysoftware.metricshub.extension.ipmi;

/*-
 * ╱╲╱╲╱╲╱╲╱╲╱╲╱╲╱╲╱╲╱╲╱╲╱╲╱╲╱╲╱╲╱╲╱╲╱╲╱╲╱╲
 * MetricsHub Ipmi Extension
 * ჻჻჻჻჻჻
 * Copyright 2023 - 2024 Sentry Software
 * ჻჻჻჻჻჻
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Affero General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU Affero General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 * ╲╱╲╱╲╱╲╱╲╱╲╱╲╱╲╱╲╱╲╱╲╱╲╱╲╱╲╱╲╱╲╱╲╱╲╱╲╱╲╱
 */

import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.MapperFeature;
import com.fasterxml.jackson.databind.SerializationFeature;
import com.fasterxml.jackson.databind.json.JsonMapper;
import com.fasterxml.jackson.dataformat.yaml.YAMLFactory;
import java.util.Map;
import java.util.Set;
import java.util.function.UnaryOperator;
import lombok.extern.slf4j.Slf4j;
import org.sentrysoftware.ipmi.client.IpmiClient;
import org.sentrysoftware.ipmi.client.IpmiClientConfiguration;
import org.sentrysoftware.metricshub.engine.common.exception.InvalidConfigurationException;
import org.sentrysoftware.metricshub.engine.common.helpers.ArrayHelper;
import org.sentrysoftware.metricshub.engine.common.helpers.LoggingHelper;
import org.sentrysoftware.metricshub.engine.configuration.IConfiguration;
import org.sentrysoftware.metricshub.engine.connector.model.identity.criterion.Criterion;
import org.sentrysoftware.metricshub.engine.connector.model.identity.criterion.IpmiCriterion;
import org.sentrysoftware.metricshub.engine.connector.model.monitor.task.source.IpmiSource;
import org.sentrysoftware.metricshub.engine.connector.model.monitor.task.source.Source;
import org.sentrysoftware.metricshub.engine.extension.IProtocolExtension;
import org.sentrysoftware.metricshub.engine.strategy.detection.CriterionTestResult;
import org.sentrysoftware.metricshub.engine.strategy.source.SourceTable;
import org.sentrysoftware.metricshub.engine.telemetry.MetricFactory;
import org.sentrysoftware.metricshub.engine.telemetry.Monitor;
import org.sentrysoftware.metricshub.engine.telemetry.TelemetryManager;

@Slf4j
public class IpmiExtension implements IProtocolExtension {

	private IpmiRequestExecutor ipmiRequestExecutor = new IpmiRequestExecutor();

	/**
	 * Protocol up status value '1.0'
	 */
	public static final Double UP = 1.0;

	/**
	 * Protocol down status value '0.0'
	 */
	public static final Double DOWN = 0.0;

	/**
	 * IPMI Up metric
	 */
	public static final String IPMI_UP_METRIC = "metricshub.host.up{protocol=\"ipmi\"}";

	@Override
	public boolean isValidConfiguration(IConfiguration configuration) {
		return configuration instanceof IpmiConfiguration;
	}

	@Override
	public Set<Class<? extends Source>> getSupportedSources() {
		return Set.of(IpmiSource.class);
	}

	@Override
	public Map<Class<? extends IConfiguration>, Set<Class<? extends Source>>> getConfigurationToSourceMapping() {
		return Map.of(IpmiConfiguration.class, Set.of(IpmiSource.class));
	}

	@Override
	public Set<Class<? extends Criterion>> getSupportedCriteria() {
		return Set.of(IpmiCriterion.class);
	}

	public void checkProtocol(TelemetryManager telemetryManager) {
		// Retrieve the hostname
		String hostname = telemetryManager.getHostConfiguration().getHostname();

		// Retrieve the host endpoint monitor
		final Monitor hostMonitor = telemetryManager.getEndpointHostMonitor();

		// Create and set the IPMI result to null
		String ipmiResult = null;

		// Retrieve IPMI Configuration from the telemetry manager host configuration
		final IpmiConfiguration ipmiConfiguration = (IpmiConfiguration) telemetryManager
			.getHostConfiguration()
			.getConfigurations()
			.get(IpmiConfiguration.class);

		// Stop the IPMI health check if there is not an IPMI configuration
		if (ipmiConfiguration == null) {
			return;
		}

		log.info(
			"Hostname {} - Checking IPMI protocol status. Sending a IPMI 'Get Chassis Status As String Result' request.",
			hostname
		);

		// Execute IPMI test command
		try {
			ipmiResult =
				IpmiClient.getChassisStatusAsStringResult(
					new IpmiClientConfiguration(
						hostname,
						ipmiConfiguration.getUsername(),
						ipmiConfiguration.getPassword(),
						ArrayHelper.hexToByteArray(ipmiConfiguration.getBmcKey()),
						ipmiConfiguration.isSkipAuth(),
						ipmiConfiguration.getTimeout()
					)
				);
		} catch (Exception e) {
			log.debug(
				"Hostname {} - Checking IPMI protocol status. IPMI exception when performing a IPMI 'Get Chassis Status As String Result' query: ",
				hostname,
				e
			);
		}

		// Generate a metric from the IPMI result
		// CHECKSTYLE:OFF
		new MetricFactory()
			.collectNumberMetric(
				hostMonitor,
				IPMI_UP_METRIC,
				ipmiResult != null ? UP : DOWN,
				telemetryManager.getStrategyTime()
			);
		// CHECKSTYLE:ON
	}

	@Override
	public SourceTable processSource(Source source, String connectorId, TelemetryManager telemetryManager) {
		final IpmiConfiguration ipmiConfiguration = (IpmiConfiguration) telemetryManager
			.getHostConfiguration()
			.getConfigurations()
			.get(IpmiConfiguration.class);

		final String hostname = telemetryManager.getHostConfiguration().getHostname();

		if (ipmiConfiguration == null) {
			log.warn("Hostname {} - The IPMI credentials are not configured. Cannot process IPMI-over-LAN source.", hostname);
			return SourceTable.empty();
		}

		try {
			final String result = ipmiRequestExecutor.executeIpmiGetSensors(hostname, ipmiConfiguration);

			if (result != null) {
				return SourceTable.builder().rawData(result).build();
			} else {
				log.error("Hostname {} - IPMI-over-LAN request returned <null> result. Returning an empty table.", hostname);
			}
		} catch (Exception e) {
			LoggingHelper.logSourceError(connectorId, source.getKey(), "IPMI-over-LAN", hostname, e);
		}

		return SourceTable.empty();
	}

	@Override
	public CriterionTestResult processCriterion(
		Criterion criterion,
		String connectorId,
		TelemetryManager telemetryManager
	) {
		final IpmiConfiguration configuration = (IpmiConfiguration) telemetryManager
			.getHostConfiguration()
			.getConfigurations()
			.get(IpmiConfiguration.class);

		final String hostname = telemetryManager.getHostConfiguration().getHostname();

		if (configuration == null) {
			log.debug(
				"Hostname {} - The IPMI credentials are not configured for this host. Cannot process IPMI-over-LAN detection.",
				hostname
			);
			return CriterionTestResult.empty();
		}

		try {
			final String result = ipmiRequestExecutor.executeIpmiDetection(hostname, configuration);
			if (result == null) {
				return CriterionTestResult
					.builder()
					.message("Received <null> result after connecting to the IPMI BMC chip with the IPMI-over-LAN interface.")
					.build();
			}

			return CriterionTestResult
				.builder()
				.result(result)
				.message("Successfully connected to the IPMI BMC chip with the IPMI-over-LAN interface.")
				.success(true)
				.build();
		} catch (final Exception e) { // NOSONAR on interruption
			final String message = String.format(
				"Hostname %s - Cannot execute IPMI-over-LAN command to get the chassis status. Exception: %s",
				hostname,
				e.getMessage()
			);
			log.debug(message, e);
			return CriterionTestResult.builder().message(message).build();
		}
	}

	@Override
	public boolean isSupportedConfigurationType(String configurationType) {
		return "ipmi".equalsIgnoreCase(configurationType);
	}

	@Override
	public IConfiguration buildConfiguration(String configurationType, JsonNode jsonNode, UnaryOperator<char[]> decrypt)
		throws InvalidConfigurationException {
		try {
			final IpmiConfiguration ipmiConfiguration = newObjectMapper().treeToValue(jsonNode, IpmiConfiguration.class);

			if (decrypt != null) {
				final char[] password = ipmiConfiguration.getPassword();
				if (password != null) {
					// Decrypt the password
					ipmiConfiguration.setPassword(decrypt.apply(password));
				}
			}

			return ipmiConfiguration;
		} catch (Exception e) {
			final String errorMessage = String.format(
				"Error while reading IPMI Configuration: %s. Error: %s",
				jsonNode,
				e.getMessage()
			);
			log.error(errorMessage);
			log.debug("Error while reading IPMI Configuration: {}. Stack trace:", jsonNode, e);
			throw new InvalidConfigurationException(errorMessage, e);
		}
	}

	/**
	 * Creates and configures a new instance of the Jackson ObjectMapper for handling YAML data.
	 *
	 * @return A configured ObjectMapper instance.
	 */
	public static JsonMapper newObjectMapper() {
		return JsonMapper
			.builder(new YAMLFactory())
			.enable(MapperFeature.ACCEPT_CASE_INSENSITIVE_ENUMS)
			.enable(SerializationFeature.INDENT_OUTPUT)
			.configure(SerializationFeature.FAIL_ON_EMPTY_BEANS, false)
			.configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false)
			.configure(DeserializationFeature.FAIL_ON_INVALID_SUBTYPE, false)
			.build();
	}
}
