package org.sentrysoftware.metricshub.agent.opentelemetry.metric.recorder;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.when;

import io.opentelemetry.proto.common.v1.KeyValue;
import io.opentelemetry.proto.metrics.v1.Metric;
import io.opentelemetry.proto.metrics.v1.NumberDataPoint;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.sentrysoftware.metricshub.agent.service.TestHelper;
import org.sentrysoftware.metricshub.engine.telemetry.metric.NumberMetric;

@ExtendWith(MockitoExtension.class)
class CounterMetricRecorderTest {

	@Mock
	private NumberMetric mockMetric;

	private CounterMetricRecorder counterMetricRecorder;

	@BeforeEach
	void setUp() {
		TestHelper.configureGlobalLogger();
		counterMetricRecorder =
			CounterMetricRecorder
				.builder()
				.withMetric(mockMetric)
				.withUnit("s")
				.withDescription("Test counter metric")
				.build();
	}

	@Test
	void doRecord_shouldReturnMetric_whenMetricHasValue() {
		// Given
		when(mockMetric.isUpdated()).thenReturn(true);
		when(mockMetric.getValue()).thenReturn(10.0);
		when(mockMetric.getName()).thenReturn("test.metric");
		when(mockMetric.getCollectTime()).thenReturn(System.currentTimeMillis());
		when(mockMetric.getAttributes()).thenReturn(Map.of("key", "value"));

		// When
		final Optional<Metric> result = counterMetricRecorder.doRecord();

		// Then
		assertTrue(result.isPresent(), "Expected a recorded metric");
		final Metric metric = result.get();
		assertEquals("test.metric", metric.getName(), "Metric name should match");
		assertEquals("Test counter metric", metric.getDescription(), "Metric description should match");
		assertEquals("s", metric.getUnit(), "Metric unit should match");
		final List<NumberDataPoint> dataPointsList = metric.getSum().getDataPointsList();
		assertEquals(1, dataPointsList.size(), "Expected a single metric");
		final NumberDataPoint numberDataPoint = dataPointsList.get(0);
		assertEquals(10.0, numberDataPoint.getAsDouble(), "Metric value should match");
		final List<KeyValue> attributesList = numberDataPoint.getAttributesList();
		assertEquals(1, attributesList.size(), "Expected a single attribute");
		final KeyValue keyValue = attributesList.get(0);
		assertEquals("key", keyValue.getKey(), "Attribute key should match");
		assertEquals("value", keyValue.getValue().getStringValue(), "Attribute value should match");
	}

	@Test
	void doRecord_shouldReturnEmptyOptional_whenMetricIsNotUpdated() {
		// Given
		when(mockMetric.isUpdated()).thenReturn(false);

		// When
		final Optional<Metric> result = counterMetricRecorder.doRecord();

		// Then
		assertTrue(result.isEmpty(), "Expected an empty optional when metric is not updated");
	}

	@Test
	void doRecord_shouldReturnEmptyOptional_whenExceptionOccurs() {
		// Given
		when(mockMetric.isUpdated()).thenReturn(true);
		when(mockMetric.getValue()).thenThrow(new RuntimeException("Test exception"));

		// When
		final Optional<Metric> result = counterMetricRecorder.doRecord();

		// Then
		assertTrue(result.isEmpty(), "Expected an empty optional when an exception occurs");
	}
}
