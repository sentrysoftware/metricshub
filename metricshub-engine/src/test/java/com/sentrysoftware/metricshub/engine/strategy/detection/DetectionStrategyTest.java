package com.sentrysoftware.metricshub.engine.strategy.detection;

import static com.sentrysoftware.metricshub.engine.common.helpers.MetricsHubConstants.CONNECTOR_STATUS_METRIC_KEY;
import static com.sentrysoftware.metricshub.engine.common.helpers.MetricsHubConstants.MONITOR_ATTRIBUTE_APPLIES_TO_OS;
import static com.sentrysoftware.metricshub.engine.common.helpers.MetricsHubConstants.MONITOR_ATTRIBUTE_CONNECTOR_ID;
import static com.sentrysoftware.metricshub.engine.common.helpers.MetricsHubConstants.MONITOR_ATTRIBUTE_ID;
import static com.sentrysoftware.metricshub.engine.common.helpers.MetricsHubConstants.MONITOR_ATTRIBUTE_NAME;
import static com.sentrysoftware.metricshub.engine.common.helpers.MetricsHubConstants.STATE_SET_METRIC_FAILED;
import static com.sentrysoftware.metricshub.engine.common.helpers.MetricsHubConstants.STATE_SET_METRIC_OK;
import static com.sentrysoftware.metricshub.engine.constants.Constants.HOST_ID;
import static com.sentrysoftware.metricshub.engine.constants.Constants.LINUX;
import static com.sentrysoftware.metricshub.engine.constants.Constants.LOCALHOST;
import static com.sentrysoftware.metricshub.engine.constants.Constants.STATE_SET;
import static com.sentrysoftware.metricshub.engine.constants.Constants.WINDOWS;
import static com.sentrysoftware.metricshub.engine.constants.Constants.YAML_TEST_FILE_NAME;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

import com.sentrysoftware.metricshub.engine.common.helpers.KnownMonitorType;
import com.sentrysoftware.metricshub.engine.configuration.HostConfiguration;
import com.sentrysoftware.metricshub.engine.connector.model.Connector;
import com.sentrysoftware.metricshub.engine.connector.model.common.DeviceKind;
import com.sentrysoftware.metricshub.engine.connector.model.metric.MetricDefinition;
import com.sentrysoftware.metricshub.engine.connector.model.metric.MetricType;
import com.sentrysoftware.metricshub.engine.connector.model.metric.StateSet;
import com.sentrysoftware.metricshub.engine.connector.parser.ConnectorLibraryParser;
import com.sentrysoftware.metricshub.engine.matsya.MatsyaClientsExecutor;
import com.sentrysoftware.metricshub.engine.telemetry.Monitor;
import com.sentrysoftware.metricshub.engine.telemetry.TelemetryManager;
import com.sentrysoftware.metricshub.engine.telemetry.metric.AbstractMetric;
import com.sentrysoftware.metricshub.engine.telemetry.metric.NumberMetric;
import com.sentrysoftware.metricshub.engine.telemetry.metric.StateSetMetric;
import java.io.IOException;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import org.junit.jupiter.api.Test;

class DetectionStrategyTest {

	Connector getConnector() throws IOException {
		// Retrieve connectors from connectors directory
		final Path yamlTestPath = Paths.get(
			"src",
			"test",
			"resources",
			"test-files",
			"connector",
			"connectorLibraryParser"
		);
		final Map<String, Connector> connectors = new ConnectorLibraryParser()
			.parseConnectorsFromAllYamlFiles(yamlTestPath);
		return connectors.get(YAML_TEST_FILE_NAME + ".yaml");
	}

