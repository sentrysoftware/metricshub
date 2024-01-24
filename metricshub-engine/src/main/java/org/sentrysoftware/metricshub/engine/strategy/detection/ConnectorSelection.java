package org.sentrysoftware.metricshub.engine.strategy.detection;

import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.TreeSet;
import java.util.stream.Collectors;
import lombok.NoArgsConstructor;
import lombok.NonNull;
import lombok.extern.slf4j.Slf4j;
import org.sentrysoftware.metricshub.engine.client.ClientsExecutor;
import org.sentrysoftware.metricshub.engine.configuration.HostConfiguration;
import org.sentrysoftware.metricshub.engine.connector.model.Connector;
import org.sentrysoftware.metricshub.engine.connector.model.ConnectorStore;
import org.sentrysoftware.metricshub.engine.telemetry.TelemetryManager;

/**
 * The {@code ConnectorSelection} class represents a strategy for selecting specific connectors based on predefined criteria.
 * It extends the {@link AbstractConnectorProcessor} class and implements the connector selection logic.
 *
 * <p>
 * The connector selection process involves filtering connectors based on case-sensitive and case-insensitive lists of selected connectors.
 * The results of the selection are stored in {@link ConnectorTestResult} objects.
 * </p>
 *
 * <p>
 * The class checks the host configuration and connector store availability before proceeding with the selection.
 * It utilizes the configured lists of selected connectors, considering both case-sensitive and case-insensitive sets.
 * </p>
 */
@Slf4j
@NoArgsConstructor
public class ConnectorSelection extends AbstractConnectorProcessor {

	/**
	 * Constructs a new {@code ConnectorSelection} instance using the provided telemetry manager and clients executor.
	 *
	 * @param telemetryManager The telemetry manager responsible for managing telemetry-related operations.
	 * @param clientsExecutor  The executor for managing clients used in the strategy.
	 */
	public ConnectorSelection(
		@NonNull final TelemetryManager telemetryManager,
		@NonNull final ClientsExecutor clientsExecutor
	) {
		super(telemetryManager, clientsExecutor);
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
