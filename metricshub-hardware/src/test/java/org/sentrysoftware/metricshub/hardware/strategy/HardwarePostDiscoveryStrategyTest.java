package org.sentrysoftware.metricshub.hardware.strategy;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.sentrysoftware.metricshub.engine.common.helpers.MetricsHubConstants.DEFAULT_KEYS;
import static org.sentrysoftware.metricshub.engine.common.helpers.MetricsHubConstants.MONITOR_ATTRIBUTE_ID;
import static org.sentrysoftware.metricshub.engine.common.helpers.MetricsHubConstants.MONITOR_ATTRIBUTE_NAME;
import static org.sentrysoftware.metricshub.hardware.common.Constants.ENCLOSURE_PRESENT_METRIC;
import static org.sentrysoftware.metricshub.hardware.util.HwConstants.CONNECTOR;
import static org.sentrysoftware.metricshub.hardware.util.HwConstants.ENCLOSURE;

import java.util.HashMap;
import java.util.Map;
import java.util.Set;
import org.junit.jupiter.api.Test;
import org.sentrysoftware.metricshub.engine.client.ClientsExecutor;
import org.sentrysoftware.metricshub.engine.common.helpers.KnownMonitorType;
import org.sentrysoftware.metricshub.engine.common.helpers.MetricsHubConstants;
import org.sentrysoftware.metricshub.engine.configuration.HostConfiguration;
import org.sentrysoftware.metricshub.engine.connector.model.Connector;
import org.sentrysoftware.metricshub.engine.connector.model.ConnectorStore;
import org.sentrysoftware.metricshub.engine.connector.model.common.DeviceKind;
import org.sentrysoftware.metricshub.engine.connector.model.identity.ConnectorIdentity;
import org.sentrysoftware.metricshub.engine.connector.model.identity.Detection;
import org.sentrysoftware.metricshub.engine.extension.ExtensionManager;
import org.sentrysoftware.metricshub.engine.telemetry.Monitor;
import org.sentrysoftware.metricshub.engine.telemetry.MonitorFactory;
import org.sentrysoftware.metricshub.engine.telemetry.TelemetryManager;
import org.sentrysoftware.metricshub.engine.telemetry.metric.NumberMetric;
import org.sentrysoftware.metricshub.extension.snmp.SnmpConfiguration;

class HardwarePostDiscoveryStrategyTest {

	@Test
	void testRunMissingAndPresentDeviceDetectionWithHardwareTag() {
		final SnmpConfiguration snmpConfig = SnmpConfiguration.builder().community("public".toCharArray()).build();
		final ConnectorStore connectorStore = new ConnectorStore();
		final Connector connector = new Connector();
		connector.setConnectorIdentity(
			ConnectorIdentity
				.builder()
				.detection(Detection.builder().tags(Set.of("hardware")).appliesTo(Set.of(DeviceKind.WINDOWS)).build())
				.build()
		);
		connectorStore.setStore(Map.of(CONNECTOR, connector));

		final TelemetryManager telemetryManager = TelemetryManager
			.builder()
			.hostConfiguration(
				HostConfiguration
					.builder()
					.hostId("PC-120")
					.hostname(MetricsHubConstants.HOST_NAME)
					.sequential(false)
					.configurations(Map.of(SnmpConfiguration.class, snmpConfig))
					.build()
			)
			.connectorStore(connectorStore)
			.build();
		final ClientsExecutor clientsExecutor = new ClientsExecutor(telemetryManager);
		final long discoveryTime = System.currentTimeMillis();
		final long previousDiscoveryTime = discoveryTime - 30 * 60 * 1000;

		// Create an enclosure monitor
		final MonitorFactory enclosureMonitorFactory = MonitorFactory
			.builder()
			.attributes(new HashMap<>(Map.of(MONITOR_ATTRIBUTE_ID, "id", MONITOR_ATTRIBUTE_NAME, "name")))
			.discoveryTime(previousDiscoveryTime)
			.connectorId(CONNECTOR)
			.telemetryManager(telemetryManager)
			.monitorType(ENCLOSURE)
			.keys(DEFAULT_KEYS)
			.build();

		final Monitor enclosureMonitor = enclosureMonitorFactory.createOrUpdateMonitor();

		// Create the extension manager
		final ExtensionManager extensionManager = ExtensionManager.builder().build();

		// Create a host Monitor
		final MonitorFactory hostMonitorFactory = MonitorFactory
			.builder()
			.attributes(new HashMap<>(Map.of(MONITOR_ATTRIBUTE_ID, "host-1", MONITOR_ATTRIBUTE_NAME, "host-1")))
			.discoveryTime(previousDiscoveryTime)
			.connectorId(CONNECTOR)
			.telemetryManager(telemetryManager)
			.monitorType(KnownMonitorType.HOST.getKey())
			.keys(DEFAULT_KEYS)
			.build();
		final Monitor hostMonitor = hostMonitorFactory.createOrUpdateMonitor();

		// There is no connector monitor having a hardware tag, so the hw.status won't be set on the host monitor
		new HardwarePostDiscoveryStrategy(telemetryManager, previousDiscoveryTime, clientsExecutor, extensionManager).run();

		assertEquals(1.0, enclosureMonitor.getMetric(ENCLOSURE_PRESENT_METRIC, NumberMetric.class).getValue());

		// Check the hw.status metric of the host monitor
		assertNull(hostMonitor.getMetric("hw.status{hw.type=\"host\", state=\"present\"}", NumberMetric.class));

		// Create a connector Monitor
		final MonitorFactory connectorMonitorFactory = MonitorFactory
			.builder()
			.attributes(new HashMap<>(Map.of(MONITOR_ATTRIBUTE_ID, "connector-1", MONITOR_ATTRIBUTE_NAME, "conn-1")))
			.discoveryTime(previousDiscoveryTime)
			.connectorId(CONNECTOR)
			.telemetryManager(telemetryManager)
			.monitorType(CONNECTOR)
			.keys(DEFAULT_KEYS)
			.build();
		final Monitor connectorMonitor = connectorMonitorFactory.createOrUpdateMonitor();

		// There is a connector monitor having a hardware tag, so the hw.status will be set on the host monitor
		new HardwarePostDiscoveryStrategy(telemetryManager, previousDiscoveryTime, clientsExecutor, extensionManager).run();

		// Check the hw.status metric of the enclosure monitor
		assertEquals(1.0, enclosureMonitor.getMetric(ENCLOSURE_PRESENT_METRIC, NumberMetric.class).getValue());

		// Check the hw.status metric of the connector monitor
		assertEquals(
			1.0,
			connectorMonitor.getMetric("hw.status{hw.type=\"connector\", state=\"present\"}", NumberMetric.class).getValue()
		);

		// Check the hw.status metric of the host monitor
		assertEquals(
			1.0,
			hostMonitor.getMetric("hw.status{hw.type=\"host\", state=\"present\"}", NumberMetric.class).getValue()
		);

		new HardwarePostDiscoveryStrategy(telemetryManager, discoveryTime, clientsExecutor, extensionManager).run();

		// Check the hw.status metric of the enclosure monitor
		assertEquals(0.0, enclosureMonitor.getMetric(ENCLOSURE_PRESENT_METRIC, NumberMetric.class).getValue());

		// Check the hw.status metric of the connector monitor
		assertEquals(
			0.0,
			connectorMonitor.getMetric("hw.status{hw.type=\"connector\", state=\"present\"}", NumberMetric.class).getValue()
		);

		// Check the hw.status metric of the host monitor
		assertEquals(
			0.0,
			hostMonitor.getMetric("hw.status{hw.type=\"host\", state=\"present\"}", NumberMetric.class).getValue()
		);
	}

