package com.sentrysoftware.matrix.strategy.collect;

import static com.sentrysoftware.matrix.common.helpers.MatrixConstants.IS_ENDPOINT;
import static com.sentrysoftware.matrix.common.helpers.MatrixConstants.MONITOR_ATTRIBUTE_ID;
import static com.sentrysoftware.matrix.constants.Constants.CONNECTOR;
import static com.sentrysoftware.matrix.constants.Constants.HOST;
import static com.sentrysoftware.matrix.constants.Constants.HOST_ID;
import static com.sentrysoftware.matrix.constants.Constants.HOST_NAME;
import static com.sentrysoftware.matrix.constants.Constants.ID;
import static com.sentrysoftware.matrix.constants.Constants.MONITOR_ID_ATTRIBUTE_VALUE;
import static com.sentrysoftware.matrix.constants.Constants.TEST_CONNECTOR_FILE_NAME;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.doReturn;

import com.sentrysoftware.matrix.common.helpers.KnownMonitorType;
import com.sentrysoftware.matrix.common.helpers.MatrixConstants;
import com.sentrysoftware.matrix.configuration.HostConfiguration;
import com.sentrysoftware.matrix.configuration.SnmpConfiguration;
import com.sentrysoftware.matrix.connector.model.ConnectorStore;
import com.sentrysoftware.matrix.matsya.MatsyaClientsExecutor;
import com.sentrysoftware.matrix.strategy.source.SourceTable;
import com.sentrysoftware.matrix.telemetry.Monitor;
import com.sentrysoftware.matrix.telemetry.MonitorFactory;
import com.sentrysoftware.matrix.telemetry.TelemetryManager;
import com.sentrysoftware.matrix.telemetry.metric.NumberMetric;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
class CollectStrategyTest {

	private static final Path TEST_CONNECTOR_PATH = Paths.get(
		"src",
		"test",
		"resources",
		"test-files",
		"strategy",
		"TestConnector.yaml"
	);
	private static final String HEALTHY = "healthy";
	private static final String STATUS_INFORMATION = "StatusInformation";

	@Mock
	private MatsyaClientsExecutor matsyaClientsExecutorMock;

	@InjectMocks
	private CollectStrategy collectStrategy;

	static Long strategyTime = new Date().getTime();

