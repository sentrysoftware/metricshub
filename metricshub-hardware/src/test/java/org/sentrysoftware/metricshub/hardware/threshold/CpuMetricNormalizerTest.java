package org.sentrysoftware.metricshub.hardware.threshold;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;
import static org.sentrysoftware.metricshub.hardware.util.HwConstants.HW_ERRORS_LIMIT_LIMIT_TYPE_CRITICAL_HW_TYPE_CPU;
import static org.sentrysoftware.metricshub.hardware.util.HwConstants.HW_ERRORS_LIMIT_LIMIT_TYPE_DEGRADED_HW_TYPE_CPU;
import static org.sentrysoftware.metricshub.hardware.util.HwConstants.HW_ERRORS_LIMIT_LIMIT_TYPE_LOW_CRITICAL_HW_TYPE_CPU;
import static org.sentrysoftware.metricshub.hardware.util.HwConstants.HW_ERRORS_LIMIT_LIMIT_TYPE_LOW_DEGRADED_HW_TYPE_CPU;
import java.util.HashMap;
import java.util.Map;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.MockedStatic;
import org.mockito.Mockito;
import org.sentrysoftware.metricshub.engine.telemetry.MetricFactory;
import org.sentrysoftware.metricshub.engine.telemetry.Monitor;
import org.sentrysoftware.metricshub.engine.telemetry.metric.AbstractMetric;
import org.sentrysoftware.metricshub.engine.telemetry.metric.NumberMetric;

public class CpuMetricNormalizerTest {

