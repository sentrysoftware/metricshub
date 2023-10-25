package com.sentrysoftware.metricshub.engine.strategy.detection;

import static com.sentrysoftware.metricshub.engine.common.helpers.MetricsHubConstants.CONNECTOR_STATUS_METRIC_KEY;
import static com.sentrysoftware.metricshub.engine.common.helpers.MetricsHubConstants.MONITOR_ATTRIBUTE_APPLIES_TO_OS;
import static com.sentrysoftware.metricshub.engine.common.helpers.MetricsHubConstants.MONITOR_ATTRIBUTE_ID;
import static com.sentrysoftware.metricshub.engine.common.helpers.MetricsHubConstants.MONITOR_ATTRIBUTE_NAME;
import static com.sentrysoftware.metricshub.engine.common.helpers.MetricsHubConstants.STATE_SET_METRIC_FAILED;
import static com.sentrysoftware.metricshub.engine.common.helpers.MetricsHubConstants.STATE_SET_METRIC_OK;

import com.sentrysoftware.metricshub.engine.common.helpers.KnownMonitorType;
import com.sentrysoftware.metricshub.engine.common.helpers.NetworkHelper;
import com.sentrysoftware.metricshub.engine.configuration.HostConfiguration;
import com.sentrysoftware.metricshub.engine.connector.model.Connector;
import com.sentrysoftware.metricshub.engine.connector.model.metric.MetricDefinition;
import com.sentrysoftware.metricshub.engine.connector.model.metric.MetricType;
import com.sentrysoftware.metricshub.engine.connector.model.metric.StateSet;
import com.sentrysoftware.metricshub.engine.matsya.MatsyaClientsExecutor;
import com.sentrysoftware.metricshub.engine.strategy.AbstractStrategy;
import com.sentrysoftware.metricshub.engine.telemetry.HostProperties;
import com.sentrysoftware.metricshub.engine.telemetry.MetricFactory;
import com.sentrysoftware.metricshub.engine.telemetry.Monitor;
import com.sentrysoftware.metricshub.engine.telemetry.MonitorFactory;
import com.sentrysoftware.metricshub.engine.telemetry.TelemetryManager;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import lombok.Builder;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;
import lombok.NonNull;
import lombok.extern.slf4j.Slf4j;

@NoArgsConstructor
@EqualsAndHashCode(callSuper = true)
@Slf4j
public class DetectionStrategy extends AbstractStrategy {

	/**
	 * Format for string value like: <em>connector_connector-id</em>
	 */
	static final String CONNECTOR_ID_FORMAT = "%s_%s";

	@Builder
	public DetectionStrategy(
		@NonNull final TelemetryManager telemetryManager,
		@NonNull final Long strategyTime,
		@NonNull final MatsyaClientsExecutor matsyaClientsExecutor
	) {
		super(telemetryManager, strategyTime, matsyaClientsExecutor);
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

		final Set<String> selectedConnectors = hostConfiguration.getSelectedConnectors();
		final List<ConnectorTestResult> connectorTestResults;
		// If one or more connector are selected, we run them
		if (selectedConnectors != null && !selectedConnectors.isEmpty()) {
			connectorTestResults = new ConnectorSelection(telemetryManager, matsyaClientsExecutor).run();
		} else { // Else we run the automatic detection
			connectorTestResults = new AutomaticDetection(telemetryManager, matsyaClientsExecutor).run();
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
			connector.getConnectorIdentity().getDetection().getAppliesTo().toString()
		);
		monitorAttributes.put("description", connector.getConnectorIdentity().getInformation());
		monitorAttributes.put("hw.parent.id", hostId);

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
			String.format(CONNECTOR_ID_FORMAT, KnownMonitorType.CONNECTOR.getKey(), connectorId, strategyTime)
		);

		// Get monitor metrics from connector
		final Map<String, MetricDefinition> metricDefinitionMap = connector.getMetrics();

		// Init the metric factory to collect metrics
		final MetricFactory metricFactory = new MetricFactory(telemetryManager.getHostname());

		if (metricDefinitionMap == null) {
			metricFactory.collectConnectorStatusNumberMetric(connectorTestResult, monitor, strategyTime);
			return;
		}

		final MetricDefinition metricDefinition = metricDefinitionMap.get(CONNECTOR_STATUS_METRIC_KEY);

		// Check whether metric type is Enum
		if (metricDefinition == null || (metricDefinition.getType() instanceof MetricType)) {
			metricFactory.collectConnectorStatusNumberMetric(connectorTestResult, monitor, strategyTime);
		} else if (metricDefinition.getType() instanceof StateSet stateSetType) {
			// When metric type is stateSet
			final String[] stateSet = stateSetType.getSet().stream().toArray(String[]::new);
			if (connectorTestResult.isSuccess()) {
				metricFactory.collectStateSetMetric(
					monitor,
					CONNECTOR_STATUS_METRIC_KEY,
					STATE_SET_METRIC_OK,
					stateSet,
					strategyTime
				);
			} else {
				metricFactory.collectStateSetMetric(
					monitor,
					CONNECTOR_STATUS_METRIC_KEY,
					STATE_SET_METRIC_FAILED,
					stateSet,
					strategyTime
				);
			}
		}
	}
}
