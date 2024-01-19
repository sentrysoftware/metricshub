package com.sentrysoftware.metricshub.hardware.strategy;

import static com.sentrysoftware.metricshub.engine.common.helpers.MetricsHubConstants.MONITOR_ATTRIBUTE_ID;
import static com.sentrysoftware.metricshub.engine.common.helpers.MetricsHubConstants.MONITOR_ATTRIBUTE_NAME;
import static com.sentrysoftware.metricshub.hardware.common.Constants.ENCLOSURE_PRESENT_METRIC;
import static com.sentrysoftware.metricshub.hardware.util.HwConstants.CONNECTOR;
import static com.sentrysoftware.metricshub.hardware.util.HwConstants.ENCLOSURE;
import static org.junit.jupiter.api.Assertions.assertEquals;

import com.sentrysoftware.metricshub.engine.ClientsExecutor;
import com.sentrysoftware.metricshub.engine.common.helpers.MetricsHubConstants;
import com.sentrysoftware.metricshub.engine.configuration.HostConfiguration;
import com.sentrysoftware.metricshub.engine.configuration.SnmpConfiguration;
import com.sentrysoftware.metricshub.engine.connector.model.ConnectorStore;
import com.sentrysoftware.metricshub.engine.telemetry.Monitor;
import com.sentrysoftware.metricshub.engine.telemetry.MonitorFactory;
import com.sentrysoftware.metricshub.engine.telemetry.TelemetryManager;
import com.sentrysoftware.metricshub.engine.telemetry.metric.NumberMetric;
import java.util.HashMap;
import java.util.Map;
import org.junit.jupiter.api.Test;

class HardwarePostDiscoveryStrategyTest {

	@Test
	void testRunMissingDeviceDetection() {
		final SnmpConfiguration snmpConfig = SnmpConfiguration.builder().community("public").build();
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
			.build();

		final Monitor monitor = monitorFactory.createOrUpdateMonitor();

		new HardwarePostDiscoveryStrategy(telemetryManager, previousDiscoveryTime, clientsExecutor).run();

		assertEquals(1.0, monitor.getMetric(ENCLOSURE_PRESENT_METRIC, NumberMetric.class).getValue());

		new HardwarePostDiscoveryStrategy(telemetryManager, discoveryTime, clientsExecutor).run();
		assertEquals(0.0, monitor.getMetric(ENCLOSURE_PRESENT_METRIC, NumberMetric.class).getValue());
	}
}
