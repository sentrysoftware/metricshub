package com.sentrysoftware.metricshub.engine.strategy.discovery;

import static com.sentrysoftware.metricshub.engine.common.helpers.MetricsHubConstants.IS_ENDPOINT;
import static com.sentrysoftware.metricshub.engine.constants.Constants.AAC_CONNECTOR_ID;
import static com.sentrysoftware.metricshub.engine.constants.Constants.CONNECTOR;
import static com.sentrysoftware.metricshub.engine.constants.Constants.DISK_CONTROLLER;
import static com.sentrysoftware.metricshub.engine.constants.Constants.HOST;
import static com.sentrysoftware.metricshub.engine.constants.Constants.HOST_ID;
import static com.sentrysoftware.metricshub.engine.constants.Constants.HOST_NAME;
import static com.sentrysoftware.metricshub.engine.constants.Constants.ID;
import static com.sentrysoftware.metricshub.engine.constants.Constants.LOGICAL_DISK;
import static com.sentrysoftware.metricshub.engine.constants.Constants.MONITOR_ID_ATTRIBUTE_VALUE;
import static com.sentrysoftware.metricshub.engine.constants.Constants.PHYSICAL_DISK;
import static com.sentrysoftware.metricshub.engine.constants.Constants.YAML_TEST_PATH;
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
import com.sentrysoftware.metricshub.engine.strategy.source.SourceTable;
import com.sentrysoftware.metricshub.engine.telemetry.Monitor;
import com.sentrysoftware.metricshub.engine.telemetry.TelemetryManager;
import com.sentrysoftware.metricshub.engine.telemetry.metric.NumberMetric;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
class DiscoveryStrategyTest {

	@Mock
	private MatsyaClientsExecutor matsyaClientsExecutorMock;

	static Long strategyTime = new Date().getTime();

	private DiscoveryStrategy discoveryStrategy;

