package com.sentrysoftware.matrix.strategy.detection;

import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Set;

import com.sentrysoftware.matrix.configuration.HostConfiguration;
import com.sentrysoftware.matrix.connector.model.Connector;
import com.sentrysoftware.matrix.connector.model.ConnectorStore;
import com.sentrysoftware.matrix.matsya.MatsyaClientsExecutor;
import com.sentrysoftware.matrix.telemetry.TelemetryManager;

import lombok.NoArgsConstructor;
import lombok.NonNull;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@NoArgsConstructor
public class ConnectorSelection extends AbstractConnectorProcessor {

	public ConnectorSelection(
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
		log.debug("Hostname {} - Start connector selection.", hostname);

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

		final Set<String> selectedConnectors = hostConfiguration.getSelectedConnectors();
		if (selectedConnectors == null || selectedConnectors.isEmpty()) {
			log.error("Hostname {} - No connectors have been selected for the detection. Stopping discovery operation.", hostname);
			return Collections.emptyList();
		}

		return runAllConnectorsDetectionCriteria(connectorStore
			.values()
			.stream()
			.filter(connector ->
				isConnectorContainedInSet(connector, selectedConnectors)
			),
			hostConfiguration
		)
		.toList();
	}
}