	@Test
	void testCreateMonitors() throws IOException {
		// Retrieve connector from connectors directory
		final Connector connector = getConnector();
		final Map<String, MetricDefinition> connectorMetrics = new HashMap<>();
		connectorMetrics.put(CONNECTOR_STATUS_METRIC_KEY, MetricDefinition.builder().type(MetricType.GAUGE).build());
		connector.setMetrics(connectorMetrics);

		// Initiate telemetryManager with host configuration
		final TelemetryManager telemetryManager = TelemetryManager
			.builder()
			.hostConfiguration(
				HostConfiguration.builder().hostId(HOST_ID).hostType(DeviceKind.LINUX).hostname(LOCALHOST).build()
			)
			.build();

		// Create detectionStrategy with the previously created telemetryManager
		final DetectionStrategy detectionStrategy = new DetectionStrategy(
			telemetryManager,
			new Date().getTime(),
			new MatsyaClientsExecutor(telemetryManager)
		);

		// Create a list of CriterionTestResult
		final List<CriterionTestResult> criterionTestResultList = new ArrayList<>();

		// Create an instance of CriterionTestResult with success set to true and add to the CriterionTestResult list
		final CriterionTestResult criterionTestResult = CriterionTestResult.builder().success(true).build();
		criterionTestResultList.add(criterionTestResult);

		// Create a ConnectorTestResult instance and set the connector and the previously created criterionTestResultList
		final ConnectorTestResult connectorTestResult = ConnectorTestResult
			.builder()
			.connector(connector)
			.criterionTestResults(criterionTestResultList)
			.build();

		// Create a ConnectorTestResult list and add the previously created connectorTestResult to it
		final List<ConnectorTestResult> connectorTestResultList = new ArrayList<>();
		connectorTestResultList.add(connectorTestResult);

		// Call DetectionStrategy and invoke create monitors with the previously created connectorTestResultList instance
		detectionStrategy.createMonitors(connectorTestResultList);

		final String compiledFilename = connector.getCompiledFilename();

		// Check monitor attributes
		final String monitorId = String.format("%s_%s", KnownMonitorType.CONNECTOR.getKey(), compiledFilename);

		assertEquals(1, telemetryManager.getMonitors().size());
		assertEquals(1, telemetryManager.getMonitors().get(KnownMonitorType.CONNECTOR.getKey()).size());
		assertEquals(
			"AAC",
			telemetryManager
				.getMonitors()
				.get(KnownMonitorType.CONNECTOR.getKey())
				.get(monitorId)
				.getAttributes()
				.get(MONITOR_ATTRIBUTE_ID)
		);
		assertEquals(
			YAML_TEST_FILE_NAME,
			telemetryManager
				.getMonitors()
				.get(KnownMonitorType.CONNECTOR.getKey())
				.get(monitorId)
				.getAttributes()
				.get(MONITOR_ATTRIBUTE_NAME)
		);
		assertEquals(
			YAML_TEST_FILE_NAME,
			telemetryManager
				.getMonitors()
				.get(KnownMonitorType.CONNECTOR.getKey())
				.get(monitorId)
				.getAttributes()
				.get(MONITOR_ATTRIBUTE_CONNECTOR_ID)
		);
		assertTrue(
			telemetryManager
				.getMonitors()
				.get(KnownMonitorType.CONNECTOR.getKey())
				.get(monitorId)
				.getAttributes()
				.get(MONITOR_ATTRIBUTE_APPLIES_TO_OS)
				.contains(LINUX)
		);
		assertTrue(
			telemetryManager
				.getMonitors()
				.get(KnownMonitorType.CONNECTOR.getKey())
				.get(monitorId)
				.getAttributes()
				.get(MONITOR_ATTRIBUTE_APPLIES_TO_OS)
				.contains(WINDOWS)
		);

		// Check monitor metrics
		NumberMetric numberMetric = (NumberMetric) telemetryManager
			.getMonitors()
			.get(KnownMonitorType.CONNECTOR.getKey())
			.get(monitorId)
			.getMetrics()
			.get(CONNECTOR_STATUS_METRIC_KEY);
		assertEquals(1.0, numberMetric.getValue());

		// Set criterion test result to isSuccess = false, call DetectionStrategy and expect the metric to be set to 0.0
		criterionTestResult.setSuccess(false);
		detectionStrategy.createMonitors(connectorTestResultList);
		numberMetric =
			(NumberMetric) telemetryManager
				.getMonitors()
				.get(KnownMonitorType.CONNECTOR.getKey())
				.get(monitorId)
				.getMetrics()
				.get(CONNECTOR_STATUS_METRIC_KEY);
		assertEquals(0.0, numberMetric.getValue());
	}