	@Test
	void testRunPresentDeviceDetectionWithoutHardwareTags() {
		final SnmpConfiguration snmpConfig = SnmpConfiguration.builder().community("public".toCharArray()).build();
		final ConnectorStore connectorStore = new ConnectorStore();
		final Connector connector = new Connector();
		connector.setConnectorIdentity(
			ConnectorIdentity.builder().detection(Detection.builder().appliesTo(Set.of(DeviceKind.WINDOWS)).build()).build()
		);
		connectorStore.setStore(Map.of(CONNECTOR, connector));

		final TelemetryManager telemetryManager = TelemetryManager
			.builder()
			.hostConfiguration(
				HostConfiguration
					.builder()
					.hostId("PC-120")
					.hostname(MetricsHubConstants.HOST_NAME)
					.sequential(false)
					.configurations(Map.of(SnmpConfiguration.class, snmpConfig))
					.build()
			)
			.connectorStore(connectorStore)
			.build();
		final ClientsExecutor clientsExecutor = new ClientsExecutor(telemetryManager);
		final long discoveryTime = System.currentTimeMillis();

		// Create an enclosure monitor
		final MonitorFactory enclosureMonitorFactory = MonitorFactory
			.builder()
			.attributes(new HashMap<>(Map.of(MONITOR_ATTRIBUTE_ID, "id", MONITOR_ATTRIBUTE_NAME, "name")))
			.discoveryTime(discoveryTime)
			.connectorId(CONNECTOR)
			.telemetryManager(telemetryManager)
			.monitorType(ENCLOSURE)
			.keys(DEFAULT_KEYS)
			.build();

		final Monitor enclosureMonitor = enclosureMonitorFactory.createOrUpdateMonitor();

		// Create a connector Monitor
		final MonitorFactory connectorMonitorFactory = MonitorFactory
			.builder()
			.attributes(new HashMap<>(Map.of(MONITOR_ATTRIBUTE_ID, "connector-1", MONITOR_ATTRIBUTE_NAME, "conn-1")))
			.discoveryTime(discoveryTime)
			.connectorId(CONNECTOR)
			.telemetryManager(telemetryManager)
			.monitorType(CONNECTOR)
			.keys(DEFAULT_KEYS)
			.build();
		final Monitor connectorMonitor = connectorMonitorFactory.createOrUpdateMonitor();

		// Create a host Monitor
		final MonitorFactory hostMonitorFactory = MonitorFactory
			.builder()
			.attributes(new HashMap<>(Map.of(MONITOR_ATTRIBUTE_ID, "host-1", MONITOR_ATTRIBUTE_NAME, "host-1")))
			.discoveryTime(discoveryTime)
			.connectorId(CONNECTOR)
			.telemetryManager(telemetryManager)
			.monitorType(KnownMonitorType.HOST.getKey())
			.keys(DEFAULT_KEYS)
			.build();
		final Monitor hostMonitor = hostMonitorFactory.createOrUpdateMonitor();

		final ExtensionManager extensionManager = ExtensionManager.builder().build();

		new HardwarePostDiscoveryStrategy(telemetryManager, discoveryTime, clientsExecutor, extensionManager).run();

		// Check the hw.status metric of the enclosure monitor
		assertNull(enclosureMonitor.getMetric(ENCLOSURE_PRESENT_METRIC, NumberMetric.class));

		// Check the hw.status metric of the connector monitor
		assertNull(connectorMonitor.getMetric("hw.status{hw.type=\"connector\", state=\"present\"}", NumberMetric.class));

		// Check the hw.status metric of the host monitor
		assertNull(hostMonitor.getMetric("hw.status{hw.type=\"host\", state=\"present\"}", NumberMetric.class));
	}
}
