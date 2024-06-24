package org.sentrysoftware.metricshub.hardware.threshold;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.sentrysoftware.metricshub.hardware.util.HwConstants.HW_ERRORS_LIMIT_LIMIT_TYPE_CRITICAL_HW_TYPE_CPU;
import static org.sentrysoftware.metricshub.hardware.util.HwConstants.HW_ERRORS_LIMIT_LIMIT_TYPE_DEGRADED_HW_TYPE_CPU;
import static org.sentrysoftware.metricshub.hardware.util.HwConstants.HW_ERRORS_LIMIT_LIMIT_TYPE_LOW_CRITICAL_HW_TYPE_CPU;
import static org.sentrysoftware.metricshub.hardware.util.HwConstants.HW_ERRORS_LIMIT_LIMIT_TYPE_LOW_DEGRADED_HW_TYPE_CPU;
import static org.sentrysoftware.metricshub.hardware.util.HwConstants.METRIC_DEGRADED_ATTRIBUTES;

import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.atomic.AtomicReference;
import org.junit.jupiter.api.Test;
import org.sentrysoftware.metricshub.engine.telemetry.Monitor;
import org.sentrysoftware.metricshub.engine.telemetry.metric.NumberMetric;

class CpuMetricNormalizerTest {

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

	@Test
	public void testNormalizeErrorsLimitMetric() {
		// When the degraded and the critical metrics are both absent
		final Monitor monitorWithoutDegradedAndCritical = Monitor
			.builder()
			.id("monitorOne")
			.metrics(
				new HashMap<>(
					Map.of(
						"hw.cpu.speed.limit{limit_type=\"max\"}",
						NumberMetric.builder().value(1.0).name("hw.cpu.speed.limit{limit_type=\"max\"}").build()
					)
				)
			)
			.build();
		final CpuMetricNormalizer cpuMetricNormalizer = new CpuMetricNormalizer();
		cpuMetricNormalizer.normalizeErrorsLimitMetric(monitorWithoutDegradedAndCritical);
		final NumberMetric criticalMetric = monitorWithoutDegradedAndCritical.getMetric(
			HW_ERRORS_LIMIT_LIMIT_TYPE_CRITICAL_HW_TYPE_CPU,
			NumberMetric.class
		);
		assertNotNull(criticalMetric);
		assertEquals(1.0, criticalMetric.getValue());

		// When the degraded and the critical metrics are both present: degraded metric value < critical metric value
		Monitor monitorWithDegradedAndCritical = Monitor
			.builder()
			.id("monitorOne")
			.metrics(
				new HashMap<>(
					Map.of(
						"hw.errors.limit{limit_type=\"degraded\", hw.type=\"cpu\"}",
						NumberMetric
							.builder()
							.value(-1.0)
							.name("hw.errors.limit{limit_type=\"degraded\", hw.type=\"cpu\"}")
							.build(),
						"hw.errors.limit{limit_type=\"critical\", hw.type=\"cpu\"}",
						NumberMetric.builder().value(1.0).name("hw.errors.limit{limit_type=\"critical\", hw.type=\"cpu\"}").build()
					)
				)
			)
			.build();
		cpuMetricNormalizer.normalizeErrorsLimitMetric(monitorWithDegradedAndCritical);
		NumberMetric criticalMonitorMetric = monitorWithDegradedAndCritical.getMetric(
			HW_ERRORS_LIMIT_LIMIT_TYPE_CRITICAL_HW_TYPE_CPU,
			NumberMetric.class
		);
		NumberMetric degradedMonitorMetric = monitorWithDegradedAndCritical.getMetric(
			HW_ERRORS_LIMIT_LIMIT_TYPE_DEGRADED_HW_TYPE_CPU,
			NumberMetric.class
		);
		assertNotNull(criticalMonitorMetric);
		assertNotNull(degradedMonitorMetric);
		assertEquals(-1.0, degradedMonitorMetric.getValue());
		assertEquals(1.0, criticalMetric.getValue());

		// When the degraded and the critical metrics are both present: degraded metric value > critical metric value
		monitorWithDegradedAndCritical =
			Monitor
				.builder()
				.id("monitorOne")
				.metrics(
					new HashMap<>(
						Map.of(
							"hw.errors.limit{limit_type=\"degraded\", hw.type=\"cpu\"}",
							NumberMetric
								.builder()
								.value(1.0)
								.name("hw.errors.limit{limit_type=\"degraded\", hw.type=\"cpu\"}")
								.build(),
							"hw.errors.limit{limit_type=\"critical\", hw.type=\"cpu\"}",
							NumberMetric
								.builder()
								.value(-1.0)
								.name("hw.errors.limit{limit_type=\"critical\", hw.type=\"cpu\"}")
								.build()
						)
					)
				)
				.build();
		cpuMetricNormalizer.normalizeErrorsLimitMetric(monitorWithDegradedAndCritical);
		criticalMonitorMetric =
			monitorWithDegradedAndCritical.getMetric(HW_ERRORS_LIMIT_LIMIT_TYPE_CRITICAL_HW_TYPE_CPU, NumberMetric.class);

		degradedMonitorMetric =
			monitorWithDegradedAndCritical.getMetric(HW_ERRORS_LIMIT_LIMIT_TYPE_DEGRADED_HW_TYPE_CPU, NumberMetric.class);
		assertNotNull(criticalMonitorMetric);
		assertNotNull(degradedMonitorMetric);
		assertEquals(-1.0, degradedMonitorMetric.getValue());
		assertEquals(1.0, criticalMetric.getValue());
	}

