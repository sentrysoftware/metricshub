package com.sentrysoftware.matrix.strategy.detection;

import com.sentrysoftware.matrix.connector.model.Connector;
import com.sentrysoftware.matrix.connector.model.metric.MetricDefinition;
import com.sentrysoftware.matrix.connector.model.metric.MetricType;
import com.sentrysoftware.matrix.connector.model.metric.StateSet;

import com.sentrysoftware.matrix.configuration.HostConfiguration;
import com.sentrysoftware.matrix.strategy.AbstractStrategy;
import com.sentrysoftware.matrix.telemetry.Monitor;
import com.sentrysoftware.matrix.telemetry.MonitorFactory;
import com.sentrysoftware.matrix.telemetry.TelemetryManager;
import com.sentrysoftware.matrix.telemetry.metric.AbstractMetric;
import com.sentrysoftware.matrix.telemetry.metric.NumberMetric;
import com.sentrysoftware.matrix.telemetry.metric.StateSetMetric;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;

import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

import static com.sentrysoftware.matrix.common.helpers.MatrixConstants.METRICS_KEY;
import static com.sentrysoftware.matrix.common.helpers.MatrixConstants.MONITOR_ATTRIBUTE_APPLIES_TO_OS;
import static com.sentrysoftware.matrix.common.helpers.MatrixConstants.MONITOR_ATTRIBUTE_CONNECTOR_ID;
import static com.sentrysoftware.matrix.common.helpers.MatrixConstants.MONITOR_ATTRIBUTE_DETECTION;
import static com.sentrysoftware.matrix.common.helpers.MatrixConstants.MONITOR_ATTRIBUTE_ID;
import static com.sentrysoftware.matrix.common.helpers.MatrixConstants.MONITOR_ATTRIBUTE_NAME;
import static com.sentrysoftware.matrix.common.helpers.MatrixConstants.STATE_SET_METRIC_FAILED;
import static com.sentrysoftware.matrix.common.helpers.MatrixConstants.STATE_SET_METRIC_OK;

@NoArgsConstructor
@EqualsAndHashCode(callSuper = true)
public class DetectionStrategy extends AbstractStrategy {

	public DetectionStrategy(final TelemetryManager telemetryManager) {
		this.telemetryManager = telemetryManager;
	}

	@Override
	public void run() {
		final HostConfiguration hostConfiguration = telemetryManager.getHostConfiguration();
		if (hostConfiguration == null) {
			return;
		}

		final Set<String> selectedConnectors = hostConfiguration.getSelectedConnectors();
		// If one or more connector are selected, we run them
		if (selectedConnectors != null && !selectedConnectors.isEmpty()) {
			new ConnectorSelection().run(telemetryManager);
		} else { // Else we run the automatic detection
			new AutomaticDetection().run(telemetryManager);
		}

	}

	@Override
	public void prepare() {
		// TODO Auto-generated method stub

	}

	@Override
	public void post() {
		// TODO Auto-generated method stub

	}

	/**
	 * This method creates monitors in TelemetryManager given a list of ConnectorTestResult
	 * @param connectorTestResultList List of ConnectorTestResult
	 */
	public void createMonitors(final List<ConnectorTestResult> connectorTestResultList) {
		connectorTestResultList.forEach(connectorTestResult -> createMonitor(connectorTestResult));
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
		if(metricDefinition != null && metricDefinition.getType() instanceof MetricType){
			final NumberMetric numberMetric = new NumberMetric();
			if(connectorTestResult.isSuccess()){
				numberMetric.setValue(1.0);
			} else {
				numberMetric.setValue(0.0);
			}
			numberMetric.setCollectTime(new Date().getTime());
			monitorMetrics.put(METRICS_KEY, numberMetric);
		} else if(metricDefinition != null && metricDefinition.getType() instanceof StateSet){
			// When metric type is stateSet
			final StateSetMetric stateSetMetric = new StateSetMetric();
			if(connectorTestResult.isSuccess()){
				stateSetMetric.setValue(STATE_SET_METRIC_OK);
			} else {
				stateSetMetric.setValue(STATE_SET_METRIC_FAILED);
			}
			stateSetMetric.setCollectTime(new Date().getTime());
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
		monitorsMap.put(monitor.getAttributes().get(MONITOR_ATTRIBUTE_ID).toString(), monitor);

		// Set the monitor in telemetryManager
		telemetryManager.getMonitors().put(connector.getConnectorIdentity().getCompiledFilename(), monitorsMap);

		// Set telemetryManager in MonitorFactory instance
		final MonitorFactory monitorFactory = MonitorFactory.builder()
			.telemetryManager(telemetryManager)
			.build();
	}
}
