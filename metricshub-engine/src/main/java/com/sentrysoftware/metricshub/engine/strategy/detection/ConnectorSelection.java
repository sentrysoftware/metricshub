package com.sentrysoftware.metricshub.engine.strategy.detection;

import com.sentrysoftware.metricshub.engine.configuration.HostConfiguration;
import com.sentrysoftware.metricshub.engine.connector.model.Connector;
import com.sentrysoftware.metricshub.engine.connector.model.ConnectorStore;
import com.sentrysoftware.metricshub.engine.matsya.MatsyaClientsExecutor;
import com.sentrysoftware.metricshub.engine.telemetry.TelemetryManager;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.TreeSet;
import java.util.stream.Collectors;
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

		final Set<String> caseInsensitiveSelectedConnectors = new TreeSet<>(String.CASE_INSENSITIVE_ORDER);
		final Set<String> caseSensitiveSelectedConnectors = hostConfiguration.getSelectedConnectors();

		if (caseSensitiveSelectedConnectors != null) {
			caseInsensitiveSelectedConnectors.addAll(caseSensitiveSelectedConnectors);
		}

		if (caseInsensitiveSelectedConnectors == null || caseInsensitiveSelectedConnectors.isEmpty()) {
			log.error(
				"Hostname {} - No connectors have been selected for the detection. Stopping discovery operation.",
				hostname
			);
			return Collections.emptyList();
		}

		return runAllConnectorsDetectionCriteria(
			connectorStore
				.values()
				.stream()
				.filter(connector -> isConnectorContainedInSet(connector, caseInsensitiveSelectedConnectors)),
			hostConfiguration
		)
			.collect(Collectors.toList()); //NOSONAR
	}
}
