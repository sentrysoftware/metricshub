package com.sentrysoftware.matrix.strategy.discovery;

import static com.sentrysoftware.matrix.common.helpers.MatrixConstants.MONITOR_ATTRIBUTE_ID;
import static com.sentrysoftware.matrix.common.helpers.MatrixConstants.MONITOR_ATTRIBUTE_NAME;
import static com.sentrysoftware.matrix.constants.Constants.CONNECTOR;
import static com.sentrysoftware.matrix.constants.Constants.ENCLOSURE;
import static com.sentrysoftware.matrix.constants.Constants.ENCLOSURE_PRESENT_METRIC;
import static com.sentrysoftware.matrix.constants.Constants.HOST_ID;
import static org.junit.jupiter.api.Assertions.assertEquals;

import com.sentrysoftware.matrix.common.helpers.MatrixConstants;
import com.sentrysoftware.matrix.configuration.HostConfiguration;
import com.sentrysoftware.matrix.configuration.SnmpConfiguration;
import com.sentrysoftware.matrix.connector.model.ConnectorStore;
import com.sentrysoftware.matrix.matsya.MatsyaClientsExecutor;
import com.sentrysoftware.matrix.telemetry.Monitor;
import com.sentrysoftware.matrix.telemetry.MonitorFactory;
import com.sentrysoftware.matrix.telemetry.TelemetryManager;
import com.sentrysoftware.matrix.telemetry.metric.NumberMetric;
import java.util.HashMap;
import java.util.Map;
import org.junit.jupiter.api.Test;

class PostDiscoveryStrategyTest {

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
					.hostId(HOST_ID)
					.hostname(MatrixConstants.HOST_NAME)
					.sequential(false)
					.configurations(Map.of(SnmpConfiguration.class, snmpConfig))
					.build()
			)
			.connectorStore(connectorStore)
			.build();
		final MatsyaClientsExecutor matsyaClientExecutor = new MatsyaClientsExecutor(telemetryManager);
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

		new PostDiscoveryStrategy(telemetryManager, previousDiscoveryTime, matsyaClientExecutor).run();

		assertEquals(1.0, monitor.getMetric(ENCLOSURE_PRESENT_METRIC, NumberMetric.class).getValue());

		new PostDiscoveryStrategy(telemetryManager, discoveryTime, matsyaClientExecutor).run();
		assertEquals(0.0, monitor.getMetric(ENCLOSURE_PRESENT_METRIC, NumberMetric.class).getValue());
	}
}