	@Test
	void testRun() throws Exception {
		final String connectorId = TEST_CONNECTOR_FILE_NAME.split("\\.")[0];

		// Create host and connector monitors and set them in the telemetry manager
		final Monitor hostMonitor = Monitor.builder().type(KnownMonitorType.HOST.getKey()).build();
		final Monitor connectorMonitor = Monitor.builder().type(KnownMonitorType.CONNECTOR.getKey()).build();
		final Map<String, Map<String, Monitor>> monitors = new HashMap<>(
			Map.of(HOST, Map.of(MONITOR_ID_ATTRIBUTE_VALUE, hostMonitor), CONNECTOR, Map.of(connectorId, connectorMonitor))
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

		MonitorFactory monitorFactory = MonitorFactory
			.builder()
			.monitorType("enclosure")
			.telemetryManager(telemetryManager)
			.connectorId(connectorId)
			.attributes(new HashMap<>(Map.of(MONITOR_ATTRIBUTE_ID, "enclosure-1")))
			.discoveryTime(strategyTime - 30 * 60 * 1000)
			.build();
		final Monitor enclosure = monitorFactory.createOrUpdateMonitor();

		monitorFactory =
			MonitorFactory
				.builder()
				.monitorType("disk_controller")
				.telemetryManager(telemetryManager)
				.connectorId(connectorId)
				.attributes(new HashMap<>(Map.of(MONITOR_ATTRIBUTE_ID, "1")))
				.discoveryTime(strategyTime - 30 * 60 * 1000)
				.build();
		final Monitor diskController = monitorFactory.createOrUpdateMonitor();

		hostMonitor.addAttribute(IS_ENDPOINT, "true");

		connectorMonitor.addAttribute(ID, TEST_CONNECTOR_FILE_NAME);

		// Create the connector store
		final ConnectorStore connectorStore = new ConnectorStore(TEST_CONNECTOR_PATH);
		telemetryManager.setConnectorStore(connectorStore);

		collectStrategy.setTelemetryManager(telemetryManager);
		collectStrategy.setStrategyTime(strategyTime);

		// Mock source table information for enclosure
		doReturn(SourceTable.csvToTable("enclosure-1;1;healthy", MatrixConstants.TABLE_SEP))
			.when(matsyaClientsExecutorMock)
			.executeSNMPTable(
				eq("1.3.6.1.4.1.795.10.1.1.30.1"),
				any(String[].class),
				any(SnmpConfiguration.class),
				anyString(),
				eq(true)
			);

		// Mock source table information for disk_controller
		doReturn(SourceTable.csvToTable("1;1;healthy", MatrixConstants.TABLE_SEP))
			.when(matsyaClientsExecutorMock)
			.executeSNMPTable(
				eq("1.3.6.1.4.1.795.10.1.1.31.1"),
				any(String[].class),
				any(SnmpConfiguration.class),
				anyString(),
				eq(true)
			);

		collectStrategy.run();

		// Check metrics
		assertEquals(
			1.0,
			diskController.getMetric("hw.status{hw.type=\"disk_controller\"}", NumberMetric.class).getValue()
		);
		assertEquals(HEALTHY, diskController.getLegacyTextParameters().get(STATUS_INFORMATION));
		assertEquals(1.0, enclosure.getMetric("hw.status{hw.type=\"enclosure\"}", NumberMetric.class).getValue());
		assertEquals(HEALTHY, enclosure.getLegacyTextParameters().get(STATUS_INFORMATION));
	}

	@Test
	void testPost() throws Exception {
		final String connectorId = TEST_CONNECTOR_FILE_NAME.split("\\.")[0];

		// Create host and connector monitors and set them in the telemetry manager
		final Monitor hostMonitor = Monitor.builder().type(KnownMonitorType.HOST.getKey()).build();
		final Monitor connectorMonitor = Monitor.builder().type(KnownMonitorType.CONNECTOR.getKey()).build();
		final Map<String, Map<String, Monitor>> monitors = new HashMap<>(
			Map.of(HOST, Map.of(MONITOR_ID_ATTRIBUTE_VALUE, hostMonitor), CONNECTOR, Map.of(connectorId, connectorMonitor))
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

		final long discoveryTime = strategyTime - 60 * 60 * 1000;

		MonitorFactory monitorFactory = MonitorFactory
			.builder()
			.monitorType("enclosure")
			.telemetryManager(telemetryManager)
			.connectorId(connectorId)
			.attributes(new HashMap<>(Map.of(MONITOR_ATTRIBUTE_ID, "enclosure-1")))
			.discoveryTime(discoveryTime)
			.build();
		final Monitor enclosure = monitorFactory.createOrUpdateMonitor();

		monitorFactory =
			MonitorFactory
				.builder()
				.monitorType("disk_controller")
				.telemetryManager(telemetryManager)
				.connectorId(connectorId)
				.attributes(new HashMap<>(Map.of(MONITOR_ATTRIBUTE_ID, "1")))
				.discoveryTime(discoveryTime)
				.build();
		final Monitor diskController = monitorFactory.createOrUpdateMonitor();

		hostMonitor.addAttribute(IS_ENDPOINT, "true");

		connectorMonitor.addAttribute(ID, TEST_CONNECTOR_FILE_NAME);

		// Create the connector store
		final ConnectorStore connectorStore = new ConnectorStore(TEST_CONNECTOR_PATH);
		telemetryManager.setConnectorStore(connectorStore);

		collectStrategy.setTelemetryManager(telemetryManager);
		collectStrategy.setStrategyTime(strategyTime);

		// Mock source table information for enclosure
		doReturn(SourceTable.csvToTable("enclosure-1;1;healthy", MatrixConstants.TABLE_SEP))
			.when(matsyaClientsExecutorMock)
			.executeSNMPTable(
				eq("1.3.6.1.4.1.795.10.1.1.30.1"),
				any(String[].class),
				any(SnmpConfiguration.class),
				anyString(),
				eq(true)
			);

		// Mock source table information for disk_controller
		doReturn(SourceTable.csvToTable("1;1;healthy", MatrixConstants.TABLE_SEP))
			.when(matsyaClientsExecutorMock)
			.executeSNMPTable(
				eq("1.3.6.1.4.1.795.10.1.1.31.1"),
				any(String[].class),
				any(SnmpConfiguration.class),
				anyString(),
				eq(true)
			);

		collectStrategy.run();

		// Check metrics
		assertEquals(
			1.0,
			diskController.getMetric("hw.status{hw.type=\"disk_controller\"}", NumberMetric.class).getValue()
		);
		assertEquals(HEALTHY, diskController.getLegacyTextParameters().get(STATUS_INFORMATION));
		assertEquals(1.0, enclosure.getMetric("hw.status{hw.type=\"enclosure\"}", NumberMetric.class).getValue());
		assertEquals(HEALTHY, enclosure.getLegacyTextParameters().get(STATUS_INFORMATION));

		// The metric is created when we create the monitor using the MonitorFactory
		final NumberMetric enclosurePresentMetric = enclosure.getMetric(
			"hw.status{hw.type=\"enclosure\", state=\"present\"}",
			NumberMetric.class
		);
		assertNotNull(enclosurePresentMetric);
		assertEquals(discoveryTime, enclosurePresentMetric.getCollectTime());

		collectStrategy.post();

		assertNotNull(enclosurePresentMetric);
		assertEquals(
			1.0,
			enclosure.getMetric("hw.status{hw.type=\"enclosure\", state=\"present\"}", NumberMetric.class).getValue()
		);
		assertEquals(strategyTime, enclosurePresentMetric.getCollectTime());
	}
}
