package com.sentrysoftware.metricshub.engine.connector.deserializer;

import static com.sentrysoftware.metricshub.engine.common.helpers.MetricsHubConstants.EMPTY;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

import com.sentrysoftware.metricshub.engine.connector.model.Connector;
import com.sentrysoftware.metricshub.engine.connector.model.metric.MetricDefinition;
import com.sentrysoftware.metricshub.engine.connector.model.metric.MetricType;
import com.sentrysoftware.metricshub.engine.connector.model.metric.StateSet;
import java.io.IOException;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;
import org.junit.jupiter.api.Test;

class MetricsDeserializerTest extends DeserializerTest {

	@Override
	public String getResourcePath() {
		return "src/test/resources/test-files/metrics/";
	}

	@Test
	void testDeserializeMetrics() throws IOException {
		final Connector connector = getConnector("metrics");

		assertNotNull(connector);

		final Map<String, MetricDefinition> metrics = connector.getMetrics();

		assertTrue(metrics instanceof HashMap, "metrics are expected to be a HashMap.");

		final Map<String, MetricDefinition> expected = Map.of(
			"hw.energy",
			MetricDefinition.builder().type(MetricType.GAUGE).unit("J").description("descr").build(),
			"hw.status",
			MetricDefinition
				.builder()
				.type(StateSet.builder().set(Set.of("degraded", "failed", "ok")).build())
				.description("descr")
				.unit(EMPTY)
				.build()
		);
		assertEquals(expected, metrics);
	}
}
