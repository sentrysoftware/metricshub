package com.sentrysoftware.matrix.strategy.discovery;

import static com.sentrysoftware.matrix.common.helpers.MatrixConstants.IS_ENDPOINT;
import static com.sentrysoftware.matrix.constants.Constants.CONNECTOR;
import static com.sentrysoftware.matrix.constants.Constants.DISK_CONTROLLER;
import static com.sentrysoftware.matrix.constants.Constants.HOST;
import static com.sentrysoftware.matrix.constants.Constants.HOST_ID;
import static com.sentrysoftware.matrix.constants.Constants.HOST_NAME;
import static com.sentrysoftware.matrix.constants.Constants.ID;
import static com.sentrysoftware.matrix.constants.Constants.LOGICAL_DISK;
import static com.sentrysoftware.matrix.constants.Constants.MONITOR_ID_ATTRIBUTE_VALUE;
import static com.sentrysoftware.matrix.constants.Constants.PHYSICAL_DISK;
import static com.sentrysoftware.matrix.constants.Constants.YAML_TEST_FILE_NAME_WITH_EXTENSION;
import static com.sentrysoftware.matrix.constants.Constants.YAML_TEST_PATH;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyBoolean;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.doReturn;

import java.util.Date;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.TimeoutException;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import com.sentrysoftware.matrix.common.helpers.KnownMonitorType;
import com.sentrysoftware.matrix.common.helpers.MatrixConstants;
import com.sentrysoftware.matrix.configuration.HostConfiguration;
import com.sentrysoftware.matrix.configuration.SnmpConfiguration;
import com.sentrysoftware.matrix.connector.model.ConnectorStore;
import com.sentrysoftware.matrix.matsya.MatsyaClientsExecutor;
import com.sentrysoftware.matrix.strategy.source.SourceTable;
import com.sentrysoftware.matrix.telemetry.Monitor;
import com.sentrysoftware.matrix.telemetry.TelemetryManager;


@ExtendWith(MockitoExtension.class)
class DiscoveryStrategyTest {

	@Mock
	private MatsyaClientsExecutor matsyaClientsExecutor;

	@InjectMocks
	private DiscoveryStrategy discoveryStrategy;

	private static Long strategyTime = new Date().getTime();

	@BeforeEach
	void beforeEeach() {
		discoveryStrategy.setStrategyTime(strategyTime);
		discoveryStrategy.setTelemetryManager(null);
	}

	@Test
	void testRun() throws InterruptedException, ExecutionException, TimeoutException {
		// Create host and connector monitors and set them in the telemetry manager
		final Monitor hostMonitor = Monitor.builder().type(KnownMonitorType.HOST.getKey()).build();
		final Monitor connectorMonitor = Monitor.builder().type(KnownMonitorType.CONNECTOR.getKey()).build();
		final Map<String, Map<String, Monitor>> monitors = new HashMap<>(
			Map.of(
				KnownMonitorType.HOST.getKey(),
				Map.of(MONITOR_ID_ATTRIBUTE_VALUE, hostMonitor),
				KnownMonitorType.CONNECTOR.getKey(),
				Map.of(YAML_TEST_FILE_NAME_WITH_EXTENSION, connectorMonitor)
			)
		);

		final TelemetryManager telemetryManager = TelemetryManager
			.builder()
			.monitors(monitors)
			.hostConfiguration(HostConfiguration.builder().hostId(HOST_ID).hostname(HOST_NAME).build())
			.build();

		hostMonitor.getAttributes().put(IS_ENDPOINT, "true");

		connectorMonitor.getAttributes().put(ID, YAML_TEST_FILE_NAME_WITH_EXTENSION);

		// Create the connector store
		final ConnectorStore connectorStore = new ConnectorStore(YAML_TEST_PATH);
		telemetryManager.setConnectorStore(connectorStore);

		// Mock source table information
		doReturn(SourceTable
			.builder()
			.table(SourceTable.csvToTable("controller-1;1;Adaptec1;bios53v2;firmware32", MatrixConstants.COMMA))
			.build()
		)
		.when(matsyaClientsExecutor)
		.executeSNMPTable(
			eq("1.3.6.1.4.1.795.10.1.1.3.1"),
			eq(new String[] {"ID","1","3","7","8"}),
			any(SnmpConfiguration.class),
			anyString(),
			anyBoolean()
		);

		// Call DiscoveryStrategy to discover the monitors
		discoveryStrategy.setTelemetryManager(telemetryManager);
		discoveryStrategy.run();

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
