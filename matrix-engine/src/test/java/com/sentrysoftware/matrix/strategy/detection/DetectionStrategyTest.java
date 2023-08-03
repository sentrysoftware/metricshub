package com.sentrysoftware.matrix.strategy.detection;

import com.sentrysoftware.matrix.configuration.HostConfiguration;
import com.sentrysoftware.matrix.connector.model.Connector;
import com.sentrysoftware.matrix.connector.model.common.DeviceKind;
import com.sentrysoftware.matrix.connector.model.metric.MetricDefinition;
import com.sentrysoftware.matrix.connector.model.metric.MetricType;
import com.sentrysoftware.matrix.connector.parser.ConnectorLibraryParser;
import com.sentrysoftware.matrix.telemetry.TelemetryManager;
import com.sentrysoftware.matrix.telemetry.metric.NumberMetric;
import org.junit.jupiter.api.Test;

import java.io.IOException;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static com.sentrysoftware.matrix.common.helpers.MatrixConstants.METRICS_KEY;
import static com.sentrysoftware.matrix.common.helpers.MatrixConstants.MONITOR_ATTRIBUTE_APPLIES_TO_OS;
import static com.sentrysoftware.matrix.common.helpers.MatrixConstants.MONITOR_ATTRIBUTE_CONNECTOR_ID;
import static com.sentrysoftware.matrix.common.helpers.MatrixConstants.MONITOR_ATTRIBUTE_ID;
import static com.sentrysoftware.matrix.common.helpers.MatrixConstants.MONITOR_ATTRIBUTE_NAME;
import static com.sentrysoftware.matrix.constants.Constants.HOST_ID;
import static com.sentrysoftware.matrix.constants.Constants.LINUX;
import static com.sentrysoftware.matrix.constants.Constants.LOCALHOST;
import static com.sentrysoftware.matrix.constants.Constants.WINDOWS;
import static com.sentrysoftware.matrix.constants.Constants.YAML_TEST_FILE_NAME;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

public class DetectionStrategyTest {

	@Test
	public void testCreateMonitors() throws IOException {

		// Retrieve connectors from connectors directory
		final Path yamlTestPath = Paths.get("src", "test", "resources", "test-files", "connector", "connectorLibraryParser");
		final Map<String, Connector> connectors = new ConnectorLibraryParser().parseConnectorsFromAllYamlFiles(yamlTestPath);
		final Connector connector = connectors.get(YAML_TEST_FILE_NAME + ".yaml");
		final Map<String, MetricDefinition> connectorMetrics = new HashMap<>();
		connectorMetrics.put(METRICS_KEY, MetricDefinition.builder().type(MetricType.GAUGE).build());
		connector.setMetrics(connectorMetrics);

		// Initiate telemetryManager with host configuration
        final TelemetryManager telemetryManager = TelemetryManager.builder()
			.hostConfiguration(HostConfiguration.builder().hostId(HOST_ID).hostType(DeviceKind.LINUX).hostname(LOCALHOST).build())
			.build();

		// Create detectionStrategy with the previously created telemetryManager
		final DetectionStrategy detectionStrategy = new DetectionStrategy(telemetryManager);

		// Create a list of CriterionTestResult
		final List<CriterionTestResult> criterionTestResultList = new ArrayList<>();

		// Create an instance of CriterionTestResult with success set to true and add to the CriterionTestResult list
		final CriterionTestResult criterionTestResult = CriterionTestResult.builder()
			.success(true)
			.build();
		criterionTestResultList.add(criterionTestResult);

		// Create a ConnectorTestResult instance and set the connector and the previously created criterionTestResultList
		final ConnectorTestResult connectorTestResult  = ConnectorTestResult.builder()
			.connector(connector)
			.criterionTestResults(criterionTestResultList)
			.build();

		// Create a ConnectorTestResult list and add the previously created connectorTestResult to it
		final List<ConnectorTestResult> connectorTestResultList = new ArrayList<>();
		connectorTestResultList.add(connectorTestResult);

		// Call DetectionStrategy and invoke create monitors with the previously created connectorTestResultList instance
		detectionStrategy.createMonitors(connectorTestResultList);

		// Check monitor attributes
		final String monitorId = HOST_ID + "@" + YAML_TEST_FILE_NAME;
		assertEquals(1, telemetryManager.getMonitors().size());
		assertEquals(1, telemetryManager.getMonitors().get(YAML_TEST_FILE_NAME).size());
		assertEquals(monitorId, telemetryManager.getMonitors().get(YAML_TEST_FILE_NAME)
			.get(monitorId).getAttributes().get(MONITOR_ATTRIBUTE_ID));
		assertEquals(YAML_TEST_FILE_NAME, telemetryManager.getMonitors().get(YAML_TEST_FILE_NAME).get(monitorId).getAttributes().get(MONITOR_ATTRIBUTE_NAME));
		assertEquals(YAML_TEST_FILE_NAME, telemetryManager.getMonitors().get(YAML_TEST_FILE_NAME).get(monitorId).getAttributes().get(MONITOR_ATTRIBUTE_CONNECTOR_ID));
		assertTrue(telemetryManager.getMonitors().get(YAML_TEST_FILE_NAME).get(monitorId).getAttributes().get(MONITOR_ATTRIBUTE_APPLIES_TO_OS).contains(LINUX));
		assertTrue(telemetryManager.getMonitors().get(YAML_TEST_FILE_NAME).get(monitorId).getAttributes().get(MONITOR_ATTRIBUTE_APPLIES_TO_OS).contains(WINDOWS));

		// Check monitor metrics
		NumberMetric numberMetric = (NumberMetric) telemetryManager.getMonitors().get(YAML_TEST_FILE_NAME).get(monitorId).getMetrics().get(METRICS_KEY);
		assertEquals(1.0, numberMetric.getValue());

		// Set criterion test result to isSuccess = false, call DetectionStrategy and expect the metric to be set to 0.0
		criterionTestResult.setSuccess(false);
		detectionStrategy.createMonitors(connectorTestResultList);
		numberMetric = (NumberMetric) telemetryManager.getMonitors().get(YAML_TEST_FILE_NAME).get(monitorId).getMetrics().get(METRICS_KEY);
		assertEquals(0.0, numberMetric.getValue());

	}
}
