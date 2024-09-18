package org.sentrysoftware.metricshub.hardware.strategy;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.doCallRealMethod;
import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.mock;
import static org.sentrysoftware.metricshub.engine.common.helpers.MetricsHubConstants.MONITOR_ATTRIBUTE_CONNECTOR_ID;

import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Date;
import java.util.List;
import java.util.Map;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.junit.jupiter.MockitoExtension;
import org.sentrysoftware.metricshub.engine.client.ClientsExecutor;
import org.sentrysoftware.metricshub.engine.common.helpers.KnownMonitorType;
import org.sentrysoftware.metricshub.engine.connector.model.ConnectorStore;
import org.sentrysoftware.metricshub.engine.extension.ExtensionManager;
import org.sentrysoftware.metricshub.engine.telemetry.MetricFactory;
import org.sentrysoftware.metricshub.engine.telemetry.Monitor;
import org.sentrysoftware.metricshub.engine.telemetry.TelemetryManager;
import org.sentrysoftware.metricshub.extension.snmp.SnmpExtension;
import org.sentrysoftware.metricshub.extension.snmp.SnmpRequestExecutor;

@ExtendWith(MockitoExtension.class)
class HardwarePostDiscoveryStrategyTest {

	@Mock
	private SnmpRequestExecutor snmpRequestExecutorMock;

	@Mock
	private HardwarePostDiscoveryStrategy hardwarePostDiscoveryStrategyMock;

	@Mock
	private MetricFactory metricFactoryMock;

	final long STRATEGY_TIME = new Date().getTime();

	final Path TEST_CONNECTOR_PATH = Paths.get("src", "test", "resources", "strategy", "postDiscovery");

	@Test
	void testHwStatusPresentMetricWithoutHardwareTag() {
		final TelemetryManager telemetryManagerMock = mock(TelemetryManager.class);
		// Create the monitor
		final Monitor cpuMonitorOnLinuxConnector = Monitor
			.builder()
			.type(KnownMonitorType.CPU.getKey())
			.attributes(Map.of(MONITOR_ATTRIBUTE_CONNECTOR_ID, "ConnectorWithoutHardwareTag"))
			.discoveryTime(STRATEGY_TIME)
			.build();

		// Mock TelemetryManager.getMonitors
		doReturn(Map.of(KnownMonitorType.CPU.getKey(), Map.of("cpuOnLinux", cpuMonitorOnLinuxConnector)))
			.when(telemetryManagerMock)
			.getMonitors();

		// Build the extension manager
		final ExtensionManager extensionManager = ExtensionManager
			.builder()
			.withProtocolExtensions(List.of(new SnmpExtension(snmpRequestExecutorMock)))
			.build();

		// Set the connector store
		final ConnectorStore connectorStore = new ConnectorStore(TEST_CONNECTOR_PATH);
		doReturn(connectorStore).when(telemetryManagerMock).getConnectorStore();

		// Mock HardwarePostDiscoveryStrategy
		hardwarePostDiscoveryStrategyMock =
			Mockito.spy(
				new HardwarePostDiscoveryStrategy(telemetryManagerMock, STRATEGY_TIME, new ClientsExecutor(), extensionManager)
			);

		// Run HardwarePostDiscoveryStrategy
		hardwarePostDiscoveryStrategyMock.run();

		// Check that the metric hw.status{hw.type="cpu", state="present"} is absent
		assertNull(cpuMonitorOnLinuxConnector.getMetric("hw.status{hw.type=\"cpu\", state=\"present\"}"));
	}

	@Test
	void testHwStatusPresentMetricWithHardwareTag() {
		final TelemetryManager telemetryManagerMock = mock(TelemetryManager.class);
		// Create the monitor
		final Monitor cpuMonitorWithHardware = Monitor
			.builder()
			.type(KnownMonitorType.CPU.getKey())
			.attributes(Map.of(MONITOR_ATTRIBUTE_CONNECTOR_ID, "ConnectorWithHardwareTag"))
			.discoveryTime(STRATEGY_TIME)
			.build();

		// Mock TelemetryManager.getMonitors
		doReturn(Map.of(KnownMonitorType.CPU.getKey(), Map.of("cpuOnHardware", cpuMonitorWithHardware)))
			.when(telemetryManagerMock)
			.getMonitors();

		// Build the extension manager
		final ExtensionManager extensionManager = ExtensionManager
			.builder()
			.withProtocolExtensions(List.of(new SnmpExtension(snmpRequestExecutorMock)))
			.build();

		// Set the connector store
		final ConnectorStore connectorStore = new ConnectorStore(TEST_CONNECTOR_PATH);
		doReturn(connectorStore).when(telemetryManagerMock).getConnectorStore();

		// Mock hostname
		doReturn("localhost").when(telemetryManagerMock).getHostname();

		// Mock HardwarePostDiscoveryStrategy
		hardwarePostDiscoveryStrategyMock =
			Mockito.spy(
				new HardwarePostDiscoveryStrategy(telemetryManagerMock, STRATEGY_TIME, new ClientsExecutor(), extensionManager)
			);

		// Mock setAsPresent
		doCallRealMethod()
			.when(hardwarePostDiscoveryStrategyMock)
			.setAsPresent(eq(cpuMonitorWithHardware), anyString(), anyString());

		// Run HardwarePostDiscoveryStrategy
		hardwarePostDiscoveryStrategyMock.run();

		// Check that the metric hw.status{hw.type="cpu", state="present"} is present
		// and it's value is correctly set
		assertEquals(1.0, cpuMonitorWithHardware.getMetric("hw.status{hw.type=\"cpu\", state=\"present\"}").getValue());
	}

