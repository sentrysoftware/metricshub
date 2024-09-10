package org.sentrysoftware.metricshub.hardware.strategy;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.sentrysoftware.metricshub.engine.common.helpers.MetricsHubConstants.DEFAULT_KEYS;
import static org.sentrysoftware.metricshub.engine.common.helpers.MetricsHubConstants.MONITOR_ATTRIBUTE_ID;
import static org.sentrysoftware.metricshub.engine.common.helpers.MetricsHubConstants.MONITOR_ATTRIBUTE_NAME;
import static org.sentrysoftware.metricshub.hardware.common.Constants.ENCLOSURE_PRESENT_METRIC;
import static org.sentrysoftware.metricshub.hardware.util.HwConstants.CONNECTOR;
import static org.sentrysoftware.metricshub.hardware.util.HwConstants.ENCLOSURE;

import java.util.HashMap;
import java.util.Map;
import org.junit.jupiter.api.Test;
import org.sentrysoftware.metricshub.engine.client.ClientsExecutor;
import org.sentrysoftware.metricshub.engine.common.helpers.MetricsHubConstants;
import org.sentrysoftware.metricshub.engine.configuration.HostConfiguration;
import org.sentrysoftware.metricshub.engine.connector.model.ConnectorStore;
import org.sentrysoftware.metricshub.engine.extension.ExtensionManager;
import org.sentrysoftware.metricshub.engine.telemetry.Monitor;
import org.sentrysoftware.metricshub.engine.telemetry.MonitorFactory;
import org.sentrysoftware.metricshub.engine.telemetry.TelemetryManager;
import org.sentrysoftware.metricshub.engine.telemetry.metric.NumberMetric;
import org.sentrysoftware.metricshub.extension.snmp.SnmpConfiguration;

class HardwarePostDiscoveryStrategyTest {

	@Test
	void testRunMissingDeviceDetection() {
		final SnmpConfiguration snmpConfig = SnmpConfiguration.builder().community("public".toCharArray()).build();
		final ConnectorStore connectorStore = new ConnectorStore();
		connectorStore.setStore(Map.of());

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
		final MonitorFactory monitorFactory = MonitorFactory
			.builder()
			.attributes(new HashMap<>(Map.of(MONITOR_ATTRIBUTE_ID, "id", MONITOR_ATTRIBUTE_NAME, "name")))
			.discoveryTime(previousDiscoveryTime)
			.connectorId(CONNECTOR)
			.telemetryManager(telemetryManager)
			.monitorType(ENCLOSURE)
			.keys(DEFAULT_KEYS)
			.build();

		final Monitor monitor = monitorFactory.createOrUpdateMonitor();

		ExtensionManager extensionManager = ExtensionManager.builder().build();

		new HardwarePostDiscoveryStrategy(telemetryManager, previousDiscoveryTime, clientsExecutor, extensionManager).run();

		assertEquals(1.0, monitor.getMetric(ENCLOSURE_PRESENT_METRIC, NumberMetric.class).getValue());

		new HardwarePostDiscoveryStrategy(telemetryManager, discoveryTime, clientsExecutor, extensionManager).run();
		assertEquals(0.0, monitor.getMetric(ENCLOSURE_PRESENT_METRIC, NumberMetric.class).getValue());
	}
}
