package com.sentrysoftware.matrix.strategy.discovery;

import com.sentrysoftware.matrix.common.helpers.KnownMonitorType;
import com.sentrysoftware.matrix.configuration.HostConfiguration;
import com.sentrysoftware.matrix.connector.model.ConnectorStore;
import com.sentrysoftware.matrix.strategy.source.SourceTable;
import com.sentrysoftware.matrix.telemetry.Monitor;
import com.sentrysoftware.matrix.telemetry.TelemetryManager;
import org.junit.jupiter.api.Test;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

import static com.sentrysoftware.matrix.common.helpers.MatrixConstants.IS_ENDPOINT;
import static com.sentrysoftware.matrix.constants.Constants.CONNECTOR;
import static com.sentrysoftware.matrix.constants.Constants.DISK_CONTROLLER;
import static com.sentrysoftware.matrix.constants.Constants.DISK_CONTROLLER_SOURCE_REF_KEY;
import static com.sentrysoftware.matrix.constants.Constants.HOST;
import static com.sentrysoftware.matrix.constants.Constants.HOST_ID;
import static com.sentrysoftware.matrix.constants.Constants.HOST_NAME;
import static com.sentrysoftware.matrix.constants.Constants.ID;
import static com.sentrysoftware.matrix.constants.Constants.LOGICAL_DISK;
import static com.sentrysoftware.matrix.constants.Constants.LOGICAL_DISK_SOURCE_REF_KEY;
import static com.sentrysoftware.matrix.constants.Constants.MONITOR_ID_ATTRIBUTE_VALUE;
import static com.sentrysoftware.matrix.constants.Constants.PHYSICAL_DISK;
import static com.sentrysoftware.matrix.constants.Constants.PHYSICAL_DISK_SOURCE_REF_KEY;
import static com.sentrysoftware.matrix.constants.Constants.SAMPLE_SOURCE_TABLE_DATA_ROW;
import static com.sentrysoftware.matrix.constants.Constants.YAML_TEST_FILE_NAME;
import static com.sentrysoftware.matrix.constants.Constants.YAML_TEST_FILE_NAME_WITH_EXTENSION;
import static com.sentrysoftware.matrix.constants.Constants.YAML_TEST_PATH;
import static org.junit.jupiter.api.Assertions.assertEquals;

public class DiscoveryStrategyTest {

	@Test
	void testRun() {
		// Create host and connector monitors and set them in the telemetry manager
		final Monitor hostMonitor = Monitor.builder().type(KnownMonitorType.HOST.getKey()).build();
		final Monitor connectorMonitor = Monitor.builder().type(KnownMonitorType.CONNECTOR.getKey()).build();
		final Map<String, Map<String, Monitor>> monitors = new HashMap<>(Map.of(
			KnownMonitorType.HOST.getKey(),
			Map.of(MONITOR_ID_ATTRIBUTE_VALUE, hostMonitor), KnownMonitorType.CONNECTOR.getKey(),
			Map.of(YAML_TEST_FILE_NAME_WITH_EXTENSION, connectorMonitor))
		);
		final TelemetryManager telemetryManager = TelemetryManager.builder().monitors(monitors)
			.hostConfiguration(HostConfiguration.builder().hostId(HOST_ID).hostname(HOST_NAME).build())
			.build();
		hostMonitor.getAttributes().put(IS_ENDPOINT, "true");
		connectorMonitor.getAttributes().put(ID, YAML_TEST_FILE_NAME_WITH_EXTENSION);

		// Create the connector store
		final ConnectorStore connectorStore = new ConnectorStore(YAML_TEST_PATH);
		telemetryManager.setConnectorStore(connectorStore);


		// Init source table data
		final List<List<String>> sourceTableData = new ArrayList<>();
		sourceTableData.add(SAMPLE_SOURCE_TABLE_DATA_ROW);
		final SourceTable sourceTable = new SourceTable();
		sourceTable.setTable(sourceTableData);
		telemetryManager.getHostProperties().getConnectorNamespace(YAML_TEST_FILE_NAME)
			.setSourceTables(Map.of(
				DISK_CONTROLLER_SOURCE_REF_KEY, sourceTable,
				LOGICAL_DISK_SOURCE_REF_KEY, sourceTable,
				PHYSICAL_DISK_SOURCE_REF_KEY, sourceTable)
			);

		// Call DiscoveryStrategy to discover the monitors
		final DiscoveryStrategy discoveryStrategy = new DiscoveryStrategy(telemetryManager);
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
