package org.sentrysoftware.metricshub.engine.strategy.detection;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.sentrysoftware.metricshub.engine.common.helpers.KnownMonitorType.HOST;
import static org.sentrysoftware.metricshub.engine.common.helpers.MetricsHubConstants.CONNECTOR_STATUS_METRIC_KEY;
import static org.sentrysoftware.metricshub.engine.common.helpers.MetricsHubConstants.MONITOR_ATTRIBUTE_APPLIES_TO_OS;
import static org.sentrysoftware.metricshub.engine.common.helpers.MetricsHubConstants.MONITOR_ATTRIBUTE_CONNECTOR_ID;
import static org.sentrysoftware.metricshub.engine.common.helpers.MetricsHubConstants.MONITOR_ATTRIBUTE_ID;
import static org.sentrysoftware.metricshub.engine.common.helpers.MetricsHubConstants.MONITOR_ATTRIBUTE_NAME;
import static org.sentrysoftware.metricshub.engine.common.helpers.MetricsHubConstants.MONITOR_ATTRIBUTE_PARENT_ID;
import static org.sentrysoftware.metricshub.engine.common.helpers.MetricsHubConstants.STATE_SET_METRIC_FAILED;
import static org.sentrysoftware.metricshub.engine.common.helpers.MetricsHubConstants.STATE_SET_METRIC_OK;
import static org.sentrysoftware.metricshub.engine.constants.Constants.AAC_CONNECTOR_ID;
import static org.sentrysoftware.metricshub.engine.constants.Constants.HOSTNAME;
import static org.sentrysoftware.metricshub.engine.constants.Constants.HOST_ID;
import static org.sentrysoftware.metricshub.engine.constants.Constants.LOCALHOST;
import static org.sentrysoftware.metricshub.engine.constants.Constants.STATE_SET;
import static org.sentrysoftware.metricshub.engine.strategy.AbstractStrategy.CONNECTOR_ID_FORMAT;

import java.io.IOException;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;
import java.util.stream.Stream;
import org.junit.jupiter.api.Test;
import org.sentrysoftware.metricshub.engine.client.ClientsExecutor;
import org.sentrysoftware.metricshub.engine.common.helpers.KnownMonitorType;
import org.sentrysoftware.metricshub.engine.configuration.HostConfiguration;
import org.sentrysoftware.metricshub.engine.connector.model.Connector;
import org.sentrysoftware.metricshub.engine.connector.model.ConnectorStore;
import org.sentrysoftware.metricshub.engine.connector.model.common.DeviceKind;
import org.sentrysoftware.metricshub.engine.connector.model.identity.ConnectorIdentity;
import org.sentrysoftware.metricshub.engine.connector.model.identity.Detection;
import org.sentrysoftware.metricshub.engine.connector.model.identity.criterion.OsCommandCriterion;
import org.sentrysoftware.metricshub.engine.connector.model.metric.MetricDefinition;
import org.sentrysoftware.metricshub.engine.connector.model.metric.MetricType;
import org.sentrysoftware.metricshub.engine.connector.model.metric.StateSet;
import org.sentrysoftware.metricshub.engine.connector.model.monitor.task.source.OsCommandSource;
import org.sentrysoftware.metricshub.engine.connector.parser.ConnectorLibraryParser;
import org.sentrysoftware.metricshub.engine.telemetry.Monitor;
import org.sentrysoftware.metricshub.engine.telemetry.TelemetryManager;
import org.sentrysoftware.metricshub.engine.telemetry.metric.AbstractMetric;
import org.sentrysoftware.metricshub.engine.telemetry.metric.NumberMetric;
import org.sentrysoftware.metricshub.engine.telemetry.metric.StateSetMetric;

class DetectionStrategyTest {

