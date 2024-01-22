package org.sentrysoftware.metricshub.engine.telemetry;

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyBoolean;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.spy;

import java.util.HashMap;
import java.util.Map;
import java.util.Set;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;
import org.sentrysoftware.metricshub.engine.client.ClientsExecutor;
import org.sentrysoftware.metricshub.engine.common.helpers.KnownMonitorType;
import org.sentrysoftware.metricshub.engine.common.helpers.MetricsHubConstants;
import org.sentrysoftware.metricshub.engine.configuration.HostConfiguration;
import org.sentrysoftware.metricshub.engine.configuration.SnmpConfiguration;
import org.sentrysoftware.metricshub.engine.connector.model.ConnectorStore;
import org.sentrysoftware.metricshub.engine.constants.Constants;
import org.sentrysoftware.metricshub.engine.strategy.AbstractStrategy;
import org.sentrysoftware.metricshub.engine.strategy.discovery.DiscoveryStrategy;
import org.sentrysoftware.metricshub.engine.strategy.source.SourceTable;

class TelemetryManagerTest {

	@Test
	void testRun() throws Exception {
		final ClientsExecutor clientsExecutorMock = spy(ClientsExecutor.class);

		// Create host and connector monitors and set them in the telemetry manager
		final Monitor hostMonitor = Monitor.builder().type(KnownMonitorType.HOST.getKey()).build();
		final Monitor connectorMonitor = Monitor.builder().type(KnownMonitorType.CONNECTOR.getKey()).build();
		final Map<String, Map<String, Monitor>> monitors = new HashMap<>(
			Map.of(
				Constants.HOST,
				Map.of(Constants.MONITOR_ID_ATTRIBUTE_VALUE, hostMonitor),
				Constants.CONNECTOR,
				Map.of(
					String.format(
						AbstractStrategy.CONNECTOR_ID_FORMAT,
						KnownMonitorType.CONNECTOR.getKey(),
						Constants.AAC_CONNECTOR_ID
					),
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
					.hostId(Constants.HOST_ID)
					.hostname(Constants.HOST_NAME)
					.sequential(false)
					.configurations(Map.of(SnmpConfiguration.class, snmpConfig))
					.build()
			)
			.build();

		hostMonitor.getAttributes().put(MetricsHubConstants.IS_ENDPOINT, "true");

		connectorMonitor.getAttributes().put(Constants.ID, Constants.AAC_CONNECTOR_ID);

		// Create the connector store
		final ConnectorStore connectorStore = new ConnectorStore(Constants.YAML_TEST_PATH);
		telemetryManager.setConnectorStore(connectorStore);

		// Mock detection criteria result
		doReturn("1.3.6.1.4.1.795.10.1.1.3.1.1.0	ASN_OCTET_STR	Test")
			.when(clientsExecutorMock)
			.executeSNMPGetNext(eq("1.3.6.1.4.1.795.10.1.1.3.1.1"), any(SnmpConfiguration.class), anyString(), anyBoolean());

		// Mock source table information for disk controller
		Mockito
			.doReturn(SourceTable.csvToTable("controller-1;1;Adaptec1;bios53v2;firmware32", MetricsHubConstants.TABLE_SEP))
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
		assertEquals(1, discoveredMonitors.get(Constants.HOST).size());
		assertEquals(1, discoveredMonitors.get(Constants.CONNECTOR).size());
		assertEquals(1, discoveredMonitors.get(Constants.DISK_CONTROLLER).size());
		assertEquals(1, discoveredMonitors.get(Constants.PHYSICAL_DISK).size());
		assertEquals(1, discoveredMonitors.get(Constants.LOGICAL_DISK).size());

		// Check discovered monitors order
		final Set<String> expectedOrder = Set.of(
			Constants.HOST,
			Constants.DISK_CONTROLLER,
			Constants.CONNECTOR,
			Constants.LOGICAL_DISK,
			Constants.PHYSICAL_DISK
		);
		assertEquals(expectedOrder, discoveredMonitors.keySet());
	}
}
