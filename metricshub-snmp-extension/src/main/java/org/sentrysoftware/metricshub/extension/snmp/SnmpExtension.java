package org.sentrysoftware.metricshub.extension.snmp;

/*-
 * ╱╲╱╲╱╲╱╲╱╲╱╲╱╲╱╲╱╲╱╲╱╲╱╲╱╲╱╲╱╲╱╲╱╲╱╲╱╲╱╲
 * MetricsHub SNMP Extension
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
import lombok.AllArgsConstructor;
import lombok.NonNull;
import lombok.extern.slf4j.Slf4j;
import org.sentrysoftware.metricshub.engine.common.exception.InvalidConfigurationException;
import org.sentrysoftware.metricshub.engine.configuration.IConfiguration;
import org.sentrysoftware.metricshub.engine.connector.model.identity.criterion.Criterion;
import org.sentrysoftware.metricshub.engine.connector.model.identity.criterion.SnmpGetCriterion;
import org.sentrysoftware.metricshub.engine.connector.model.identity.criterion.SnmpGetNextCriterion;
import org.sentrysoftware.metricshub.engine.connector.model.monitor.task.source.SnmpGetSource;
import org.sentrysoftware.metricshub.engine.connector.model.monitor.task.source.SnmpTableSource;
import org.sentrysoftware.metricshub.engine.connector.model.monitor.task.source.Source;
import org.sentrysoftware.metricshub.engine.extension.IProtocolExtension;
import org.sentrysoftware.metricshub.engine.strategy.detection.CriterionTestResult;
import org.sentrysoftware.metricshub.engine.strategy.source.SourceTable;
import org.sentrysoftware.metricshub.engine.telemetry.MetricFactory;
import org.sentrysoftware.metricshub.engine.telemetry.Monitor;
import org.sentrysoftware.metricshub.engine.telemetry.TelemetryManager;

/**
 * This class implements the {@link IProtocolExtension} contract, reports the supported features,
 * processes SNMP sources and criteria.
 */
@Slf4j
@AllArgsConstructor
public class SnmpExtension implements IProtocolExtension {

	private SnmpRequestExecutor snmpRequestExecutor;

	/**
	 * Creates a new instance of the {@link SnmpExtension} implementation.
	 */
	public SnmpExtension() {
		snmpRequestExecutor = new SnmpRequestExecutor();
	}

	/**
	 * Protocol up status value '1.0'
	 */
	public static final Double UP = 1.0;

	/**
	 * Protocol down status value '0.0'
	 */
	public static final Double DOWN = 0.0;

	/**
	 * Up metric name format that will be saved by the metric factory
	 */
	static final String SNMP_UP_METRIC = "metricshub.host.up{protocol=\"snmp\"}";

	/**
	 * The SNMP OID value to use in the health check test
	 */
	public static final String SNMP_OID = "1.3.6.1";

	@Override
	public boolean isValidConfiguration(IConfiguration configuration) {
		return configuration instanceof SnmpConfiguration;
	}

	@Override
	public Set<Class<? extends Source>> getSupportedSources() {
		return Set.of(SnmpTableSource.class, SnmpGetSource.class);
	}

	@Override
	public Set<Class<? extends Criterion>> getSupportedCriteria() {
		return Set.of(SnmpGetCriterion.class, SnmpGetNextCriterion.class);
	}