	@Test
	public void testNormalize() {
		final CpuMetricNormalizer cpuMetricNormalizer = new CpuMetricNormalizer();
		// When the critical metric is not available and the degraded metric is
		final Monitor monitorWithOnlyDegradedMetric = Monitor
			.builder()
			.id("monitorOne")
			.metrics(
				new HashMap<>(
					Map.of(
						"hw.errors.limit{limit_type=\"low.degraded\", hw.type=\"cpu\"}",
						NumberMetric
							.builder()
							.value(1.0)
							.name("hw.errors.limit{limit_type=\"low.degraded\", hw.type=\"cpu\"}")
							.build()
					)
				)
			)
			.build();
		cpuMetricNormalizer.normalize(monitorWithOnlyDegradedMetric);
		final NumberMetric criticalMonitorMetric = monitorWithOnlyDegradedMetric.getMetric(
			HW_ERRORS_LIMIT_LIMIT_TYPE_LOW_CRITICAL_HW_TYPE_CPU,
			NumberMetric.class
		);

		final NumberMetric degradedMonitorMetric = monitorWithOnlyDegradedMetric.getMetric(
			HW_ERRORS_LIMIT_LIMIT_TYPE_LOW_DEGRADED_HW_TYPE_CPU,
			NumberMetric.class
		);
		assertNotNull(criticalMonitorMetric);
		assertNotNull(degradedMonitorMetric);
		assertEquals(1.0, degradedMonitorMetric.getValue());
		assertEquals(0.9, criticalMonitorMetric.getValue());
	}

	@Test
	void tesIsMetricAvailable() {
		// Add metrics to the monitor
		final Monitor monitorWithDegradedAndCritical = Monitor
			.builder()
			.id("monitorOne")
			.metrics(
				new HashMap<>(
					Map.of(
						"hw.errors.limit{limit_type=\"degraded\", hw.type=\"cpu\"}",
						NumberMetric
							.builder()
							.value(-1.0)
							.name("hw.errors.limit{limit_type=\"degraded\", hw.type=\"cpu\"}")
							.build(),
						"hw.errors.limit{limit_type=\"critical\", hw.type=\"cpu\"}",
						NumberMetric.builder().value(1.0).name("hw.errors.limit{limit_type=\"critical\", hw.type=\"cpu\"}").build()
					)
				)
			)
			.build();
		final CpuMetricNormalizer cpuMetricNormalizer = new CpuMetricNormalizer();

		final AtomicReference<NumberMetric> matchingMetric = new AtomicReference<>();

		// The metric is available
		final boolean isDegradedMetricAvailable = cpuMetricNormalizer.isMetricAvailable(
			monitorWithDegradedAndCritical.getMetrics(),
			"hw.errors.limit",
			METRIC_DEGRADED_ATTRIBUTES,
			matchingMetric
		);
		assertTrue(isDegradedMetricAvailable);

		// The metric is not available
		final Map<String, String> SPEED_LIMIT_ATTRIBUTES = Map.of("limit_type", "max");
		final boolean isSpeedLimitAvailable = cpuMetricNormalizer.isMetricAvailable(
			monitorWithDegradedAndCritical.getMetrics(),
			"hw.cpu.speed.limit",
			SPEED_LIMIT_ATTRIBUTES,
			matchingMetric
		);
		assertFalse(isSpeedLimitAvailable);
	}
}
