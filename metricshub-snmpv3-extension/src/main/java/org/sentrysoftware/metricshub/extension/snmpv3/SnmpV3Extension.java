package org.sentrysoftware.metricshub.extension.snmpv3;

/*-
 * ╱╲╱╲╱╲╱╲╱╲╱╲╱╲╱╲╱╲╱╲╱╲╱╲╱╲╱╲╱╲╱╲╱╲╱╲╱╲╱╲
 * MetricsHub SNMP V3 Extension
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
import java.util.function.Function;
import java.util.function.UnaryOperator;
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
import org.sentrysoftware.metricshub.extension.snmp.ISnmpConfiguration;
import org.sentrysoftware.metricshub.extension.snmp.detection.SnmpGetCriterionProcessor;
import org.sentrysoftware.metricshub.extension.snmp.detection.SnmpGetNextCriterionProcessor;
import org.sentrysoftware.metricshub.extension.snmp.source.SnmpGetSourceProcessor;
import org.sentrysoftware.metricshub.extension.snmp.source.SnmpTableSourceProcessor;

/**
 * This class implements the {@link IProtocolExtension} contract, reports the supported features,
 * processes SNMP sources and criteria.
 */
@Slf4j
public class SnmpV3Extension implements IProtocolExtension {

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
	public static final String SNMPV3_UP_METRIC = "metricshub.host.up{protocol=\"snmpv3\"}";

	/**
	 * The SNMP OID value to use in the health check test
	 */
	public static final String SNMPV3_OID = "1.3.6.1";

	private SnmpV3RequestExecutor snmpV3RequestExecutor;

	/**
	 * Creates a new instance of the {@link SnmpV3Extension} implementation.
	 */
	public SnmpV3Extension() {
		snmpV3RequestExecutor = new SnmpV3RequestExecutor();
	}

	@Override
	public boolean isValidConfiguration(IConfiguration configuration) {
		return configuration instanceof SnmpV3Configuration;
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

		// Create and set the SNMP V3 result to null
		String snmpResult = null;

		// Retrieve SNMP V3 Configuration from the telemetry manager host configuration
		final SnmpV3Configuration snmpV3Configuration = (SnmpV3Configuration) telemetryManager
			.getHostConfiguration()
			.getConfigurations()
			.get(SnmpV3Configuration.class);

		// Stop the SNMP V3 health check if there is not an SNMP V3 configuration
		if (snmpV3Configuration == null) {
			return;
		}

		log.info("Hostname {} - Checking SNMP protocol status. Sending Get Next request on {}.", hostname, SNMPV3_OID);

		// Execute SNMP test command
		try {
			snmpResult = snmpV3RequestExecutor.executeSNMPGetNext(SNMPV3_OID, snmpV3Configuration, hostname, true);
		} catch (Exception e) {
			log.debug(
				"Hostname {} - Checking SNMP protocol status. SNMP exception when performing a SNMP Get Next query on {}: ",
				hostname,
				SNMPV3_OID,
				e
			);
		}

		// Generate a metric from the SNMP result
		// CHECKSTYLE:OFF
		new MetricFactory()
			.collectNumberMetric(
				hostMonitor,
				SNMPV3_UP_METRIC,
				snmpResult != null ? UP : DOWN,
				telemetryManager.getStrategyTime()
			);
		// CHECKSTYLE:ON
	}

	@Override
	public SourceTable processSource(Source source, String connectorId, TelemetryManager telemetryManager) {
		final Function<TelemetryManager, ISnmpConfiguration> configurationRetriever = manager ->
			(ISnmpConfiguration) manager.getHostConfiguration().getConfigurations().get(SnmpV3Configuration.class);

		if (source instanceof SnmpTableSource snmpTableSource) {
			return new SnmpTableSourceProcessor(snmpV3RequestExecutor, configurationRetriever)
				.process(snmpTableSource, connectorId, telemetryManager);
		} else if (source instanceof SnmpGetSource snmpGetSource) {
			return new SnmpGetSourceProcessor(snmpV3RequestExecutor, configurationRetriever)
				.process(snmpGetSource, connectorId, telemetryManager);
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
		final Function<TelemetryManager, ISnmpConfiguration> configurationRetriever = manager ->
			(ISnmpConfiguration) manager.getHostConfiguration().getConfigurations().get(SnmpV3Configuration.class);

		if (criterion instanceof SnmpGetCriterion snmpGetCriterion) {
			return new SnmpGetCriterionProcessor(snmpV3RequestExecutor, configurationRetriever)
				.process(snmpGetCriterion, connectorId, telemetryManager);
		} else if (criterion instanceof SnmpGetNextCriterion snmpGetNextCriterion) {
			return new SnmpGetNextCriterionProcessor(snmpV3RequestExecutor, configurationRetriever)
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
		return Map.of(SnmpV3Configuration.class, Set.of(SnmpTableSource.class, SnmpGetSource.class));
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
			final SnmpV3Configuration snmpV3Configuration = newObjectMapper()
				.treeToValue(jsonNode, SnmpV3Configuration.class);

			if (decrypt != null) {
				// Decrypt the community
				final char[] communityDecypted = decrypt.apply(snmpV3Configuration.getCommunity());
				snmpV3Configuration.setCommunity(communityDecypted);
			}

			return snmpV3Configuration;
		} catch (Exception e) {
			final String errorMessage = String.format(
				"Error while reading SNMP V3 Configuration: %s. Error: %s",
				jsonNode,
				e.getMessage()
			);
			log.error(errorMessage);
			log.debug("Error while reading SNMP V3 Configuration: {}. Stack trace:", jsonNode, e);
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