	@Test
	void testRun() throws Exception {
		// Create host and connector monitors and set them in the telemetry manager
		final Monitor hostMonitor = Monitor.builder().type(KnownMonitorType.HOST.getKey()).build();
		final Monitor connectorMonitor = Monitor.builder().type(KnownMonitorType.CONNECTOR.getKey()).build();
		final Map<String, Map<String, Monitor>> monitors = new HashMap<>(
			Map.of(
				HOST,
				Map.of(MONITOR_ID_ATTRIBUTE_VALUE, hostMonitor),
				CONNECTOR,
				Map.of(AAC_CONNECTOR_ID, connectorMonitor)
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

		hostMonitor.getAttributes().put(IS_ENDPOINT, "true");

		connectorMonitor.getAttributes().put(ID, AAC_CONNECTOR_ID);

		// Create the connector store
		final ConnectorStore connectorStore = new ConnectorStore(YAML_TEST_PATH);
		telemetryManager.setConnectorStore(connectorStore);

		discoveryStrategy =
			DiscoveryStrategy
				.builder()
				.matsyaClientsExecutor(matsyaClientsExecutorMock)
				.strategyTime(strategyTime)
				.telemetryManager(telemetryManager)
				.build();

		// Mock detection criteria result
		doReturn("result")
			.when(matsyaClientsExecutorMock)
			.executeSNMPGet(eq("1.3.6.1.4.1.795.10.1.1.3.1.1"), any(SnmpConfiguration.class), anyString(), anyBoolean());

		// Mock source table information for disk controller
		doReturn(SourceTable.csvToTable("controller-1;1;Adaptec1;bios53v2;firmware32", MetricsHubConstants.TABLE_SEP))
			.when(matsyaClientsExecutorMock)
			.executeSNMPTable(
				eq("1.3.6.1.4.1.795.10.1.1.3.1"),
				any(String[].class),
				any(SnmpConfiguration.class),
				anyString(),
				eq(true)
			);

		// Mock source table information for physical_disk
		doReturn(SourceTable.csvToTable("disk-1;1;0;vendor-1;5;500000;512", MetricsHubConstants.TABLE_SEP))
			.when(matsyaClientsExecutorMock)
			.executeSNMPTable(
				eq("1.3.6.1.4.1.795.10.1.1.5.1"),
				any(String[].class),
				any(SnmpConfiguration.class),
				anyString(),
				eq(true)
			);

		// Mock source table information for logical_disk
		doReturn(SourceTable.csvToTable("logical-disk-1;1;500;RAID-5", MetricsHubConstants.TABLE_SEP))
			.when(matsyaClientsExecutorMock)
			.executeSNMPTable(
				eq("1.3.6.1.4.1.795.10.1.1.4.1"),
				any(String[].class),
				any(SnmpConfiguration.class),
				anyString(),
				eq(true)
			);

		// Call DiscoveryStrategy to discover the monitors
		discoveryStrategy.run();
		new PostDiscoveryStrategy(telemetryManager, strategyTime, matsyaClientsExecutorMock).run();

		// Check discovered monitors
		final Map<String, Map<String, Monitor>> discoveredMonitors = telemetryManager.getMonitors();

		assertEquals(5, discoveredMonitors.size());
		assertEquals(1, discoveredMonitors.get(HOST).size());
		assertEquals(1, discoveredMonitors.get(CONNECTOR).size());
		assertEquals(1, discoveredMonitors.get(DISK_CONTROLLER).size());
		assertEquals(1, discoveredMonitors.get(PHYSICAL_DISK).size());
		assertEquals(1, discoveredMonitors.get(LOGICAL_DISK).size());

		// Check discovered monitors order
		final Set<String> expectedOrder = Set.of(HOST, DISK_CONTROLLER, CONNECTOR, LOGICAL_DISK, PHYSICAL_DISK);
		assertEquals(expectedOrder, discoveredMonitors.keySet());

		final Monitor diskControllerMonitor = discoveredMonitors
			.get(DISK_CONTROLLER)
			.get("AAC_disk_controller_controller-1");
		final Monitor logicalDiskCMonitor = discoveredMonitors.get(LOGICAL_DISK).get("AAC_logical_disk_logical-disk-1");
		final Monitor physicalDiskMonitor = discoveredMonitors.get(PHYSICAL_DISK).get("AAC_physical_disk_disk-1");

		// Check that the monitors' present status are set to 1
		assertEquals(
			1.0,
			diskControllerMonitor
				.getMetric("hw.status{hw.type=\"disk_controller\", state=\"present\"}", NumberMetric.class)
				.getValue()
		);
		assertEquals(
			1.0,
			logicalDiskCMonitor
				.getMetric("hw.status{hw.type=\"logical_disk\", state=\"present\"}", NumberMetric.class)
				.getValue()
		);
		assertEquals(
			1.0,
			physicalDiskMonitor
				.getMetric("hw.status{hw.type=\"physical_disk\", state=\"present\"}", NumberMetric.class)
				.getValue()
		);

		final long nextDiscoveryTime = strategyTime + 60 * 60 * 1000;
		discoveryStrategy.setStrategyTime(nextDiscoveryTime);

		// Mock source table with no information for disk controller
		doReturn(SourceTable.csvToTable("", MetricsHubConstants.TABLE_SEP))
			.when(matsyaClientsExecutorMock)
			.executeSNMPTable(
				eq("1.3.6.1.4.1.795.10.1.1.3.1"),
				any(String[].class),
				any(SnmpConfiguration.class),
				anyString(),
				eq(true)
			);

		// Mock source table with no information for physical_disk
		doReturn(SourceTable.csvToTable("", MetricsHubConstants.TABLE_SEP))
			.when(matsyaClientsExecutorMock)
			.executeSNMPTable(
				eq("1.3.6.1.4.1.795.10.1.1.5.1"),
				any(String[].class),
				any(SnmpConfiguration.class),
				anyString(),
				eq(true)
			);

		// Mock source table with no information for logical_disk
		doReturn(SourceTable.csvToTable("", MetricsHubConstants.TABLE_SEP))
			.when(matsyaClientsExecutorMock)
			.executeSNMPTable(
				eq("1.3.6.1.4.1.795.10.1.1.4.1"),
				any(String[].class),
				any(SnmpConfiguration.class),
				anyString(),
				eq(true)
			);
		discoveryStrategy.run();
		new PostDiscoveryStrategy(telemetryManager, nextDiscoveryTime, matsyaClientsExecutorMock).run();

		// Check that the monitors are set to missing as they are not present in the previous discovery job
		assertEquals(
			0.0,
			diskControllerMonitor
				.getMetric("hw.status{hw.type=\"disk_controller\", state=\"present\"}", NumberMetric.class)
				.getValue()
		);
		assertEquals(
			0.0,
			logicalDiskCMonitor
				.getMetric("hw.status{hw.type=\"logical_disk\", state=\"present\"}", NumberMetric.class)
				.getValue()
		);
		assertEquals(
			0.0,
			physicalDiskMonitor
				.getMetric("hw.status{hw.type=\"physical_disk\", state=\"present\"}", NumberMetric.class)
				.getValue()
		);
	}
}
