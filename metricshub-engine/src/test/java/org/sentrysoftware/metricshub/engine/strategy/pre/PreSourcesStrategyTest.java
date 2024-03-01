package org.sentrysoftware.metricshub.engine.strategy.pre;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyBoolean;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.doReturn;
import static org.sentrysoftware.metricshub.engine.common.helpers.MetricsHubConstants.IS_ENDPOINT;
import static org.sentrysoftware.metricshub.engine.common.helpers.MetricsHubConstants.MONITOR_ATTRIBUTE_ID;
import static org.sentrysoftware.metricshub.engine.constants.Constants.CONNECTOR;
import static org.sentrysoftware.metricshub.engine.constants.Constants.DISK_CONTROLLER;
import static org.sentrysoftware.metricshub.engine.constants.Constants.ENCLOSURE;
import static org.sentrysoftware.metricshub.engine.constants.Constants.HEALTHY;
import static org.sentrysoftware.metricshub.engine.constants.Constants.HOST;
import static org.sentrysoftware.metricshub.engine.constants.Constants.HOST_ID;
import static org.sentrysoftware.metricshub.engine.constants.Constants.HOST_NAME;
import static org.sentrysoftware.metricshub.engine.constants.Constants.ID;
import static org.sentrysoftware.metricshub.engine.constants.Constants.MONITOR_ID_ATTRIBUTE_VALUE;
import static org.sentrysoftware.metricshub.engine.constants.Constants.STATUS_INFORMATION;
import static org.sentrysoftware.metricshub.engine.strategy.AbstractStrategy.CONNECTOR_ID_FORMAT;

import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.sentrysoftware.metricshub.engine.client.ClientsExecutor;
import org.sentrysoftware.metricshub.engine.common.helpers.KnownMonitorType;
import org.sentrysoftware.metricshub.engine.common.helpers.MetricsHubConstants;
import org.sentrysoftware.metricshub.engine.configuration.HostConfiguration;
import org.sentrysoftware.metricshub.engine.configuration.SnmpConfiguration;
import org.sentrysoftware.metricshub.engine.connector.model.ConnectorStore;
import org.sentrysoftware.metricshub.engine.connector.model.common.DeviceKind;
import org.sentrysoftware.metricshub.engine.strategy.IStrategy;
import org.sentrysoftware.metricshub.engine.strategy.collect.CollectStrategy;
import org.sentrysoftware.metricshub.engine.strategy.discovery.DiscoveryStrategy;
import org.sentrysoftware.metricshub.engine.strategy.source.SourceTable;
import org.sentrysoftware.metricshub.engine.telemetry.ConnectorNamespace;
import org.sentrysoftware.metricshub.engine.telemetry.Monitor;
import org.sentrysoftware.metricshub.engine.telemetry.MonitorFactory;
import org.sentrysoftware.metricshub.engine.telemetry.TelemetryManager;
import org.sentrysoftware.metricshub.engine.telemetry.metric.NumberMetric;

@ExtendWith(MockitoExtension.class)
class PreSourcesStrategyTest {

	// Connector path
	public static final Path TEST_CONNECTOR_PATH = Paths.get("src", "test", "resources", "test-files", "strategy", "pre");

	@Mock
	private ClientsExecutor clientsExecutorMock;

	private IStrategy collectStrategy;

	private IStrategy discoveryStrategy;

	static Long strategyTime = new Date().getTime();

	TelemetryManager telemetryManager;
	Monitor enclosure;
	Monitor diskController;

