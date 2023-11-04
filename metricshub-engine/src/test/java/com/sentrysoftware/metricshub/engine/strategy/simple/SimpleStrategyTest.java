package com.sentrysoftware.metricshub.engine.strategy.simple;

import static com.sentrysoftware.metricshub.engine.common.helpers.MetricsHubConstants.IS_ENDPOINT;
import static com.sentrysoftware.metricshub.engine.constants.Constants.CONNECTOR;
import static com.sentrysoftware.metricshub.engine.constants.Constants.DISK_CONTROLLER;
import static com.sentrysoftware.metricshub.engine.constants.Constants.DISK_CONTROLLER_PRESENT_METRIC;
import static com.sentrysoftware.metricshub.engine.constants.Constants.ENCLOSURE;
import static com.sentrysoftware.metricshub.engine.constants.Constants.ENCLOSURE_PRESENT_METRIC;
import static com.sentrysoftware.metricshub.engine.constants.Constants.HOST;
import static com.sentrysoftware.metricshub.engine.constants.Constants.HOST_ID;
import static com.sentrysoftware.metricshub.engine.constants.Constants.HOST_NAME;
import static com.sentrysoftware.metricshub.engine.constants.Constants.ID;
import static com.sentrysoftware.metricshub.engine.constants.Constants.MONITOR_ID_ATTRIBUTE_VALUE;
import static com.sentrysoftware.metricshub.engine.constants.Constants.TEST_CONNECTOR_WITH_SIMPLE_ID;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyBoolean;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.doReturn;

import com.sentrysoftware.metricshub.engine.common.helpers.KnownMonitorType;
import com.sentrysoftware.metricshub.engine.common.helpers.MetricsHubConstants;
import com.sentrysoftware.metricshub.engine.configuration.HostConfiguration;
import com.sentrysoftware.metricshub.engine.configuration.SnmpConfiguration;
import com.sentrysoftware.metricshub.engine.connector.model.ConnectorStore;
import com.sentrysoftware.metricshub.engine.matsya.MatsyaClientsExecutor;
import com.sentrysoftware.metricshub.engine.strategy.discovery.PostDiscoveryStrategy;
import com.sentrysoftware.metricshub.engine.strategy.source.SourceTable;
import com.sentrysoftware.metricshub.engine.telemetry.Monitor;
import com.sentrysoftware.metricshub.engine.telemetry.TelemetryManager;
import com.sentrysoftware.metricshub.engine.telemetry.metric.NumberMetric;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
class SimpleStrategyTest {

	private static final Path TEST_CONNECTOR_PATH = Paths.get(
		"src",
		"test",
		"resources",
		"test-files",
		"strategy",
		"TestConnectorWithSimple.yaml"
	);

	@Mock
	private MatsyaClientsExecutor matsyaClientsExecutorMock;

	static Long strategyTime = new Date().getTime();

	private SimpleStrategy simpleStrategy;

