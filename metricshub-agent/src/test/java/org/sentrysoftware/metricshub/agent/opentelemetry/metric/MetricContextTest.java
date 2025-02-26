package org.sentrysoftware.metricshub.agent.opentelemetry.metric;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

import org.junit.jupiter.api.Test;
import org.sentrysoftware.metricshub.agent.config.StateSetMetricCompression;
import org.sentrysoftware.metricshub.engine.connector.model.metric.MetricType;

class MetricContextTest {

	@Test
	void shouldCreateMetricContextWithCorrectValues() {
		final MetricContext context = MetricContext
			.builder()
			.withType(MetricType.GAUGE)
			.withUnit("s")
			.withDescription("Test description")
			.withIsSuppressZerosCompression(true)
			.build();

		assertEquals(MetricType.GAUGE, context.getType(), "Metric type should match");
		assertEquals("s", context.getUnit(), "Unit should match");
		assertEquals("Test description", context.getDescription(), "Description should match");
		assertTrue(context.isSuppressZerosCompression(), "Suppress zeros compression should be true");
	}

	@Test
	void isSuppressZerosCompression_shouldReturnTrueForMatchingValue() {
		assertTrue(
			MetricContext.isSuppressZerosCompression(StateSetMetricCompression.SUPPRESS_ZEROS),
			"Should return true for suppress_zeros"
		);
	}

	@Test
	void isSuppressZerosCompression_shouldReturnFalseForNonMatchingValue() {
		assertFalse(
			MetricContext.isSuppressZerosCompression(StateSetMetricCompression.NONE),
			"Should return false for non-matching value"
		);
	}
}
