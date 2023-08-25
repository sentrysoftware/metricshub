package com.sentrysoftware.matrix.strategy.detection;

import com.sentrysoftware.matrix.common.helpers.KnownMonitorType;
import com.sentrysoftware.matrix.common.helpers.NetworkHelper;
import com.sentrysoftware.matrix.configuration.HostConfiguration;
import com.sentrysoftware.matrix.connector.model.Connector;
import com.sentrysoftware.matrix.connector.model.metric.MetricDefinition;
import com.sentrysoftware.matrix.connector.model.metric.MetricType;
import com.sentrysoftware.matrix.connector.model.metric.StateSet;
import com.sentrysoftware.matrix.strategy.AbstractStrategy;
import com.sentrysoftware.matrix.telemetry.HostProperties;
import com.sentrysoftware.matrix.telemetry.Monitor;
import com.sentrysoftware.matrix.telemetry.MonitorFactory;
import com.sentrysoftware.matrix.telemetry.TelemetryManager;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;
import lombok.extern.slf4j.Slf4j;

import java.net.UnknownHostException;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

import static com.sentrysoftware.matrix.common.helpers.MatrixConstants.CONNECTOR_STATUS_METRIC_KEY;
import static com.sentrysoftware.matrix.common.helpers.MatrixConstants.MONITOR_ATTRIBUTE_APPLIES_TO_OS;
import static com.sentrysoftware.matrix.common.helpers.MatrixConstants.MONITOR_ATTRIBUTE_CONNECTOR_ID;
import static com.sentrysoftware.matrix.common.helpers.MatrixConstants.MONITOR_ATTRIBUTE_DESCRIPTION;
import static com.sentrysoftware.matrix.common.helpers.MatrixConstants.MONITOR_ATTRIBUTE_ID;
import static com.sentrysoftware.matrix.common.helpers.MatrixConstants.MONITOR_ATTRIBUTE_NAME;
import static com.sentrysoftware.matrix.common.helpers.MatrixConstants.MONITOR_ATTRIBUTE_PARENT;
import static com.sentrysoftware.matrix.common.helpers.MatrixConstants.STATE_SET;
import static com.sentrysoftware.matrix.common.helpers.MatrixConstants.STATE_SET_METRIC_FAILED;
import static com.sentrysoftware.matrix.common.helpers.MatrixConstants.STATE_SET_METRIC_OK;

@NoArgsConstructor
@EqualsAndHashCode(callSuper = true)
@Slf4j
public class DetectionStrategy extends AbstractStrategy {

	public DetectionStrategy(
		final TelemetryManager telemetryManager,
		final long strategyTime
	) {
		this.telemetryManager = telemetryManager;
		this.strategyTime = strategyTime;
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
			connectorTestResults = new ConnectorSelection(telemetryManager).run();
		} else { // Else we run the automatic detection
			connectorTestResults = new AutomaticDetection(telemetryManager).run();
		}

		// Create Host monitor
		final MonitorFactory monitorFactory = new MonitorFactory();
		monitorFactory.createHostMonitor(hostProperties.isLocalhost());

		// Create monitors
		createMonitors(connectorTestResults);
	}

	@Override
	public void prepare() {
		// Not implemented

	}

	@Override
	public void post() {
		// Not implemented

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

		// Create the monitor factory
		final MonitorFactory monitorFactory = MonitorFactory.builder().telemetryManager(telemetryManager).build();

		// Set monitor attributes
		final Map<String, String> monitorAttributes = new HashMap<>();
		final String hostId = telemetryManager.getHostConfiguration().getHostId();
		final String connectorCompiledFileName = connector.getConnectorIdentity().getCompiledFilename();
		monitorAttributes.put(MONITOR_ATTRIBUTE_ID, hostId + "@" + connectorCompiledFileName);
		monitorAttributes.put(MONITOR_ATTRIBUTE_NAME, connectorCompiledFileName);
		monitorAttributes.put(MONITOR_ATTRIBUTE_CONNECTOR_ID, connectorCompiledFileName);
		monitorAttributes.put(MONITOR_ATTRIBUTE_APPLIES_TO_OS, connector.getConnectorIdentity().getDetection()
			.getAppliesTo().toString());
		monitorAttributes.put(MONITOR_ATTRIBUTE_DESCRIPTION, connector.getConnectorIdentity().getInformation());
		monitorAttributes.put(MONITOR_ATTRIBUTE_PARENT, hostId);

		// Create or update the monitor by calling monitor factory
		final Monitor monitor = monitorFactory.createOrUpdateMonitor(monitorAttributes, null, KnownMonitorType.CONNECTOR.getKey());

		// Get monitor metrics from connector
		final MetricDefinition metricDefinition = connector.getMetrics().get(CONNECTOR_STATUS_METRIC_KEY);

		// Check whether metric type is Enum
		if (metricDefinition != null && metricDefinition.getType() instanceof MetricType) {
			if (connectorTestResult.isSuccess()) {
				monitorFactory.collectNumberMetric(monitor, CONNECTOR_STATUS_METRIC_KEY, 1.0, strategyTime);
			} else {
				monitorFactory.collectNumberMetric(monitor, CONNECTOR_STATUS_METRIC_KEY, 0.0, strategyTime);
			}
		} else if (metricDefinition != null && metricDefinition.getType() instanceof StateSet) {
			// When metric type is stateSet
			if (connectorTestResult.isSuccess()) {
				monitorFactory.collectStateSetMetric(monitor, CONNECTOR_STATUS_METRIC_KEY, STATE_SET_METRIC_OK, STATE_SET, strategyTime);
			} else {
				monitorFactory.collectStateSetMetric(monitor, CONNECTOR_STATUS_METRIC_KEY, STATE_SET_METRIC_FAILED, STATE_SET, strategyTime);
			}
		}
	}
}