	@Test
	public void testContainsAllEntries() {
		final Map<String, String> firstMap = new HashMap<>();
		firstMap.put("key1", "value1");
		firstMap.put("key2", "value2");

		final Map<String, String> secondMap = new HashMap<>();
		secondMap.put("key1", "value1");

		assertTrue(AbstractMetricNormalizer.containsAllEntries(firstMap, secondMap));

		secondMap.put("key2", "differentValue");
		assertFalse(AbstractMetricNormalizer.containsAllEntries(firstMap, secondMap));
	}
/**
	@Test
	public void testIsMetricAvailable() {
		final String metricName = "metric.name{attribute1=value1,attribute2=value2}";
		final String prefix = "metric.name";
		final Map<String, String> attributes = Map.of("attribute1", "value1", "attribute2", "value2");

		// Mocking the static methods of MetricFactory
		try (MockedStatic<MetricFactory> mockedFactory = Mockito.mockStatic(MetricFactory.class)) {
			mockedFactory.when(() -> MetricFactory.extractName(metricName)).thenReturn(prefix);
			mockedFactory.when(() -> MetricFactory.extractAttributesFromMetricName(metricName)).thenReturn(attributes);
			final CpuMetricNormalizer cpuMetricNormalizer = new CpuMetricNormalizer();
			final boolean result = cpuMetricNormalizer.isMetricAvailable(metricName, prefix, attributes);
			assertTrue(result);
		}
	}
**/
	@Test
	public void testNormalizeErrorsLimitMetric() {
		// When the degraded and the critical metrics are both absent
		final Monitor monitorWithoutDegradedAndCritical = Monitor.builder()
				.id("monitorOne")
				.metrics(new HashMap<>(Map.of("hw.cpu.speed.limit{limit_type=\"max\"}",
						NumberMetric.builder().value(1.0).name("hw.cpu.speed.limit{limit_type=\"max\"}").build())))
				.build();
		final CpuMetricNormalizer cpuMetricNormalizer = new CpuMetricNormalizer();
		cpuMetricNormalizer.normalizeErrorsLimitMetric(monitorWithoutDegradedAndCritical);
		final NumberMetric criticalMetric = monitorWithoutDegradedAndCritical.getMetric(HW_ERRORS_LIMIT_LIMIT_TYPE_CRITICAL_HW_TYPE_CPU,
				NumberMetric.class);
		assertNotNull(criticalMetric);
		assertEquals(1.0, criticalMetric.getValue());

		// When the degraded and the critical metrics are both present: degraded metric value < critical metric value
		Monitor monitorWithDegradedAndCritical = Monitor.builder()
				.id("monitorOne")
				.metrics(new HashMap<>(Map.of("hw.errors.limit{limit_type=\"degraded\", hw.type=\"cpu\"}",
						NumberMetric.builder().value(-1.0).name("hw.errors.limit{limit_type=\"degraded\", hw.type=\"cpu\"}").build(),
						"hw.errors.limit{limit_type=\"critical\", hw.type=\"cpu\"}",
						NumberMetric.builder().value(1.0).name("hw.errors.limit{limit_type=\"critical\", hw.type=\"cpu\"}")
								.build())))
				.build();
		cpuMetricNormalizer.normalizeErrorsLimitMetric(monitorWithDegradedAndCritical);
		NumberMetric criticalMonitorMetric = monitorWithDegradedAndCritical.getMetric(HW_ERRORS_LIMIT_LIMIT_TYPE_CRITICAL_HW_TYPE_CPU,
				NumberMetric.class);
		NumberMetric degradedMonitorMetric = monitorWithDegradedAndCritical.getMetric(HW_ERRORS_LIMIT_LIMIT_TYPE_DEGRADED_HW_TYPE_CPU,
				NumberMetric.class);
		assertNotNull(criticalMonitorMetric);
		assertNotNull(degradedMonitorMetric);
		assertEquals(-1.0, degradedMonitorMetric.getValue());
		assertEquals(1.0, criticalMetric.getValue());

		// When the degraded and the critical metrics are both present: degraded metric value > critical metric value
		monitorWithDegradedAndCritical = Monitor.builder()
				.id("monitorOne")
				.metrics(new HashMap<>(Map.of("hw.errors.limit{limit_type=\"degraded\", hw.type=\"cpu\"}",
						NumberMetric.builder().value(1.0).name("hw.errors.limit{limit_type=\"degraded\", hw.type=\"cpu\"}").build(),
						"hw.errors.limit{limit_type=\"critical\", hw.type=\"cpu\"}",
						NumberMetric.builder().value(-1.0).name("hw.errors.limit{limit_type=\"critical\", hw.type=\"cpu\"}")
								.build())))
				.build();
		cpuMetricNormalizer.normalizeErrorsLimitMetric(monitorWithDegradedAndCritical);
		criticalMonitorMetric = monitorWithDegradedAndCritical.getMetric(HW_ERRORS_LIMIT_LIMIT_TYPE_CRITICAL_HW_TYPE_CPU,
				NumberMetric.class);

		degradedMonitorMetric = monitorWithDegradedAndCritical.getMetric(HW_ERRORS_LIMIT_LIMIT_TYPE_DEGRADED_HW_TYPE_CPU,
				NumberMetric.class);
		assertNotNull(criticalMonitorMetric);
		assertNotNull(degradedMonitorMetric);
		assertEquals(-1.0, degradedMonitorMetric.getValue());
		assertEquals(1.0, criticalMetric.getValue());
	}

	@Test
	public void testNormalize() {
		final CpuMetricNormalizer cpuMetricNormalizer = new CpuMetricNormalizer();
		// When the critical metric is not available and the degraded metric is
		final Monitor monitorWithOnlyDegradedMetric = Monitor.builder()
				.id("monitorOne")
				.metrics(new HashMap<>(Map.of("hw.errors.limit{limit_type=\"low.degraded\", hw.type=\"cpu\"}",
						NumberMetric.builder().value(1.0).name("hw.errors.limit{limit_type=\"low.degraded\", hw.type=\"cpu\"}").build())))
				.build();
		cpuMetricNormalizer.normalize(monitorWithOnlyDegradedMetric);
		final NumberMetric criticalMonitorMetric = monitorWithOnlyDegradedMetric.getMetric(HW_ERRORS_LIMIT_LIMIT_TYPE_LOW_CRITICAL_HW_TYPE_CPU,
				NumberMetric.class);

		final NumberMetric degradedMonitorMetric = monitorWithOnlyDegradedMetric.getMetric(HW_ERRORS_LIMIT_LIMIT_TYPE_LOW_DEGRADED_HW_TYPE_CPU,
				NumberMetric.class);
		assertNotNull(criticalMonitorMetric);
		assertNotNull(degradedMonitorMetric);
		assertEquals(1.0, degradedMonitorMetric.getValue());
		assertEquals(0.9, criticalMonitorMetric.getValue());
	}
}
