package com.sentrysoftware.matrix.strategy.detection;

import java.util.List;
import java.util.Map;
import java.util.Set;

import com.sentrysoftware.matrix.configuration.HostConfiguration;
import com.sentrysoftware.matrix.connector.model.Connector;
import com.sentrysoftware.matrix.connector.model.ConnectorStore;
import com.sentrysoftware.matrix.telemetry.TelemetryManager;

import lombok.extern.slf4j.Slf4j;

@Slf4j
public class ConnectorSelection extends AbstractConnectorProcessor {

	@Override
	public List<ConnectorTestResult> run(TelemetryManager telemetryManager) {
		final HostConfiguration hostConfiguration = telemetryManager.getHostConfiguration();
		if (hostConfiguration == null) {
			log.error("Empty host configuration, aborting detection job");
			return null;
		}

		final String hostname = hostConfiguration.getHostname();
		log.debug("Hostname {} - Start Discovery", hostname);

		final ConnectorStore telemetryManagerConnectorStore = telemetryManager.getConnectorStore();
		if (telemetryManagerConnectorStore == null) {
			log.error("Hostname {} - No connectorStore found. Stopping detection operation.", hostname);
			return null;
		}

		final Map<String, Connector> connectorStore = telemetryManagerConnectorStore.getStore();
		if (connectorStore == null) {
			log.error("Hostname {} - No connectorStore found. Stopping detection operation.", hostname);
			return null;
		}

		final Set<String> selectedConnectors = hostConfiguration.getSelectedConnectors();
		if (selectedConnectors == null || selectedConnectors.isEmpty()) {
			log.error("Hostname {} - No connectors have been selected for the detection. Stopping discovery operation.", hostname);
			return null;
		}

		return runAllConnectorsDetectionCriteria(
			connectorStore.values().stream().filter(connector -> isConnectorContainedInSet(connector, selectedConnectors)),
			hostConfiguration).toList();
	}
}