	void initTest() {
		// Create host and connector monitors and set them in the telemetry manager
		final Monitor hostMonitor = Monitor.builder().type(KnownMonitorType.HOST.getKey()).build();
		final Monitor connectorMonitor = Monitor.builder().type(KnownMonitorType.CONNECTOR.getKey()).build();
		final Map<String, Map<String, Monitor>> monitors = new HashMap<>(
			Map.of(
				HOST,
				Map.of(MONITOR_ID_ATTRIBUTE_VALUE, hostMonitor),
				CONNECTOR,
				Map.of(String.format(CONNECTOR_ID_FORMAT, KnownMonitorType.CONNECTOR.getKey(), "preSource"), connectorMonitor)
			)
		);

		final SnmpConfiguration snmpConfig = SnmpConfiguration.builder().community("public").build();

		telemetryManager =
			TelemetryManager
				.builder()
				.monitors(monitors)
				.hostConfiguration(
					HostConfiguration
						.builder()
						.hostId(HOST_ID)
						.hostname(HOST_NAME)
						.hostType(DeviceKind.LINUX)
						.sequential(false)
						.configurations(Map.of(SnmpConfiguration.class, snmpConfig))
						.build()
				)
				.build();

		MonitorFactory monitorFactory = MonitorFactory
			.builder()
			.monitorType(ENCLOSURE)
			.telemetryManager(telemetryManager)
			.connectorId("preSource")
			.attributes(new HashMap<>(Map.of(MONITOR_ATTRIBUTE_ID, "enclosure-1")))
			.discoveryTime(strategyTime - 30 * 60 * 1000)
			.build();
		enclosure = monitorFactory.createOrUpdateMonitor();

		monitorFactory =
			MonitorFactory
				.builder()
				.monitorType(DISK_CONTROLLER)
				.telemetryManager(telemetryManager)
				.connectorId("preSource")
				.attributes(new HashMap<>(Map.of(MONITOR_ATTRIBUTE_ID, "1")))
				.discoveryTime(strategyTime - 30 * 60 * 1000)
				.build();
		diskController = monitorFactory.createOrUpdateMonitor();

		hostMonitor.addAttribute(IS_ENDPOINT, "true");

		connectorMonitor.addAttribute(ID, "preSource");

		// Create the connector store
		final ConnectorStore connectorStore = new ConnectorStore(TEST_CONNECTOR_PATH);
		telemetryManager.setConnectorStore(connectorStore);
	}

