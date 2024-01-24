package org.sentrysoftware.metricshub.engine.strategy.detection;

import static org.sentrysoftware.metricshub.engine.common.helpers.MetricsHubConstants.CONNECTOR_STATUS_METRIC_KEY;
import static org.sentrysoftware.metricshub.engine.common.helpers.MetricsHubConstants.MONITOR_ATTRIBUTE_APPLIES_TO_OS;
import static org.sentrysoftware.metricshub.engine.common.helpers.MetricsHubConstants.MONITOR_ATTRIBUTE_ID;
import static org.sentrysoftware.metricshub.engine.common.helpers.MetricsHubConstants.MONITOR_ATTRIBUTE_NAME;
import static org.sentrysoftware.metricshub.engine.common.helpers.MetricsHubConstants.MONITOR_ATTRIBUTE_PARENT_ID;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;
import lombok.Builder;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;
import lombok.NonNull;
import lombok.extern.slf4j.Slf4j;
import org.sentrysoftware.metricshub.engine.client.ClientsExecutor;
import org.sentrysoftware.metricshub.engine.common.helpers.KnownMonitorType;
import org.sentrysoftware.metricshub.engine.common.helpers.MetricsHubConstants;
import org.sentrysoftware.metricshub.engine.common.helpers.NetworkHelper;
import org.sentrysoftware.metricshub.engine.configuration.HostConfiguration;
import org.sentrysoftware.metricshub.engine.connector.model.Connector;
import org.sentrysoftware.metricshub.engine.strategy.AbstractStrategy;
import org.sentrysoftware.metricshub.engine.telemetry.HostProperties;
import org.sentrysoftware.metricshub.engine.telemetry.MetricFactory;
import org.sentrysoftware.metricshub.engine.telemetry.Monitor;
import org.sentrysoftware.metricshub.engine.telemetry.MonitorFactory;
import org.sentrysoftware.metricshub.engine.telemetry.TelemetryManager;

/**
 * The {@code DetectionStrategy} class represents the strategy for detecting and creating monitors based on the configuration and connector detection results.
 * It is responsible for running connector detection, creating monitors for the detected connectors, and updating the status of configured connectors.
 *
 * <p>
 * The class extends {@link AbstractStrategy} and implements the detection and creation logic for monitors based on connector detection results.
 * It uses the TelemetryManager to manage monitors and metrics associated with connectors.
 * </p>
 */
@NoArgsConstructor
@EqualsAndHashCode(callSuper = true)
@Slf4j
public class DetectionStrategy extends AbstractStrategy {

	/**
	 * Builder for constructing instances of {@code DetectionStrategy}.
	 *
	 * @param telemetryManager The telemetry manager for managing monitors and metrics.
	 * @param strategyTime     The time at which the detection strategy is executed.
	 * @param clientsExecutor  The executor for running connector clients.
	 */
	@Builder
	public DetectionStrategy(
		@NonNull final TelemetryManager telemetryManager,
		@NonNull final Long strategyTime,
		@NonNull final ClientsExecutor clientsExecutor
	) {
		super(telemetryManager, strategyTime, clientsExecutor);
	}

	@Override
	public void run() {
		final HostConfiguration hostConfiguration = telemetryManager.getHostConfiguration();
		final HostProperties hostProperties = telemetryManager.getHostProperties();
		if (hostConfiguration == null) {
			return;
		}

		final String hostname = hostConfiguration.getHostname();
		log.debug("Hostname {} - Start detection strategy.", hostname);

		// Detect if we monitor localhost then set the localhost property in the HostProperties instance
		hostProperties.setLocalhost(NetworkHelper.isLocalhost(hostname));

		// Get the configured connector
		final String configuredConnectorId = hostConfiguration.getConfiguredConnectorId();

		final Set<String> selectedConnectors = hostConfiguration.getSelectedConnectors();
		List<ConnectorTestResult> connectorTestResults = new ArrayList<>();
		// If one or more connector are selected, we run them
		if (selectedConnectors != null && !selectedConnectors.isEmpty()) {
			connectorTestResults = new ConnectorSelection(telemetryManager, clientsExecutor).run();
		} else if (configuredConnectorId == null) { // Else we run the automatic detection if we haven't a configured connector
			connectorTestResults = new AutomaticDetection(telemetryManager, clientsExecutor).run();
		}

		// Create Host monitor
		final MonitorFactory monitorFactory = MonitorFactory
			.builder()
			.telemetryManager(telemetryManager)
			.discoveryTime(strategyTime)
			.build();
		monitorFactory.createEndpointHostMonitor(hostProperties.isLocalhost());

		// Create monitors
		createMonitors(connectorTestResults);

		// Create configured connector monitor
		createConfiguredConnectorMonitor(configuredConnectorId);
	}

