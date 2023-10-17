package com.sentrysoftware.metricshub.engine.strategy.collect;

import static com.sentrysoftware.metricshub.engine.common.helpers.MetricsHubConstants.MONITOR_ATTRIBUTE_ID;
import static com.sentrysoftware.metricshub.engine.common.helpers.MetricsHubConstants.MONITOR_ATTRIBUTE_NAME;
import static com.sentrysoftware.metricshub.engine.constants.Constants.*;
import static com.sentrysoftware.metricshub.engine.constants.Constants.HOST_ID;
import static org.junit.jupiter.api.Assertions.assertEquals;

import com.sentrysoftware.metricshub.engine.common.helpers.MetricsHubConstants;
import com.sentrysoftware.metricshub.engine.configuration.HostConfiguration;
import com.sentrysoftware.metricshub.engine.configuration.SnmpConfiguration;
import com.sentrysoftware.metricshub.engine.connector.model.Connector;
import com.sentrysoftware.metricshub.engine.connector.model.ConnectorStore;
import com.sentrysoftware.metricshub.engine.matsya.MatsyaClientsExecutor;
import com.sentrysoftware.metricshub.engine.telemetry.MetricFactory;
import com.sentrysoftware.metricshub.engine.telemetry.Monitor;
import com.sentrysoftware.metricshub.engine.telemetry.MonitorFactory;
import com.sentrysoftware.metricshub.engine.telemetry.TelemetryManager;
import com.sentrysoftware.metricshub.engine.telemetry.metric.NumberMetric;
import java.util.HashMap;
import java.util.Map;
import org.junit.jupiter.api.Test;

class PrepareCollectStrategyTest {

	private static final String HW_METRIC = "hw.metric";

	@Test
	void testRunRefreshDiscoveredMetrics() {
		final SnmpConfiguration snmpConfig = SnmpConfiguration.builder().community("public").build();
		final ConnectorStore connectorStore = new ConnectorStore();
		connectorStore.setStore(Map.of());

		final TelemetryManager telemetryManager = TelemetryManager
			.builder()
			.hostConfiguration(
				HostConfiguration
					.builder()
					.hostId(HOST_ID)
					.hostname(MetricsHubConstants.HOST_NAME)
					.sequential(false)
					.configurations(Map.of(SnmpConfiguration.class, snmpConfig))
					.build()
			)
			.connectorStore(connectorStore)
			.build();
		final MatsyaClientsExecutor matsyaClientExecutor = new MatsyaClientsExecutor(telemetryManager);
		final long collectTime = System.currentTimeMillis();
		final MonitorFactory monitorFactory = MonitorFactory
			.builder()
			.attributes(new HashMap<>(Map.of(MONITOR_ATTRIBUTE_ID, "id", MONITOR_ATTRIBUTE_NAME, "name")))
			.discoveryTime(collectTime - 30 * 60 * 1000)
			.connectorId(CONNECTOR)
			.telemetryManager(telemetryManager)
			.monitorType(ENCLOSURE)
			.build();

		final Monitor monitor = monitorFactory.createOrUpdateMonitor();

		final MetricFactory metricFactory = new MetricFactory(telemetryManager.getHostname());

		metricFactory.collectMonitorMetrics(
			ENCLOSURE,
			new Connector(),
			monitor,
			MetricsHubConstants.HOST_NAME,
			Map.of(HW_METRIC, "1"),
			collectTime,
			true
		);

		new PrepareCollectStrategy(telemetryManager, collectTime, matsyaClientExecutor).run();

		final NumberMetric metric = monitor.getMetric(HW_METRIC, NumberMetric.class);

		assertEquals(metric.getCollectTime(), collectTime);
	}
}
