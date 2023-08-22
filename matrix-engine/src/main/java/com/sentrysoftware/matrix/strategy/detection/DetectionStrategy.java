package com.sentrysoftware.matrix.strategy.detection;

import static com.sentrysoftware.matrix.common.helpers.MatrixConstants.*;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

import com.sentrysoftware.matrix.common.helpers.KnownMonitorType;
import com.sentrysoftware.matrix.common.helpers.NetworkHelper;
import com.sentrysoftware.matrix.configuration.HostConfiguration;
import com.sentrysoftware.matrix.connector.model.Connector;
import com.sentrysoftware.matrix.connector.model.metric.MetricDefinition;
import com.sentrysoftware.matrix.connector.model.metric.MetricType;
import com.sentrysoftware.matrix.connector.model.metric.StateSet;
import com.sentrysoftware.matrix.strategy.AbstractStrategy;
import com.sentrysoftware.matrix.telemetry.Monitor;
import com.sentrysoftware.matrix.telemetry.MonitorFactory;
import com.sentrysoftware.matrix.telemetry.TelemetryManager;
import com.sentrysoftware.matrix.telemetry.metric.AbstractMetric;
import com.sentrysoftware.matrix.telemetry.metric.NumberMetric;
import com.sentrysoftware.matrix.telemetry.metric.StateSetMetric;

import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;
import lombok.extern.slf4j.Slf4j;

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
		if (hostConfiguration == null) {
			return;
		}

		final String hostname = hostConfiguration.getHostname();
		log.debug("Hostname {} - Start detection strategy.", hostname);
		
		// Detect if we monitor localhost then set the localhost property in the HostProperties instance
		telemetryManager
			.getHostProperties()
			.setLocalhost(NetworkHelper.isLocalhost(hostname));

		final Set<String> selectedConnectors = hostConfiguration.getSelectedConnectors();
		final List<ConnectorTestResult> connectorTestResults;
		// If one or more connector are selected, we run them
		if (selectedConnectors != null && !selectedConnectors.isEmpty()) {
			connectorTestResults = new ConnectorSelection(telemetryManager).run();
		} else { // Else we run the automatic detection
			connectorTestResults = new AutomaticDetection(telemetryManager).run();
		}

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
	 * @param connectorTestResultList List of ConnectorTestResult
	 */
	void createMonitors(final List<ConnectorTestResult> connectorTestResultList) {
		connectorTestResultList.forEach(this::createMonitor);
	}

	/**
	 * This method creates a monitor in TelemetryManager for a given ConnectorTestResult instance
	 * @param connectorTestResult ConnectorTestResult instance
	 */
	public void createMonitor(final ConnectorTestResult connectorTestResult) {
		final Connector connector = connectorTestResult.getConnector();
		final Map<String, AbstractMetric> monitorMetrics = new HashMap<>();
		final Map<String, String> monitorAttributes = new HashMap<>();

		// Get monitor metrics from connector
		final MetricDefinition metricDefinition = connector.getMetrics().get(METRICS_KEY);

		// Check whether metric type is Enum
		if (metricDefinition != null && metricDefinition.getType() instanceof MetricType) {
			final NumberMetric numberMetric = new NumberMetric();
			if (connectorTestResult.isSuccess()) {
				numberMetric.setValue(1.0);
			} else {
				numberMetric.setValue(0.0);
			}
			numberMetric.setCollectTime(strategyTime);
			monitorMetrics.put(METRICS_KEY, numberMetric);
		} else if (metricDefinition != null && metricDefinition.getType() instanceof StateSet) {
			// When metric type is stateSet
			final StateSetMetric stateSetMetric = new StateSetMetric();
			if (connectorTestResult.isSuccess()) {
				stateSetMetric.setValue(STATE_SET_METRIC_OK);
			} else {
				stateSetMetric.setValue(STATE_SET_METRIC_FAILED);
			}
			stateSetMetric.setCollectTime(strategyTime);
			monitorMetrics.put(METRICS_KEY, stateSetMetric);
		}

		// Get monitor attributes
		monitorAttributes.put(MONITOR_ATTRIBUTE_ID, telemetryManager.getHostConfiguration().getHostId() + "@" + connector.getConnectorIdentity().getCompiledFilename());
		monitorAttributes.put(MONITOR_ATTRIBUTE_NAME, connector.getConnectorIdentity().getCompiledFilename());
		monitorAttributes.put(MONITOR_ATTRIBUTE_CONNECTOR_ID, connector.getConnectorIdentity().getCompiledFilename());
		monitorAttributes.put(MONITOR_ATTRIBUTE_APPLIES_TO_OS, connector.getConnectorIdentity().getDetection().getAppliesTo().toString());
		monitorAttributes.put(MONITOR_ATTRIBUTE_DETECTION, connector.getConnectorIdentity().getDetection().toString());

		// Build a monitor object from attributes and metrics
		final Monitor monitor = Monitor.builder()
			.attributes(monitorAttributes)
			.metrics(monitorMetrics)
			.build();

		final Map<String, Monitor> monitorsMap = new HashMap<>();
		monitorsMap.put(monitor.getAttributes().get(MONITOR_ATTRIBUTE_ID), monitor);

		// Set the monitor in telemetryManager
		telemetryManager.getMonitors().put(KnownMonitorType.CONNECTOR.getKey(), monitorsMap);

		// Set telemetryManager in MonitorFactory instance
		final MonitorFactory monitorFactory = MonitorFactory
			.builder()
			.telemetryManager(telemetryManager)
			.build();
	}
}