	@Override
	public void checkProtocol(TelemetryManager telemetryManager) {
		// Retrieve the hostname
		final String hostname = telemetryManager.getHostConfiguration().getHostname();

		// Retrieve the host endpoint monitor
		final Monitor hostMonitor = telemetryManager.getEndpointHostMonitor();

		// Create and set the SNMP result to null
		String snmpResult = null;

		// Retrieve SNMP Configuration from the telemetry manager host configuration
		final SnmpConfiguration snmpConfiguration = (SnmpConfiguration) telemetryManager
			.getHostConfiguration()
			.getConfigurations()
			.get(SnmpConfiguration.class);

		// Stop the SNMP health check if there is not an SNMP configuration
		if (snmpConfiguration == null) {
			return;
		}

		log.info("Hostname {} - Checking SNMP protocol status. Sending Get Next request on {}.", hostname, SNMP_OID);

		// Execute SNMP test command
		try {
			snmpResult = snmpRequestExecutor.executeSNMPGetNext(SNMP_OID, snmpConfiguration, hostname, true);
		} catch (Exception e) {
			log.debug(
				"Hostname {} - Checking SNMP protocol status. SNMP exception when performing a SNMP Get Next query on {}: ",
				hostname,
				SNMP_OID,
				e
			);
		}

		// Generate a metric from the SNMP result
		// CHECKSTYLE:OFF
		new MetricFactory()
			.collectNumberMetric(
				hostMonitor,
				SNMP_UP_METRIC,
				snmpResult != null ? UP : DOWN,
				telemetryManager.getStrategyTime()
			);
		// CHECKSTYLE:ON
	}

	@Override
	public SourceTable processSource(Source source, String connectorId, TelemetryManager telemetryManager) {
		if (source instanceof SnmpTableSource snmpTableSource) {
			return new SnmpTableSourceProcessor(snmpRequestExecutor).process(snmpTableSource, connectorId, telemetryManager);
		} else if (source instanceof SnmpGetSource snmpGetSource) {
			return new SnmpGetSourceProcessor(snmpRequestExecutor).process(snmpGetSource, connectorId, telemetryManager);
		}
		throw new IllegalArgumentException(
			String.format(
				"Hostname %s - Cannot process source %s.",
				telemetryManager.getHostname(),
				source != null ? source.getClass().getSimpleName() : "<null>"
			)
		);
	}

	@Override
	public CriterionTestResult processCriterion(
		Criterion criterion,
		String connectorId,
		TelemetryManager telemetryManager
	) {
		if (criterion instanceof SnmpGetCriterion snmpGetCriterion) {
			return new SnmpGetCriterionProcessor(snmpRequestExecutor)
				.process(snmpGetCriterion, connectorId, telemetryManager);
		} else if (criterion instanceof SnmpGetNextCriterion snmpGetNextCriterion) {
			return new SnmpGetNextCriterionProcessor(snmpRequestExecutor)
				.process(snmpGetNextCriterion, connectorId, telemetryManager);
		}
		throw new IllegalArgumentException(
			String.format(
				"Hostname %s - Cannot process criterion %s.",
				telemetryManager.getHostname(),
				criterion != null ? criterion.getClass().getSimpleName() : "<null>"
			)
		);
	}

	@Override
	public Map<Class<? extends IConfiguration>, Set<Class<? extends Source>>> getConfigurationToSourceMapping() {
		return Map.of(SnmpConfiguration.class, Set.of(SnmpTableSource.class, SnmpGetSource.class));
	}

	@Override
	public boolean isSupportedConfigurationType(String configurationType) {
		return "snmp".equalsIgnoreCase(configurationType);
	}

	@Override
	public IConfiguration buildConfiguration(
		@NonNull String configurationType,
		@NonNull JsonNode jsonNode,
		UnaryOperator<char[]> decrypt
	) throws InvalidConfigurationException {
		try {
			final SnmpConfiguration snmpConfiguration = newObjectMapper().treeToValue(jsonNode, SnmpConfiguration.class);

			if (decrypt != null) {
				char[] community = snmpConfiguration.getCommunity();
				if (community != null) {
					// Decrypt the community
					snmpConfiguration.setCommunity(decrypt.apply(community));
				}
			}

			return snmpConfiguration;
		} catch (Exception e) {
			final String errorMessage = String.format(
				"Error while reading SNMP Configuration: %s. Error: %s",
				jsonNode,
				e.getMessage()
			);
			log.error(errorMessage);
			log.debug("Error while reading SNMP Configuration: {}. Stack trace:", jsonNode, e);
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