	@Test
	void testCollectNumberMetricUsingConnectorResult() throws IOException {
		// Retrieve connector from connectors directory
		final Connector connector = getConnector();

		// Create a list of CriterionTestResult
		final List<CriterionTestResult> criterionTestResultList = new ArrayList<>();

		// Create an instance of CriterionTestResult with success set to true and add to the CriterionTestResult list
		final CriterionTestResult criterionTestResult = CriterionTestResult.builder().success(true).build();
		criterionTestResultList.add(criterionTestResult);

		// Create a ConnectorTestResult instance and set the connector and the previously created criterionTestResultList
		final ConnectorTestResult connectorTestResult = ConnectorTestResult
			.builder()
			.connector(connector)
			.criterionTestResults(criterionTestResultList)
			.build();

		// Initiate telemetryManager with host configuration
		final TelemetryManager telemetryManager = TelemetryManager
			.builder()
			.hostConfiguration(
				HostConfiguration.builder().hostId(HOST_ID).hostType(DeviceKind.LINUX).hostname(LOCALHOST).build()
			)
			.build();

		// Create detectionStrategy with the previously created telemetryManager
		final DetectionStrategy detectionStrategy = new DetectionStrategy(
			telemetryManager,
			new Date().getTime(),
			new MatsyaClientsExecutor(telemetryManager)
		);

		// Set a NumberMetric in the connector
		connector.setMetrics(
			Map.of(CONNECTOR_STATUS_METRIC_KEY, MetricDefinition.builder().type(MetricType.GAUGE).build())
		);
		detectionStrategy.createMonitor(connectorTestResult);

		// Retrieve the created monitor
		final Map<String, Monitor> connectorMonitorMap = telemetryManager
			.getMonitors()
			.get(KnownMonitorType.CONNECTOR.getKey());
		final String compiledFilename = connector.getConnectorIdentity().getCompiledFilename();
		final Monitor monitor = connectorMonitorMap.get(
			String.format(DetectionStrategy.CONNECTOR_ID_FORMAT, KnownMonitorType.CONNECTOR.getKey(), compiledFilename)
		);

		// Retrieve monitor's metric value (ConnectorResult is successful)
		AbstractMetric metric = monitor.getMetrics().get(CONNECTOR_STATUS_METRIC_KEY);
		assertTrue(metric instanceof NumberMetric);
		assertEquals(1.0, ((NumberMetric) metric).getValue());

		// Switch CriterionTestResult success to false
		criterionTestResult.setSuccess(false);

		// Call create monitor again with criterionTestResult set to failed
		detectionStrategy.createMonitor(connectorTestResult);

		// Retrieve monitor's metric value(ConnectorResult is failed)
		metric = monitor.getMetrics().get(CONNECTOR_STATUS_METRIC_KEY);
		assertEquals(0.0, ((NumberMetric) metric).getValue());
	}

	@Test
	void testCollectStateSetMetricUsingConnectorResult() throws IOException {
		// Retrieve connector from connectors directory
		final Connector connector = getConnector();

		// Create a list of CriterionTestResult
		final List<CriterionTestResult> criterionTestResultList = new ArrayList<>();

		// Create an instance of CriterionTestResult with success set to true and add to the CriterionTestResult list
		final CriterionTestResult criterionTestResult = CriterionTestResult.builder().success(true).build();
		criterionTestResultList.add(criterionTestResult);

		// Create a ConnectorTestResult instance and set the connector and the previously created criterionTestResultList
		final ConnectorTestResult connectorTestResult = ConnectorTestResult
			.builder()
			.connector(connector)
			.criterionTestResults(criterionTestResultList)
			.build();

		// Initiate telemetryManager with host configuration
		final TelemetryManager telemetryManager = TelemetryManager
			.builder()
			.hostConfiguration(
				HostConfiguration.builder().hostId(HOST_ID).hostType(DeviceKind.LINUX).hostname(LOCALHOST).build()
			)
			.build();

		// Create detectionStrategy with the previously created telemetryManager
		final DetectionStrategy detectionStrategy = new DetectionStrategy(
			telemetryManager,
			new Date().getTime(),
			new MatsyaClientsExecutor(telemetryManager)
		);

		// Set a StateSetMetric in the connector
		final StateSet stateSet = new StateSet();
		stateSet.setSet(Set.of(STATE_SET[0], STATE_SET[1], STATE_SET[2]));
		connector.setMetrics(Map.of(CONNECTOR_STATUS_METRIC_KEY, MetricDefinition.builder().type(stateSet).build()));
		detectionStrategy.createMonitor(connectorTestResult);

		// Retrieve the created monitor
		final Map<String, Monitor> connectorMonitorMap = telemetryManager
			.getMonitors()
			.get(KnownMonitorType.CONNECTOR.getKey());
		final String compiledFilename = connector.getConnectorIdentity().getCompiledFilename();
		final Monitor monitor = connectorMonitorMap.get(
			String.format(DetectionStrategy.CONNECTOR_ID_FORMAT, KnownMonitorType.CONNECTOR.getKey(), compiledFilename)
		);
		// Retrieve monitor's metric value (ConnectorResult is successful)
		AbstractMetric metric = monitor.getMetrics().get(CONNECTOR_STATUS_METRIC_KEY);
		assertTrue(metric instanceof StateSetMetric);
		assertEquals(STATE_SET_METRIC_OK, ((StateSetMetric) metric).getValue());

		// Switch CriterionTestResult success to false
		criterionTestResult.setSuccess(false);

		// Call create monitor again with criterionTestResult set to failed
		detectionStrategy.createMonitor(connectorTestResult);

		// Retrieve monitor's metric value (ConnectorResult is failed)
		metric = monitor.getMetrics().get(CONNECTOR_STATUS_METRIC_KEY);
		assertEquals(STATE_SET_METRIC_FAILED, ((StateSetMetric) metric).getValue());
	}
}
