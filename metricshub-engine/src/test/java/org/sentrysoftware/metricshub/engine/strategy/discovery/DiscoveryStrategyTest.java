package org.sentrysoftware.metricshub.engine.strategy.discovery;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyBoolean;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.doReturn;
import static org.sentrysoftware.metricshub.engine.common.helpers.KnownMonitorType.CONNECTOR;
import static org.sentrysoftware.metricshub.engine.common.helpers.KnownMonitorType.DISK_CONTROLLER;
import static org.sentrysoftware.metricshub.engine.common.helpers.KnownMonitorType.HOST;
import static org.sentrysoftware.metricshub.engine.common.helpers.KnownMonitorType.LOGICAL_DISK;
import static org.sentrysoftware.metricshub.engine.common.helpers.KnownMonitorType.PHYSICAL_DISK;
import static org.sentrysoftware.metricshub.engine.common.helpers.MetricsHubConstants.IS_ENDPOINT;
import static org.sentrysoftware.metricshub.engine.strategy.AbstractStrategy.CONNECTOR_ID_FORMAT;

import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.sentrysoftware.metricshub.engine.client.ClientsExecutor;
import org.sentrysoftware.metricshub.engine.common.helpers.MetricsHubConstants;
import org.sentrysoftware.metricshub.engine.configuration.HostConfiguration;
import org.sentrysoftware.metricshub.engine.configuration.SnmpConfiguration;
import org.sentrysoftware.metricshub.engine.connector.model.ConnectorStore;
import org.sentrysoftware.metricshub.engine.strategy.source.SourceTable;
import org.sentrysoftware.metricshub.engine.telemetry.Monitor;
import org.sentrysoftware.metricshub.engine.telemetry.TelemetryManager;

@ExtendWith(MockitoExtension.class)
class DiscoveryStrategyTest {

	private static final Path YAML_TEST_PATH = Paths.get(
		"src",
		"test",
		"resources",
		"test-files",
		"strategy",
		"discovery"
	);

	private static final String AAC_CONNECTOR_ID = "AAC";

	@Mock
	private ClientsExecutor clientsExecutorMock;

	static Long strategyTime = new Date().getTime();

	private DiscoveryStrategy discoveryStrategy;

	@Test
	void testRun() throws Exception {
		// Create host and connector monitors and set them in the telemetry manager
		final Monitor hostMonitor = Monitor.builder().type(HOST.getKey()).build();
		final Monitor connectorMonitor = Monitor.builder().type(CONNECTOR.getKey()).build();
		final Map<String, Map<String, Monitor>> monitors = new HashMap<>(
			Map.of(
				HOST.getKey(),
				Map.of("anyMonitorId", hostMonitor),
				CONNECTOR.getKey(),
				Map.of(String.format(CONNECTOR_ID_FORMAT, CONNECTOR.getKey(), AAC_CONNECTOR_ID), connectorMonitor)
			)
		);

		final SnmpConfiguration snmpConfig = SnmpConfiguration.builder().community("public").build();

		final TelemetryManager telemetryManager = TelemetryManager
			.builder()
			.monitors(monitors)
			.hostConfiguration(
				HostConfiguration
					.builder()
					.hostId("host01")
					.hostname("ec-01")
					.sequential(false)
					.configurations(Map.of(SnmpConfiguration.class, snmpConfig))
					.build()
			)
			.build();

		hostMonitor.getAttributes().put(IS_ENDPOINT, "true");
		hostMonitor.setIsEndpoint(true);

		connectorMonitor.getAttributes().put("id", AAC_CONNECTOR_ID);

		// Create the connector store
		final ConnectorStore connectorStore = new ConnectorStore(YAML_TEST_PATH);
		telemetryManager.setConnectorStore(connectorStore);

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

		// Mock source table information for disk controller
		doReturn(SourceTable.csvToTable("controller-1;1;Adaptec1;bios53v2;firmware32", MetricsHubConstants.TABLE_SEP))
			.when(clientsExecutorMock)
			.executeSNMPTable(
				eq("1.3.6.1.4.1.795.10.1.1.3.1"),
				any(String[].class),
				any(SnmpConfiguration.class),
				anyString(),
				eq(true)
			);

		// Mock source table information for physical_disk
		doReturn(SourceTable.csvToTable("disk-1;1;0;vendor-1;5;500000;512", MetricsHubConstants.TABLE_SEP))
			.when(clientsExecutorMock)
			.executeSNMPTable(
				eq("1.3.6.1.4.1.795.10.1.1.5.1"),
				any(String[].class),
				any(SnmpConfiguration.class),
				anyString(),
				eq(true)
			);

		// Mock source table information for logical_disk
		doReturn(SourceTable.csvToTable("logical-disk-1;1;500;RAID-5", MetricsHubConstants.TABLE_SEP))
			.when(clientsExecutorMock)
			.executeSNMPTable(
				eq("1.3.6.1.4.1.795.10.1.1.4.1"),
				any(String[].class),
				any(SnmpConfiguration.class),
				anyString(),
				eq(true)
			);

		// Call DiscoveryStrategy to discover the monitors
		discoveryStrategy.run();

		// Check discovered monitors
		final Map<String, Map<String, Monitor>> discoveredMonitors = telemetryManager.getMonitors();

		assertEquals(5, discoveredMonitors.size());
		assertEquals(1, discoveredMonitors.get(HOST.getKey()).size());
		assertEquals(1, discoveredMonitors.get(CONNECTOR.getKey()).size());
		assertEquals(1, discoveredMonitors.get(DISK_CONTROLLER.getKey()).size());
		assertEquals(1, discoveredMonitors.get(PHYSICAL_DISK.getKey()).size());
		assertEquals(1, discoveredMonitors.get(LOGICAL_DISK.getKey()).size());

		// Check discovered monitors order
		final Set<String> expectedOrder = Set.of(
			HOST.getKey(),
			DISK_CONTROLLER.getKey(),
			CONNECTOR.getKey(),
			LOGICAL_DISK.getKey(),
			PHYSICAL_DISK.getKey()
		);
		assertEquals(expectedOrder, discoveredMonitors.keySet());
	}
}