	@Test
	void testRunFromCollect() throws Exception {
		initTest();

		collectStrategy =
			CollectStrategy
				.builder()
				.clientsExecutor(clientsExecutorMock)
				.strategyTime(strategyTime)
				.telemetryManager(telemetryManager)
				.build();

		// Mock detection criteria result
		doReturn("1.3.6.1.4.1.795.10.1.1.3.1.1.0	ASN_OCTET_STR	Test")
			.when(clientsExecutorMock)
			.executeSNMPGetNext(eq("1.3.6.1.4.1.795.10.1.1.3.1.1"), any(SnmpConfiguration.class), anyString(), anyBoolean());

		// Mock source table information for enclosure
		doReturn(SourceTable.csvToTable("enclosure-1;1;healthy", MetricsHubConstants.TABLE_SEP))
			.when(clientsExecutorMock)
			.executeSNMPTable(
				eq("1.3.6.1.4.1.795.10.1.1.30.1"),
				any(String[].class),
				any(SnmpConfiguration.class),
				anyString(),
				eq(true)
			);

		// Mock source table information for disk_controller
		doReturn(SourceTable.csvToTable("1;1;healthy", MetricsHubConstants.TABLE_SEP))
			.when(clientsExecutorMock)
			.executeSNMPTable(
				eq("1.3.6.1.4.1.795.10.1.1.31.1"),
				any(String[].class),
				any(SnmpConfiguration.class),
				anyString(),
				eq(true)
			);

		// Mock source table information for the snmp pre source
		doReturn(SourceTable.csvToTable("preSource-1;1;2;3;4;5;6;7;healthy;health-ok", MetricsHubConstants.TABLE_SEP))
			.when(clientsExecutorMock)
			.executeSNMPTable(
				eq("1.3.6.1.4.1.795.10.1.1.4.5"),
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

		// Check the successful execution of PreSourceStrategy
		final ConnectorNamespace connectorNamespace = telemetryManager
			.getHostProperties()
			.getConnectorNamespace("preSource");
		assertNotNull(connectorNamespace);
		final SourceTable sourceTable = connectorNamespace.getSourceTables().get("${source::pre.snmpSource}");
		assertNotNull(sourceTable);
		assertEquals("preSource-1", sourceTable.getTable().get(0).get(0));
		assertEquals("1", sourceTable.getTable().get(0).get(1));
		assertEquals("2", sourceTable.getTable().get(0).get(2));
		assertEquals("3", sourceTable.getTable().get(0).get(3));
		assertEquals("4", sourceTable.getTable().get(0).get(4));
		assertEquals("5", sourceTable.getTable().get(0).get(5));
		assertEquals("6", sourceTable.getTable().get(0).get(6));
		assertEquals("7", sourceTable.getTable().get(0).get(7));
		assertEquals("healthy", sourceTable.getTable().get(0).get(8));
		assertEquals("health-ok", sourceTable.getTable().get(0).get(9));
	}

	@Test
	void testRunFromDiscovery() throws Exception {
		initTest();

		discoveryStrategy =
			DiscoveryStrategy
				.builder()
				.clientsExecutor(clientsExecutorMock)
				.strategyTime(strategyTime)
				.telemetryManager(telemetryManager)
				.build();

		// Mock detection criteria result
		doReturn("1.3.6.1.4.1.795.10.1.1.3.1.1.0	ASN_OCTET_STR	Test")
			.when(clientsExecutorMock)
			.executeSNMPGetNext(eq("1.3.6.1.4.1.795.10.1.1.3.1.1"), any(SnmpConfiguration.class), anyString(), anyBoolean());

		// Mock source table information for enclosure
		doReturn(SourceTable.csvToTable("enclosure-1;1;2;3;4;5;6;7;healthy;health-ok", MetricsHubConstants.TABLE_SEP))
			.when(clientsExecutorMock)
			.executeSNMPTable(
				eq("1.3.6.1.4.1.795.10.1.1.3.1"),
				any(String[].class),
				any(SnmpConfiguration.class),
				anyString(),
				eq(true)
			);

		// Mock source table information for disk_controller
		doReturn(SourceTable.csvToTable("diskController-1;1;2;3;4;5;6;7;healthy;health-ok", MetricsHubConstants.TABLE_SEP))
			.when(clientsExecutorMock)
			.executeSNMPTable(
				eq("1.3.6.1.4.1.795.10.1.1.4.1"),
				any(String[].class),
				any(SnmpConfiguration.class),
				anyString(),
				eq(true)
			);

		// Mock source table information for the snmp pre source
		doReturn(SourceTable.csvToTable("preSource-1;1;2;3;4;5;6;7;healthy;health-ok", MetricsHubConstants.TABLE_SEP))
			.when(clientsExecutorMock)
			.executeSNMPTable(
				eq("1.3.6.1.4.1.795.10.1.1.4.5"),
				any(String[].class),
				any(SnmpConfiguration.class),
				anyString(),
				eq(true)
			);

		discoveryStrategy.run();

		// Check the successful execution of PreSourceStrategy
		final ConnectorNamespace connectorNamespace = telemetryManager
			.getHostProperties()
			.getConnectorNamespace("preSource");
		assertNotNull(connectorNamespace);
		final SourceTable sourceTable = connectorNamespace.getSourceTables().get("${source::pre.snmpSource}");
		assertNotNull(sourceTable);
		assertEquals("preSource-1", sourceTable.getTable().get(0).get(0));
		assertEquals("1", sourceTable.getTable().get(0).get(1));
		assertEquals("2", sourceTable.getTable().get(0).get(2));
		assertEquals("3", sourceTable.getTable().get(0).get(3));
		assertEquals("4", sourceTable.getTable().get(0).get(4));
		assertEquals("5", sourceTable.getTable().get(0).get(5));
		assertEquals("6", sourceTable.getTable().get(0).get(6));
		assertEquals("7", sourceTable.getTable().get(0).get(7));
		assertEquals("healthy", sourceTable.getTable().get(0).get(8));
		assertEquals("health-ok", sourceTable.getTable().get(0).get(9));
	}
}
