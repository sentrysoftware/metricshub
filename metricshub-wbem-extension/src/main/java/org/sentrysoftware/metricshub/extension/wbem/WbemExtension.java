package org.sentrysoftware.metricshub.extension.wbem;

/*-
 * ╱╲╱╲╱╲╱╲╱╲╱╲╱╲╱╲╱╲╱╲╱╲╱╲╱╲╱╲╱╲╱╲╱╲╱╲╱╲╱╲
 * MetricsHub Wbem Extension
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

import static org.sentrysoftware.metricshub.extension.wbem.WbemRequestExecutor.isAcceptableException;

import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.MapperFeature;
import com.fasterxml.jackson.databind.SerializationFeature;
import com.fasterxml.jackson.databind.json.JsonMapper;
import com.fasterxml.jackson.dataformat.yaml.YAMLFactory;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.function.UnaryOperator;
import lombok.Data;
import lombok.extern.slf4j.Slf4j;
import org.sentrysoftware.metricshub.engine.common.exception.InvalidConfigurationException;
import org.sentrysoftware.metricshub.engine.common.helpers.LoggingHelper;
import org.sentrysoftware.metricshub.engine.configuration.IConfiguration;
import org.sentrysoftware.metricshub.engine.connector.model.identity.criterion.Criterion;
import org.sentrysoftware.metricshub.engine.connector.model.identity.criterion.WbemCriterion;
import org.sentrysoftware.metricshub.engine.connector.model.identity.criterion.WqlCriterion;
import org.sentrysoftware.metricshub.engine.connector.model.monitor.task.source.Source;
import org.sentrysoftware.metricshub.engine.connector.model.monitor.task.source.WbemSource;
import org.sentrysoftware.metricshub.engine.extension.IProtocolExtension;
import org.sentrysoftware.metricshub.engine.strategy.detection.CriterionTestResult;
import org.sentrysoftware.metricshub.engine.strategy.source.SourceTable;
import org.sentrysoftware.metricshub.engine.telemetry.MetricFactory;
import org.sentrysoftware.metricshub.engine.telemetry.Monitor;
import org.sentrysoftware.metricshub.engine.telemetry.TelemetryManager;

@Slf4j
public class WbemExtension implements IProtocolExtension {

	private static final String AUTOMATIC_NAMESPACE = "automatic";
	private static final String INTEROP_LOWER_CASE = "interop";

	/**
	 * Protocol up status value '1.0'
	 */
	public static final Double UP = 1.0;

	/**
	 * Protocol down status value '0.0'
	 */
	public static final Double DOWN = 0.0;

	/**
	 * WBEM Up metric
	 */
	public static final String WBEM_UP_METRIC = "metricshub.host.up{protocol=\"wbem\"}";

	/**
	 * List of WBEM protocol health check test Namespaces
	 */
	public static final List<String> WBEM_UP_TEST_NAMESPACES = List.of(
		"root/Interop",
		INTEROP_LOWER_CASE,
		"root/PG_Interop",
		"PG_Interop"
	);

	/**
	 * WQL Query to test WBEM protocol health check
	 */
	public static final String WBEM_TEST_QUERY = "SELECT Name FROM CIM_NameSpace";

	private WbemRequestExecutor wbemRequestExecutor;

	/**
	 * Creates a new instance of the {@link WbemExtension} implementation.
	 */
	public WbemExtension() {
		wbemRequestExecutor = new WbemRequestExecutor();
	}

	@Override
	public boolean isValidConfiguration(IConfiguration configuration) {
		return configuration instanceof WbemConfiguration;
	}

	@Override
	public Set<Class<? extends Source>> getSupportedSources() {
		return Set.of(WbemSource.class);
	}

	@Override
	public Map<Class<? extends IConfiguration>, Set<Class<? extends Source>>> getConfigurationToSourceMapping() {
		return Map.of(WbemConfiguration.class, Set.of(WbemSource.class));
	}

	@Override
	public Set<Class<? extends Criterion>> getSupportedCriteria() {
		return Set.of(WbemCriterion.class);
	}

	@Override
	public void checkProtocol(TelemetryManager telemetryManager) {
		// Retrieve the hostname
		final String hostname = telemetryManager.getHostConfiguration().getHostname();

		// Retrieve the host endpoint monitor
		final Monitor hostMonitor = telemetryManager.getEndpointHostMonitor();

		// Create and set the WBEM result to null
		String wbemResult = null;

		// Retrieve WBEM configuration from the telemetry manager
		final WbemConfiguration wbemConfiguration = (WbemConfiguration) telemetryManager
			.getHostConfiguration()
			.getConfigurations()
			.get(WbemConfiguration.class);

		// Stop the WBEM health check if there is not an WBEM configuration
		if (wbemConfiguration == null) {
			return;
		}

		log.info(
			"Hostname {} - Checking WBEM protocol status. Sending a WQL SELECT request on different namespaces.",
			hostname
		);

		final Long strategyTime = telemetryManager.getStrategyTime();
		final MetricFactory metricFactory = new MetricFactory();

		for (final String wbemNamespace : WBEM_UP_TEST_NAMESPACES) {
			try {
				log.info(
					"Hostname {} - Checking WBEM protocol status. Sending a WQL SELECT request on {} namespace.",
					hostname,
					wbemNamespace
				);

				// The query on the WBEM namespace returned a result
				//CHECKSTYLE:OFF
				if (
					wbemRequestExecutor.executeWbem(
						hostname,
						wbemConfiguration,
						WBEM_TEST_QUERY,
						wbemNamespace,
						telemetryManager
					) !=
					null
					//CHECKSTYLE:OFF
				) {
					// Collect the metric with a '1.0' value and stop the test
					metricFactory.collectNumberMetric(hostMonitor, WBEM_UP_METRIC, UP, strategyTime);
					return;
				}
			} catch (Exception e) {
				if (isAcceptableException(e)) {
					// Collect the WBEM metric with a '1.0' value and stop the test as the thrown exception is acceptable
					metricFactory.collectNumberMetric(hostMonitor, WBEM_UP_METRIC, UP, strategyTime);
					return;
				}
				log.debug(
					"Hostname {} - Checking WBEM protocol status. WBEM exception when performing a WQL SELECT query on '{}' namespace: ",
					hostname,
					wbemNamespace,
					e
				);
			}
		}

		// Generate a metric from the WBEM result
		// CHECKSTYLE:OFF
		metricFactory.collectNumberMetric(hostMonitor, WBEM_UP_METRIC, wbemResult != null ? UP : DOWN, strategyTime);
		// CHECKSTYLE:ON
	}

	@Override
	public CriterionTestResult processCriterion(
		Criterion criterion,
		String connectorId,
		TelemetryManager telemetryManager
	) {
		if (criterion instanceof WbemCriterion wbemCriterion) {
			// Sanity check
			if (criterion == null) {
				return CriterionTestResult.error(wbemCriterion, "Malformed criterion. Cannot perform detection.");
			}

			// Gather the necessary info on the test that needs to be performed
			final String hostname = telemetryManager.getHostConfiguration().getHostname();

			final WbemConfiguration wbemConfiguration = (WbemConfiguration) telemetryManager
				.getHostConfiguration()
				.getConfigurations()
				.get(WbemConfiguration.class);
			if (wbemConfiguration == null) {
				return CriterionTestResult.error(wbemCriterion, "The WBEM credentials are not configured for this host.");
			}

			WbemCriterionProcessor wbemCriterionProcessor = new WbemCriterionProcessor(wbemRequestExecutor);

			// If namespace is specified as "Automatic"
			if (AUTOMATIC_NAMESPACE.equalsIgnoreCase(wbemCriterion.getNamespace())) {
				final String cachedNamespace = telemetryManager
					.getHostProperties()
					.getConnectorNamespace(connectorId)
					.getAutomaticWbemNamespace();

				// If not detected already, find the namespace
				if (cachedNamespace == null) {
					return wbemCriterionProcessor.findNamespace(
						hostname,
						wbemConfiguration,
						wbemCriterion,
						telemetryManager,
						connectorId
					);
				}

				// Update the criterion with the cached namespace
				WqlCriterion cachedNamespaceCriterion = wbemCriterion.copy();
				cachedNamespaceCriterion.setNamespace(cachedNamespace);

				// Run the test
				return wbemCriterionProcessor.performDetectionTest(
					hostname,
					wbemConfiguration,
					cachedNamespaceCriterion,
					telemetryManager
				);
			}

			// Run the test
			return wbemCriterionProcessor.performDetectionTest(hostname, wbemConfiguration, wbemCriterion, telemetryManager);
		}

		throw new IllegalArgumentException(
			String.format(
				"Hostname %s - Cannot process criterion %s.",
				telemetryManager.getHostname(),
				criterion != null ? criterion.getClass().getSimpleName() : "<null>"
			)
		);
	}

	public SourceTable processSource(Source source, String connectorId, TelemetryManager telemetryManager) {
		final String hostname = telemetryManager.getHostConfiguration().getHostname();

		WbemSource wbemSource = (WbemSource) source;

		if (wbemSource == null || wbemSource.getQuery() == null) {
			log.error("Hostname {} - Malformed WBEM Source {}. Returning an empty table.", hostname, wbemSource);
			return SourceTable.empty();
		}

		final WbemConfiguration wbemConfiguration = (WbemConfiguration) telemetryManager
			.getHostConfiguration()
			.getConfigurations()
			.get(WbemConfiguration.class);

		if (wbemConfiguration == null) {
			log.debug(
				"Hostname {} - The WBEM credentials are not configured. Returning an empty table for WBEM source {}.",
				hostname,
				wbemSource.getKey()
			);
			return SourceTable.empty();
		}

		WbemSourceProcessor wbemSourceProcessor = new WbemSourceProcessor();
		// Get the namespace, the default one is : root/cimv2
		final String namespace = wbemSourceProcessor.getNamespace(wbemSource, telemetryManager, connectorId);

		try {
			if (hostname == null) {
				log.error("Hostname {} - No hostname indicated, the URL cannot be built.", hostname);
				return SourceTable.empty();
			}
			if (wbemConfiguration.getPort() == null || wbemConfiguration.getPort() == 0) {
				log.error("Hostname {} - No port indicated to connect to the host", hostname);
				return SourceTable.empty();
			}

			final List<List<String>> table = wbemRequestExecutor.executeWbem(
				hostname,
				wbemConfiguration,
				wbemSource.getQuery(),
				namespace,
				telemetryManager
			);

			return SourceTable.builder().table(table).build();
		} catch (Exception e) {
			LoggingHelper.logSourceError(
				connectorId,
				wbemSource.getKey(),
				String.format(
					"WBEM query=%s, Username=%s, Timeout=%d, Namespace=%s",
					wbemSource.getQuery(),
					wbemConfiguration.getUsername(),
					wbemConfiguration.getTimeout(),
					namespace
				),
				hostname,
				e
			);

			return SourceTable.empty();
		}
	}

	@Override
	public boolean isSupportedConfigurationType(String configurationType) {
		return "wbem".equalsIgnoreCase(configurationType);
	}

	@Override
	public IConfiguration buildConfiguration(String configurationType, JsonNode jsonNode, UnaryOperator<char[]> decrypt)
		throws InvalidConfigurationException {
		try {
			final WbemConfiguration wbemConfiguration = newObjectMapper().treeToValue(jsonNode, WbemConfiguration.class);

			if (decrypt != null) {
				// Decrypt the password
				final char[] passwordDecrypted = decrypt.apply(wbemConfiguration.getPassword());
				wbemConfiguration.setPassword(passwordDecrypted);
			}

			return wbemConfiguration;
		} catch (Exception e) {
			final String errorMessage = String.format(
				"Error while reading WBEM Configuration: %s. Error: %s",
				jsonNode,
				e.getMessage()
			);
			log.error(errorMessage);
			log.debug("Error while reading WBEM Configuration: {}. Stack trace:", jsonNode, e);
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

	/**
	 * Represents a WQL Query (i.e. a query in a namespace)
	 */
	@Data
	static class WqlQuery {

		private String wql;
		private String namespace;

		WqlQuery(final String wql, final String namespace) {
			this.wql = wql;
			this.namespace = namespace;
		}
	}
}