	private static final String METRICS_HUB_CONFIGURED_CONNECTOR_ID = "MetricsHub-Configured-Connector";
	private static final long CURRENT_TIME_MILLIS = System.currentTimeMillis();

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
		return connectors.get(AAC_CONNECTOR_ID);
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
			new ClientsExecutor(telemetryManager)
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
			AAC_CONNECTOR_ID,
			telemetryManager
				.getMonitors()
				.get(KnownMonitorType.CONNECTOR.getKey())
				.get(monitorId)
				.getAttributes()
				.get(MONITOR_ATTRIBUTE_NAME)
		);
		assertEquals(
			AAC_CONNECTOR_ID,
			telemetryManager
				.getMonitors()
				.get(KnownMonitorType.CONNECTOR.getKey())
				.get(monitorId)
				.getAttributes()
				.get(MONITOR_ATTRIBUTE_CONNECTOR_ID)
		);
		assertEquals(
			Stream
				.of(DeviceKind.LINUX, DeviceKind.WINDOWS)
				.sorted()
				.map(deviceKind -> deviceKind.toString().toLowerCase())
				.collect(Collectors.joining(",")),
			telemetryManager
				.getMonitors()
				.get(KnownMonitorType.CONNECTOR.getKey())
				.get(monitorId)
				.getAttributes()
				.get(MONITOR_ATTRIBUTE_APPLIES_TO_OS)
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
			new ClientsExecutor(telemetryManager)
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
			String.format(CONNECTOR_ID_FORMAT, KnownMonitorType.CONNECTOR.getKey(), compiledFilename)
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
			new ClientsExecutor(telemetryManager)
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
			String.format(CONNECTOR_ID_FORMAT, KnownMonitorType.CONNECTOR.getKey(), compiledFilename)
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

	@Test
	void testCreateConfiguredConnectorMonitor() {
		// Initiate telemetryManager with host configuration
		final TelemetryManager telemetryManager = TelemetryManager
			.builder()
			.hostConfiguration(
				HostConfiguration
					.builder()
					.hostId(HOST_ID)
					.hostType(DeviceKind.LINUX)
					.hostname(LOCALHOST)
					.configuredConnectorId(METRICS_HUB_CONFIGURED_CONNECTOR_ID)
					.build()
			)
			.connectorStore(new ConnectorStore())
			.build();

		// Create detectionStrategy with the previously created telemetryManager
		new DetectionStrategy(telemetryManager, new Date().getTime(), new ClientsExecutor(telemetryManager)).run();

		final Monitor configuredConnectorMonitor = telemetryManager.findMonitorByTypeAndId(
			KnownMonitorType.CONNECTOR.getKey(),
			"connector_" + METRICS_HUB_CONFIGURED_CONNECTOR_ID
		);
		assertNotNull(configuredConnectorMonitor);
		assertEquals(METRICS_HUB_CONFIGURED_CONNECTOR_ID, configuredConnectorMonitor.getAttribute(MONITOR_ATTRIBUTE_ID));
		assertEquals(METRICS_HUB_CONFIGURED_CONNECTOR_ID, configuredConnectorMonitor.getAttribute(MONITOR_ATTRIBUTE_NAME));
		assertEquals(HOST_ID, configuredConnectorMonitor.getAttribute(MONITOR_ATTRIBUTE_PARENT_ID));
		assertTrue(
			telemetryManager.getHostProperties().getConnectorNamespace(METRICS_HUB_CONFIGURED_CONNECTOR_ID).isStatusOk()
		);
		assertEquals(1.0, configuredConnectorMonitor.getMetric(CONNECTOR_STATUS_METRIC_KEY, NumberMetric.class).getValue());
	}

	/**
	 * Creates and parameterizes a DetectionStrategy instance
	 * @return a ready-to-use Detection Strategy
	 */
	DetectionStrategy getDetectionStrategy() {
		// Create a host monitor
		Monitor hostMonitor = Monitor.builder().type(HOST.getKey()).isEndpoint(true).build();

		Map<String, Map<String, Monitor>> monitors = new HashMap<>(Map.of(HOST.getKey(), Map.of(HOSTNAME, hostMonitor)));

		// Create a connector with connector identity and a detection
		Connector connector = Connector
			.builder()
			.connectorIdentity(ConnectorIdentity.builder().detection(new Detection()).build())
			.build();

		ConnectorStore store = new ConnectorStore();
		store.setStore(Map.of("connector", connector));

		// Create a telemetry manager without configuration
		TelemetryManager telemetryManager = TelemetryManager
			.builder()
			.monitors(monitors)
			.hostConfiguration(HostConfiguration.builder().hostId(HOSTNAME).hostname(HOSTNAME).build())
			.connectorStore(store)
			.build();

		return new DetectionStrategy(telemetryManager, CURRENT_TIME_MILLIS, new ClientsExecutor());
	}

	@Test
	void testVerifySsh() throws IOException {
		// Call getDetectionStrategy to retrieve a ready-to-use detection strategy
		DetectionStrategy detectionStrategy = getDetectionStrategy();
		TelemetryManager telemetryManager = detectionStrategy.getTelemetryManager();
		Connector connector = telemetryManager.getConnectorStore().getStore().get("connector");

		// verify SSH protocol using
		detectionStrategy.verifySsh(connector);

		// verify Ssh without Source Type and OsCommandCriteria
		assertFalse(telemetryManager.getHostProperties().isMustCheckSshStatus());
		assertFalse(telemetryManager.getHostProperties().isOsCommandExecutesLocally());
		assertFalse(telemetryManager.getHostProperties().isOsCommandExecutesRemotely());

		// verify Ssh with OsCommandSource and OsCommandCriterion that executes locally
		connector.setSourceTypes(Set.of(OsCommandSource.class));
		OsCommandCriterion localCriterion = OsCommandCriterion
			.builder()
			.executeLocally(true)
			.commandLine("command")
			.build();
		connector.getConnectorIdentity().getDetection().setCriteria(List.of(localCriterion));

		detectionStrategy.verifySsh(connector);

		assertTrue(telemetryManager.getHostProperties().isMustCheckSshStatus());
		assertTrue(telemetryManager.getHostProperties().isOsCommandExecutesLocally());
		assertFalse(telemetryManager.getHostProperties().isOsCommandExecutesRemotely());

		// verify Ssh with OsCommandSource and OsCommandCriterion that executes remotely
		OsCommandCriterion remoteCriterion = OsCommandCriterion.builder().commandLine("command").build();
		connector.getConnectorIdentity().getDetection().setCriteria(List.of(remoteCriterion));

		detectionStrategy.verifySsh(connector);

		assertTrue(telemetryManager.getHostProperties().isMustCheckSshStatus());
		assertFalse(telemetryManager.getHostProperties().isOsCommandExecutesLocally());
		assertTrue(telemetryManager.getHostProperties().isOsCommandExecutesRemotely());

		// verify Ssh with OsCommandSource and OsCommandCriteria that executes locally and remotely
		connector.getConnectorIdentity().getDetection().setCriteria(List.of(localCriterion, remoteCriterion));

		detectionStrategy.verifySsh(connector);

		assertTrue(telemetryManager.getHostProperties().isMustCheckSshStatus());
		assertTrue(telemetryManager.getHostProperties().isOsCommandExecutesLocally());
		assertTrue(telemetryManager.getHostProperties().isOsCommandExecutesRemotely());
	}

	@Test
	void testVerifySshCriteria() {
		// Call getDetectionStrategy to retrieve a ready-to-use detection strategy
		DetectionStrategy detectionStrategy = getDetectionStrategy();
		TelemetryManager telemetryManager = detectionStrategy.getTelemetryManager();
		Connector connector = telemetryManager.getConnectorStore().getStore().get("connector");

		// Verify that verifySshCriteria correctly handles a OsCommandCriterion configured to execute only locally
		OsCommandCriterion localCriterion = OsCommandCriterion
			.builder()
			.executeLocally(true)
			.commandLine("command")
			.build();
		connector.getConnectorIdentity().getDetection().setCriteria(List.of(localCriterion));

		detectionStrategy.verifySshCriteria(connector.getConnectorIdentity().getDetection().getCriteria());

		assertTrue(telemetryManager.getHostProperties().isOsCommandExecutesLocally());
		assertFalse(telemetryManager.getHostProperties().isOsCommandExecutesRemotely());

		// Verify that verifySshCriteria correctly handles a OsCommandCriterion configured to execute only remotely
		OsCommandCriterion remoteCriterion = OsCommandCriterion.builder().commandLine("command").build();
		connector.getConnectorIdentity().getDetection().setCriteria(List.of(remoteCriterion));

		detectionStrategy.verifySshCriteria(connector.getConnectorIdentity().getDetection().getCriteria());

		assertFalse(telemetryManager.getHostProperties().isOsCommandExecutesLocally());
		assertTrue(telemetryManager.getHostProperties().isOsCommandExecutesRemotely());

		// Verify that verifySshCriteria correctly handles OsCommandCriteria that executes locally and remotely
		connector.getConnectorIdentity().getDetection().setCriteria(List.of(localCriterion, remoteCriterion));

		detectionStrategy.verifySshCriteria(connector.getConnectorIdentity().getDetection().getCriteria());

		assertTrue(telemetryManager.getHostProperties().isOsCommandExecutesLocally());
		assertTrue(telemetryManager.getHostProperties().isOsCommandExecutesRemotely());

		// Verify that verifySshCriteria correctly return false when there are no OsCommandCriteria
		connector.getConnectorIdentity().getDetection().setCriteria(Collections.emptyList());

		detectionStrategy.verifySshCriteria(connector.getConnectorIdentity().getDetection().getCriteria());

		assertFalse(telemetryManager.getHostProperties().isOsCommandExecutesLocally());
		assertFalse(telemetryManager.getHostProperties().isOsCommandExecutesRemotely());
	}

	@Test
	void testVerifySource() {
		// Call getDetectionStrategy to retrieve a ready-to-use detection strategy
		DetectionStrategy detectionStrategy = getDetectionStrategy();
		TelemetryManager telemetryManager = detectionStrategy.getTelemetryManager();
		Connector connector = telemetryManager.getConnectorStore().getStore().get("connector");

		// check that "IsMustCheckSshStatus" is false when there are no OsCommandSources
		detectionStrategy.verifySshSources(connector.getSourceTypes());
		assertFalse(telemetryManager.getHostProperties().isMustCheckSshStatus());

		// check that "IsMustCheckSshStatus" is true when there are OsCommandSources
		connector.setSourceTypes(Set.of(OsCommandSource.class));
		detectionStrategy.verifySshSources(connector.getSourceTypes());
		assertTrue(telemetryManager.getHostProperties().isMustCheckSshStatus());
	}
}