	/**
	 * Create a new connector monitor for the configured connector
	 *
	 * @param configuredConnectorId unique identifier of the connector
	 */
	void createConfiguredConnectorMonitor(final String configuredConnectorId) {
		if (configuredConnectorId == null) {
			return;
		}

		final String hostId = telemetryManager.getHostConfiguration().getHostId();

		// Set monitor attributes
		final Map<String, String> monitorAttributes = new HashMap<>();
		monitorAttributes.put(MONITOR_ATTRIBUTE_ID, configuredConnectorId);
		monitorAttributes.put(MONITOR_ATTRIBUTE_NAME, configuredConnectorId);
		monitorAttributes.put(MONITOR_ATTRIBUTE_PARENT_ID, hostId);

		// Create the monitor factory
		final MonitorFactory monitorFactory = MonitorFactory
			.builder()
			.telemetryManager(telemetryManager)
			.monitorType(KnownMonitorType.CONNECTOR.getKey())
			.attributes(monitorAttributes)
			.connectorId(configuredConnectorId)
			.discoveryTime(strategyTime)
			.build();

		// Create or update the monitor by calling monitor factory
		final Monitor monitor = monitorFactory.createOrUpdateMonitor(
			String.format(CONNECTOR_ID_FORMAT, KnownMonitorType.CONNECTOR.getKey(), configuredConnectorId)
		);

		telemetryManager.getHostProperties().getConnectorNamespace(configuredConnectorId).setStatusOk(true);

		new MetricFactory(telemetryManager.getHostname())
			.collectNumberMetric(monitor, CONNECTOR_STATUS_METRIC_KEY, 1.0, strategyTime);
	}

	/**
	 * This method creates monitors in TelemetryManager given a list of ConnectorTestResult
	 *
	 * @param connectorTestResultList List of ConnectorTestResult
	 */
	void createMonitors(final List<ConnectorTestResult> connectorTestResultList) {
		connectorTestResultList.forEach(this::createMonitor);
	}

	/**
	 * This method creates a monitor in TelemetryManager for a given ConnectorTestResult instance
	 *
	 * @param connectorTestResult ConnectorTestResult instance
	 */
	public void createMonitor(final ConnectorTestResult connectorTestResult) {
		// Get the connector
		final Connector connector = connectorTestResult.getConnector();

		// Set monitor attributes
		final Map<String, String> monitorAttributes = new HashMap<>();
		final String hostId = telemetryManager.getHostConfiguration().getHostId();
		final String connectorId = connector.getCompiledFilename();
		monitorAttributes.put(MONITOR_ATTRIBUTE_ID, connectorId);
		monitorAttributes.put(MONITOR_ATTRIBUTE_NAME, connectorId);
		monitorAttributes.put(
			MONITOR_ATTRIBUTE_APPLIES_TO_OS,
			connector
				.getConnectorIdentity()
				.getDetection()
				.getAppliesTo()
				.stream()
				.map(deviceKind -> deviceKind.toString().toLowerCase())
				.sorted()
				.collect(Collectors.joining(MetricsHubConstants.COMMA))
		);
		monitorAttributes.put("description", connector.getConnectorIdentity().getInformation());
		monitorAttributes.put(MONITOR_ATTRIBUTE_PARENT_ID, hostId);

		// Create the monitor factory
		final MonitorFactory monitorFactory = MonitorFactory
			.builder()
			.telemetryManager(telemetryManager)
			.monitorType(KnownMonitorType.CONNECTOR.getKey())
			.attributes(monitorAttributes)
			.connectorId(connectorId)
			.discoveryTime(strategyTime)
			.build();

		// Create or update the monitor by calling monitor factory
		final Monitor monitor = monitorFactory.createOrUpdateMonitor(
			String.format(CONNECTOR_ID_FORMAT, KnownMonitorType.CONNECTOR.getKey(), connectorId)
		);

		collectConnectorStatus(connectorTestResult, connector, connectorId, monitor);
	}
}