	@Test
	void testRun() throws Exception {
		// Create host and connector monitors and set them in the telemetry manager
		final Monitor hostMonitor = Monitor.builder().type(KnownMonitorType.HOST.getKey()).build();
		hostMonitor.getAttributes().put(IS_ENDPOINT, "true");

		final Monitor connectorMonitor = Monitor.builder().type(KnownMonitorType.CONNECTOR.getKey()).build();
		final Map<String, Map<String, Monitor>> monitors = new HashMap<>(
			Map.of(
				HOST,
				Map.of(MONITOR_ID_ATTRIBUTE_VALUE, hostMonitor),
				CONNECTOR,
				Map.of(TEST_CONNECTOR_WITH_SIMPLE_ID, connectorMonitor)
			)
		);

		final SnmpConfiguration snmpConfig = SnmpConfiguration.builder().community("public").build();

		final TelemetryManager telemetryManager = TelemetryManager
			.builder()
			.monitors(monitors)
			.hostConfiguration(
				HostConfiguration
					.builder()
					.hostId(HOST_ID)
					.hostname(HOST_NAME)
					.sequential(false)
					.configurations(Map.of(SnmpConfiguration.class, snmpConfig))
					.build()
			)
			.build();

		connectorMonitor.getAttributes().put(ID, TEST_CONNECTOR_WITH_SIMPLE_ID);

		// Create the connector store
		final ConnectorStore connectorStore = new ConnectorStore(TEST_CONNECTOR_PATH);
		telemetryManager.setConnectorStore(connectorStore);

		// Set simple strategy information
		simpleStrategy =
			SimpleStrategy
				.builder()
				.matsyaClientsExecutor(matsyaClientsExecutorMock)
				.strategyTime(strategyTime)
				.telemetryManager(telemetryManager)
				.build();

		// Mock detection criteria result
		doReturn("result")
			.when(matsyaClientsExecutorMock)
			.executeSNMPGet(eq("1.3.6.1.4.1.795.10.1.1.3.1.1"), any(SnmpConfiguration.class), anyString(), anyBoolean());

		// Mock source table information for enclosure
		doReturn(SourceTable.csvToTable("enclosure-1;1;healthy", MetricsHubConstants.TABLE_SEP))
			.when(matsyaClientsExecutorMock)
			.executeSNMPTable(
				eq("1.3.6.1.4.1.795.10.1.1.3.1"),
				any(String[].class),
				any(SnmpConfiguration.class),
				anyString(),
				eq(true)
			);

		// Mock source table information for disk_controller
		doReturn(SourceTable.csvToTable("1;1;healthy", MetricsHubConstants.TABLE_SEP))
			.when(matsyaClientsExecutorMock)
			.executeSNMPTable(
				eq("1.3.6.1.4.1.795.10.1.1.4.1"),
				any(String[].class),
				any(SnmpConfiguration.class),
				anyString(),
				eq(true)
			);
		simpleStrategy.run();
		new PostDiscoveryStrategy(telemetryManager, strategyTime, matsyaClientsExecutorMock).run();

		// Check processed monitors
		final Map<String, Map<String, Monitor>> processedMonitors = telemetryManager.getMonitors();

		final Map<String, Monitor> enclosureMonitors = processedMonitors.get(ENCLOSURE);
		final Map<String, Monitor> diskControllerMonitors = processedMonitors.get(DISK_CONTROLLER);

		assertEquals(4, processedMonitors.size());
		assertEquals(1, enclosureMonitors.size());
		assertEquals(1, diskControllerMonitors.size());

		// Check processed monitors metrics
		final Monitor enclosure = enclosureMonitors.get("TestConnectorWithSimple_enclosure_enclosure-1");
		final Monitor diskController = diskControllerMonitors.get("TestConnectorWithSimple_disk_controller_1");

		assertEquals(1.0, enclosure.getMetric("hw.status{hw.type=\"enclosure\"}", NumberMetric.class).getValue());
		assertEquals(
			1.0,
			diskController.getMetric("hw.status{hw.type=\"disk_controller\"}", NumberMetric.class).getValue()
		);

		// Check that the monitors' present status are set to 1
		assertEquals(1.0, enclosure.getMetric(ENCLOSURE_PRESENT_METRIC, NumberMetric.class).getValue());
		assertEquals(1.0, diskController.getMetric(DISK_CONTROLLER_PRESENT_METRIC, NumberMetric.class).getValue());

		final long nextDiscoveryTime = strategyTime + 2 * 60 * 1000;
		simpleStrategy.setStrategyTime(nextDiscoveryTime);

		// Mock source table with no information for enclosure
		doReturn(SourceTable.csvToTable("", MetricsHubConstants.TABLE_SEP))
			.when(matsyaClientsExecutorMock)
			.executeSNMPTable(
				eq("1.3.6.1.4.1.795.10.1.1.3.1"),
				any(String[].class),
				any(SnmpConfiguration.class),
				anyString(),
				eq(true)
			);

		// Mock source table with no information for disk_controller
		doReturn(SourceTable.csvToTable("", MetricsHubConstants.TABLE_SEP))
			.when(matsyaClientsExecutorMock)
			.executeSNMPTable(
				eq("1.3.6.1.4.1.795.10.1.1.4.1"),
				any(String[].class),
				any(SnmpConfiguration.class),
				anyString(),
				eq(true)
			);
		simpleStrategy.run();
		new PostDiscoveryStrategy(telemetryManager, nextDiscoveryTime, matsyaClientsExecutorMock).run();

		// Check that the monitors are set to missing when they are not present in the previous simple job
		assertEquals(0.0, enclosure.getMetric(ENCLOSURE_PRESENT_METRIC, NumberMetric.class).getValue());
		assertEquals(0.0, diskController.getMetric(DISK_CONTROLLER_PRESENT_METRIC, NumberMetric.class).getValue());
	}
}
