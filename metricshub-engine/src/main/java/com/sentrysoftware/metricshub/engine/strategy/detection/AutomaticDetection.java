package com.sentrysoftware.metricshub.engine.strategy.detection;

import static com.sentrysoftware.metricshub.engine.strategy.utils.DetectionHelper.hasAtLeastOneTagOf;

import com.sentrysoftware.metricshub.engine.configuration.HostConfiguration;
import com.sentrysoftware.metricshub.engine.connector.model.Connector;
import com.sentrysoftware.metricshub.engine.connector.model.ConnectorStore;
import com.sentrysoftware.metricshub.engine.connector.model.common.DeviceKind;
import com.sentrysoftware.metricshub.engine.connector.model.identity.ConnectionType;
import com.sentrysoftware.metricshub.engine.connector.model.monitor.task.source.Source;
import com.sentrysoftware.metricshub.engine.matsya.MatsyaClientsExecutor;
import com.sentrysoftware.metricshub.engine.telemetry.TelemetryManager;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;
import lombok.NoArgsConstructor;
import lombok.NonNull;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@NoArgsConstructor
public class AutomaticDetection extends AbstractConnectorProcessor {

	public AutomaticDetection(
		@NonNull final TelemetryManager telemetryManager,
		@NonNull final MatsyaClientsExecutor matsyaClientsExecutor
	) {
		super(telemetryManager, matsyaClientsExecutor);
	}

	@Override
	public List<ConnectorTestResult> run() {
		final HostConfiguration hostConfiguration = telemetryManager.getHostConfiguration();
		if (hostConfiguration == null) {
			log.error("Empty host configuration, aborting detection job.");
			return Collections.emptyList();
		}

		final String hostname = hostConfiguration.getHostname();
		log.debug("Hostname {} - Start automatic detection.", hostname);

		final ConnectorStore telemetryManagerConnectorStore = telemetryManager.getConnectorStore();
		if (telemetryManagerConnectorStore == null) {
			log.error("Hostname {} - No connectorStore found. Stopping detection operation.", hostname);
			return Collections.emptyList();
		}

		final Map<String, Connector> connectorStore = telemetryManagerConnectorStore.getStore();
		if (connectorStore == null) {
			log.error("Hostname {} - No connectorStore found. Stopping detection operation.", hostname);
			return Collections.emptyList();
		}

		final DeviceKind deviceKind = hostConfiguration.getHostType();
		final Set<String> excludedConnectors = hostConfiguration.getExcludedConnectors();
		final boolean isLocalhost = telemetryManager.getHostProperties().isLocalhost();
		final Set<Class<? extends Source>> acceptedSources = hostConfiguration.determineAcceptedSources(isLocalhost);

		// Filter the excluded connectors from the list of connectors
		if (excludedConnectors != null && !excludedConnectors.isEmpty()) {
			excludedConnectors.stream().forEach(connectorStore::remove);
		}

		if (connectorStore.isEmpty()) {
			log.error("Hostname {} - No connector to detect. Stopping detection operation.", hostname);
			return new ArrayList<>();
		}

		final Set<String> includeConnectorTags = telemetryManager.getHostConfiguration().getIncludeConnectorTags();

		final List<Connector> connectors = connectorStore
			.values()
			.stream()
			.filter(connector -> connector.getOrCreateConnectorIdentity().getDetection() != null)
			.filter(connector -> hasAtLeastOneTagOf(includeConnectorTags, connector))
			// No Auto Detection Filtering
			.filter(connector -> !connector.getOrCreateConnectorIdentity().getDetection().isDisableAutoDetection())
			// DeviceKind Filtering
			.filter(connector -> connector.getOrCreateConnectorIdentity().getDetection().getAppliesTo().contains(deviceKind))
			// ConnectionType Filtering
			.filter(connector -> connectionTypesFiltering(connector, isLocalhost))
			// Accepted Sources Filtering
			.filter(connector -> anyMatch(connector.getSourceTypes(), acceptedSources))
			.collect(Collectors.toList()); //NOSONAR

		final Set<String> supersedes = new HashSet<>();
		List<ConnectorTestResult> connectorTestResults = runAllConnectorsDetectionCriteria(
			connectors.stream(),
			hostConfiguration
		)
			// Keep Only Success Connectors
			.filter(ConnectorTestResult::isSuccess)
			.collect(Collectors.toList()); //NOSONAR

		// Find all Superseded connectors
		connectorTestResults.forEach(connectorTestResult -> updateSupersedes(supersedes, connectorTestResult));

		// Filter Superseded connectors
		connectorTestResults =
			connectorTestResults
				.stream()
				.filter(connectorTestResult ->
					!supersedes.contains(
						connectorTestResult.getConnector().getConnectorIdentity().getCompiledFilename().toLowerCase()
					)
				)
				.collect(Collectors.toList()); //NOSONAR

		// Filter onLastResort Connectors
		filterLastResortConnectors(connectorTestResults, hostname);

		return connectorTestResults;
	}

	/**
	 * Return true if the connector has the same type of connection as the host (local or remote)
	 * @param connector   The connector to test
	 * @param isLocalHost True if
	 * @return boolean value
	 */
	private boolean connectionTypesFiltering(final Connector connector, final boolean isLocalHost) {
		return connector
			.getConnectorIdentity()
			.getDetection()
			.getConnectionTypes()
			.contains(isLocalHost ? ConnectionType.LOCAL : ConnectionType.REMOTE);
	}

	/**
	 * Return true if any element of the acceptedSources match at least a value in sourceTypes
	 * @param sourceTypes
	 * @param acceptedSources
	 * @return boolean value
	 */
	private boolean anyMatch(
		final Set<Class<? extends Source>> sourceTypes,
		final Set<Class<? extends Source>> acceptedSources
	) {
		for (Class<? extends Source> source : acceptedSources) {
			if (sourceTypes.contains(source)) {
				return true;
			}
		}
		return false;
	}
}
