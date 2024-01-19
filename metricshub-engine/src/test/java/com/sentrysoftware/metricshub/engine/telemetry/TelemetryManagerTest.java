package com.sentrysoftware.metricshub.engine.telemetry;

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
import static com.sentrysoftware.metricshub.engine.strategy.AbstractStrategy.CONNECTOR_ID_FORMAT;
import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyBoolean;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.spy;

import com.sentrysoftware.metricshub.engine.ClientsExecutor;
import com.sentrysoftware.metricshub.engine.common.helpers.KnownMonitorType;
import com.sentrysoftware.metricshub.engine.common.helpers.MetricsHubConstants;
import com.sentrysoftware.metricshub.engine.configuration.HostConfiguration;
import com.sentrysoftware.metricshub.engine.configuration.SnmpConfiguration;
import com.sentrysoftware.metricshub.engine.connector.model.ConnectorStore;
import com.sentrysoftware.metricshub.engine.strategy.discovery.DiscoveryStrategy;
import com.sentrysoftware.metricshub.engine.strategy.source.SourceTable;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;
import org.junit.jupiter.api.Test;

class TelemetryManagerTest {

	@Test
	void testRun() throws Exception {
		final ClientsExecutor clientsExecutorMock = spy(ClientsExecutor.class);

		// Create host and connector monitors and set them in the telemetry manager
		final Monitor hostMonitor = Monitor.builder().type(KnownMonitorType.HOST.getKey()).build();
		final Monitor connectorMonitor = Monitor.builder().type(KnownMonitorType.CONNECTOR.getKey()).build();
		final Map<String, Map<String, Monitor>> monitors = new HashMap<>(
			Map.of(
				HOST,
				Map.of(MONITOR_ID_ATTRIBUTE_VALUE, hostMonitor),
				CONNECTOR,
				Map.of(
					String.format(CONNECTOR_ID_FORMAT, KnownMonitorType.CONNECTOR.getKey(), AAC_CONNECTOR_ID),
					connectorMonitor
				)
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
		assertDoesNotThrow(() ->
			telemetryManager.run(new DiscoveryStrategy(telemetryManager, System.currentTimeMillis(), clientsExecutorMock))
		);

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
	}
}