	@Test
	void testHwStatusMissingMetricWithoutHardwareTag() {
		final TelemetryManager telemetryManagerMock = mock(TelemetryManager.class);
		// Create the monitor
		final Monitor cpuMonitorOnLinuxConnector = Monitor
			.builder()
			.type(KnownMonitorType.CPU.getKey())
			.attributes(Map.of(MONITOR_ATTRIBUTE_CONNECTOR_ID, "ConnectorWithoutHardwareTag"))
			.discoveryTime(STRATEGY_TIME - 6000)
			.build();

		// Mock TelemetryManager.getMonitors
		doReturn(Map.of(KnownMonitorType.CPU.getKey(), Map.of("cpuOnLinux", cpuMonitorOnLinuxConnector)))
			.when(telemetryManagerMock)
			.getMonitors();

		// Build the extension manager
		final ExtensionManager extensionManager = ExtensionManager
			.builder()
			.withProtocolExtensions(List.of(new SnmpExtension(snmpRequestExecutorMock)))
			.build();

		// Set the connector store
		final ConnectorStore connectorStore = new ConnectorStore(TEST_CONNECTOR_PATH);
		doReturn(connectorStore).when(telemetryManagerMock).getConnectorStore();

		// Mock HardwarePostDiscoveryStrategy
		hardwarePostDiscoveryStrategyMock =
			Mockito.spy(
				new HardwarePostDiscoveryStrategy(telemetryManagerMock, STRATEGY_TIME, new ClientsExecutor(), extensionManager)
			);

		// Run HardwarePostDiscoveryStrategy
		hardwarePostDiscoveryStrategyMock.run();

		// Check that the metric hw.status{hw.type="cpu", state="present"} is absent
		assertNull(cpuMonitorOnLinuxConnector.getMetric("hw.status{hw.type=\"cpu\", state=\"present\"}"));
	}

	@Test
	void testHwStatusMissingMetricWithHardwareTag() {
		final TelemetryManager telemetryManagerMock = mock(TelemetryManager.class);
		// Create the monitor
		final Monitor cpuMonitorWithHardware = Monitor
			.builder()
			.type(KnownMonitorType.CPU.getKey())
			.attributes(Map.of(MONITOR_ATTRIBUTE_CONNECTOR_ID, "ConnectorWithHardwareTag"))
			.discoveryTime(STRATEGY_TIME - 6000)
			.build();

		// Mock TelemetryManager.getMonitors
		doReturn(Map.of(KnownMonitorType.CPU.getKey(), Map.of("cpuOnHardware", cpuMonitorWithHardware)))
			.when(telemetryManagerMock)
			.getMonitors();

		// Build the extension manager
		final ExtensionManager extensionManager = ExtensionManager
			.builder()
			.withProtocolExtensions(List.of(new SnmpExtension(snmpRequestExecutorMock)))
			.build();

		// Set the connector store
		final ConnectorStore connectorStore = new ConnectorStore(TEST_CONNECTOR_PATH);
		doReturn(connectorStore).when(telemetryManagerMock).getConnectorStore();

		// Mock hostname
		doReturn("localhost").when(telemetryManagerMock).getHostname();

		// Mock HardwarePostDiscoveryStrategy
		hardwarePostDiscoveryStrategyMock =
			Mockito.spy(
				new HardwarePostDiscoveryStrategy(telemetryManagerMock, STRATEGY_TIME, new ClientsExecutor(), extensionManager)
			);

		// Mock setAsMissing
		doCallRealMethod()
			.when(hardwarePostDiscoveryStrategyMock)
			.setAsMissing(eq(cpuMonitorWithHardware), anyString(), anyString());

		// Run HardwarePostDiscoveryStrategy
		hardwarePostDiscoveryStrategyMock.run();

		// Check that the metric hw.status{hw.type="cpu", state="present"} is present
		// and it's value is correctly set
		assertEquals(0.0, cpuMonitorWithHardware.getMetric("hw.status{hw.type=\"cpu\", state=\"present\"}").getValue());
	}
}
