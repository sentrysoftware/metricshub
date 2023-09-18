package com.sentrysoftware.matrix.strategy.simple;

import com.sentrysoftware.matrix.common.helpers.KnownMonitorType;
import com.sentrysoftware.matrix.common.helpers.MatrixConstants;
import com.sentrysoftware.matrix.configuration.HostConfiguration;
import com.sentrysoftware.matrix.configuration.SnmpConfiguration;
import com.sentrysoftware.matrix.connector.model.ConnectorStore;
import com.sentrysoftware.matrix.matsya.MatsyaClientsExecutor;
import com.sentrysoftware.matrix.strategy.source.SourceTable;
import com.sentrysoftware.matrix.telemetry.Monitor;
import com.sentrysoftware.matrix.telemetry.TelemetryManager;
import com.sentrysoftware.matrix.telemetry.metric.NumberMetric;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Date;
import java.util.HashMap;
import java.util.Map;

import static com.sentrysoftware.matrix.common.helpers.MatrixConstants.IS_ENDPOINT;
import static com.sentrysoftware.matrix.constants.Constants.CONNECTOR;
import static com.sentrysoftware.matrix.constants.Constants.DISK_CONTROLLER;
import static com.sentrysoftware.matrix.constants.Constants.ENCLOSURE;
import static com.sentrysoftware.matrix.constants.Constants.HOST;
import static com.sentrysoftware.matrix.constants.Constants.HOST_ID;
import static com.sentrysoftware.matrix.constants.Constants.HOST_NAME;
import static com.sentrysoftware.matrix.constants.Constants.ID;
import static com.sentrysoftware.matrix.constants.Constants.MONITOR_ID_ATTRIBUTE_VALUE;
import static com.sentrysoftware.matrix.constants.Constants.TEST_CONNECTOR_FILE_NAME;
import static com.sentrysoftware.matrix.constants.Constants.TEST_CONNECTOR_PATH;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.doReturn;

@ExtendWith(MockitoExtension.class)
public class SimpleStrategyTest {
	@Mock
	private MatsyaClientsExecutor matsyaClientsExecutorMock;

	@InjectMocks
	private SimpleStrategy simpleStrategy;

	static Long strategyTime = new Date().getTime();

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
						Map.of(TEST_CONNECTOR_FILE_NAME, connectorMonitor)
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

		final String connectorId = TEST_CONNECTOR_FILE_NAME.split("\\.")[0];
		connectorMonitor.getAttributes().put(ID, TEST_CONNECTOR_FILE_NAME);

		// Create the connector store
		final ConnectorStore connectorStore = new ConnectorStore(TEST_CONNECTOR_PATH);
		telemetryManager.setConnectorStore(connectorStore);
		simpleStrategy.setTelemetryManager(telemetryManager);
		simpleStrategy.setStrategyTime(strategyTime);
		simpleStrategy.setSimple(true);

		// Mock source table information for enclosure
		doReturn(SourceTable.csvToTable("enclosure-1;1;healthy", MatrixConstants.TABLE_SEP))
				.when(matsyaClientsExecutorMock)
				.executeSNMPTable(
						eq("1.3.6.1.4.1.795.10.1.1.3.1"),
						any(String[].class),
						any(SnmpConfiguration.class),
						anyString(),
						eq(true)
				);

		// Mock source table information for disk_controller
		doReturn(SourceTable.csvToTable("1;1;healthy", MatrixConstants.TABLE_SEP))
				.when(matsyaClientsExecutorMock)
				.executeSNMPTable(
						eq("1.3.6.1.4.1.795.10.1.1.4.1"),
						any(String[].class),
						any(SnmpConfiguration.class),
						anyString(),
						eq(true)
				);
		simpleStrategy.run();

		// Check discovered monitors
		final Map<String, Map<String, Monitor>> discoveredMonitors = telemetryManager.getMonitors();

		final Map<String, Monitor> enclosureMonitors = discoveredMonitors.get(ENCLOSURE);
		final Map<String, Monitor> diskControllerMonitors = discoveredMonitors.get(DISK_CONTROLLER);

		assertEquals(4, discoveredMonitors.size());
		assertEquals(1, enclosureMonitors.size());
		assertEquals(1, diskControllerMonitors.size());

		// Check discovered monitors metrics
		final Monitor enclosure = enclosureMonitors.get("TestConnector_enclosure_enclosure-1");
		final Monitor diskController = diskControllerMonitors.get("TestConnector_disk_controller_1");

		assertEquals(1.0, enclosure.getMetric("hw.status{hw.type=\"enclosure\"}", NumberMetric.class).getValue());
		assertEquals(1.0, diskController.getMetric("hw.status{hw.type=\"disk_controller\"}", NumberMetric.class).getValue());
	}
}
