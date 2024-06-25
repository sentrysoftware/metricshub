package org.sentrysoftware.metricshub.hardware.threshold;

import static org.junit.jupiter.api.Assertions.*;

import java.util.HashMap;
import java.util.Map;

import org.junit.jupiter.api.Test;
import org.sentrysoftware.metricshub.engine.telemetry.Monitor;
import org.sentrysoftware.metricshub.engine.telemetry.metric.NumberMetric;

class FanMetricNormalizerTest {
	

	private static final long STRATEGY_TIME = System.currentTimeMillis();
	private static final String HOSTNAME = "hostname";
	private static final String HW_FAN_SPEED_LIMIT = "hw.fan.speed.limit";
	public static final String HW_FAN_SPEED_LIMIT_LIMIT_TYPE_LOW_CRITICAL_HW_TYPE_FAN =
		HW_FAN_SPEED_LIMIT + "{limit_type=\"low.critical\", hw.type=\"fan\"}";
	public static final String HW_FAN_SPEED_LIMIT_LIMIT_TYPE_LOW_DEGRADED_HW_TYPE_FAN =
		HW_FAN_SPEED_LIMIT + "{limit_type=\"low.degraded\", hw.type=\"fan\"}";

	@Test
	void testNormalizeSpeedLimitMetric() {
		{
			// When the low degraded and the low critical metrics are both absent
			final Monitor monitorWithoutHwFanSpeedMetric = Monitor
				.builder()
				.id("monitorOne")
				.type("fan")
				.metrics(
					new HashMap<>(
						Map.of(
							"hw.fan.utilization.limit{limit_type=\"max\"}",
							NumberMetric.builder().value(1.0).name("hw.fan.utilization.limit{limit_type=\"max\"}").build()
						)
					)
				)
				.build();

			new FanMetricNormalizer(STRATEGY_TIME, HOSTNAME).normalizeSpeedLimitMetric(monitorWithoutHwFanSpeedMetric, "hw.fan.speed.limit" , 500D , 0D);
			assertNull(
					monitorWithoutHwFanSpeedMetric.getMetric(HW_FAN_SPEED_LIMIT_LIMIT_TYPE_LOW_CRITICAL_HW_TYPE_FAN, NumberMetric.class)
			);
		}

		{
			final NumberMetric hwSpeedMetric = NumberMetric
				.builder()
				.value(1.0)
				.name("hw.speed{hw.type=\"fan\"}")
				.attributes(Map.of("hw.type", "fan"))
				.build();

			final Monitor monitorWithoutHwSpeedLimitMetric = Monitor
				.builder()
				.id("monitorOne")
				.type("fan")
				.metrics(new HashMap<>(Map.of("hw.speed{hw.type=\"cpu\"}",  hwSpeedMetric)))
				.build();
			new FanMetricNormalizer(STRATEGY_TIME, HOSTNAME).normalizeSpeedLimitMetric(monitorWithoutHwSpeedLimitMetric, "hw.fan.speed.limit" , 500D , 0D);
			assertNull(
					monitorWithoutHwSpeedLimitMetric.getMetric(HW_FAN_SPEED_LIMIT_LIMIT_TYPE_LOW_CRITICAL_HW_TYPE_FAN, NumberMetric.class)
			);
		}

		{
			final NumberMetric hwErrorsMetric = NumberMetric
				.builder()
				.value(1.0)
				.name("hw.errors{hw.type=\"cpu\"}")
				.attributes(Map.of("hw.type", "cpu"))
				.build();

			final Monitor monitorWithoutHwErrorsMetric = Monitor
				.builder()
				.id("monitorOne")
				.type("disk")
				.metrics(new HashMap<>(Map.of("hw.errors{hw.type=\"cpu\"}", hwErrorsMetric)))
				.build();

			new CpuMetricNormalizer(STRATEGY_TIME, HOSTNAME).normalizeErrorsLimitMetric(monitorWithoutHwErrorsMetric);
			assertNull(
				monitorWithoutHwErrorsMetric.getMetric(HW_ERRORS_LIMIT_LIMIT_TYPE_CRITICAL_HW_TYPE_CPU, NumberMetric.class)
			);
		}

		{
			final NumberMetric hwErrorsMetric = NumberMetric.builder().value(1.0).name("hw.errors").build();
			hwErrorsMetric.setCollectTime(STRATEGY_TIME);
			hwErrorsMetric.setPreviousCollectTime(STRATEGY_TIME - 1000 * 60 * 2);

			final Monitor monitorWithoutHwErrorsMetric = Monitor
				.builder()
				.id("monitorOne")
				.type("cpu")
				.metrics(new HashMap<>(Map.of("hw.errors", hwErrorsMetric)))
				.build();

			new CpuMetricNormalizer(STRATEGY_TIME, HOSTNAME).normalizeErrorsLimitMetric(monitorWithoutHwErrorsMetric);
			assertNotNull(
				monitorWithoutHwErrorsMetric.getMetric(HW_ERRORS_LIMIT_LIMIT_TYPE_CRITICAL_HW_TYPE_CPU, NumberMetric.class)
			);
		}

		{
			final NumberMetric hwErrorsMetric = NumberMetric.builder().value(1.0).name("hw.errors").build();
			hwErrorsMetric.setCollectTime(STRATEGY_TIME);
			hwErrorsMetric.setPreviousCollectTime(STRATEGY_TIME - 1000 * 60 * 2);
			final NumberMetric hwErrorsLimitMetric = NumberMetric
				.builder()
				.value(1.0)
				.name(HW_ERRORS_LIMIT_LIMIT_TYPE_CRITICAL_HW_TYPE_CPU)
				.attributes(Map.of("limit_type", "critical", "hw.type", "cpu"))
				.build();
			final Monitor monitorWithoutHwErrorsMetric = Monitor
				.builder()
				.id("monitorOne")
				.type("cpu")
				.metrics(
					new HashMap<>(
						Map.of("hw.errors", hwErrorsMetric, HW_ERRORS_LIMIT_LIMIT_TYPE_CRITICAL_HW_TYPE_CPU, hwErrorsLimitMetric)
					)
				)
				.build();

			new CpuMetricNormalizer(STRATEGY_TIME, HOSTNAME).normalizeErrorsLimitMetric(monitorWithoutHwErrorsMetric);
			assertNotNull(
				monitorWithoutHwErrorsMetric.getMetric(HW_ERRORS_LIMIT_LIMIT_TYPE_CRITICAL_HW_TYPE_CPU, NumberMetric.class)
			);
			assertNull(
				monitorWithoutHwErrorsMetric.getMetric(HW_ERRORS_LIMIT_LIMIT_TYPE_DEGRADED_HW_TYPE_CPU, NumberMetric.class)
			);
		}

		{
			final NumberMetric hwErrorsMetric = NumberMetric.builder().value(1.0).name("hw.errors").build();
			hwErrorsMetric.setCollectTime(STRATEGY_TIME);
			hwErrorsMetric.setPreviousCollectTime(STRATEGY_TIME - 1000 * 60 * 2);
			final NumberMetric hwErrorsLimitMetric = NumberMetric
				.builder()
				.value(1.0)
				.name(HW_ERRORS_LIMIT_LIMIT_TYPE_DEGRADED_HW_TYPE_CPU)
				.attributes(Map.of("limit_type", "degraded", "hw.type", "cpu"))
				.build();
			final Monitor monitorWithoutHwErrorsMetric = Monitor
				.builder()
				.id("monitorOne")
				.type("cpu")
				.metrics(
					new HashMap<>(
						Map.of("hw.errors", hwErrorsMetric, HW_ERRORS_LIMIT_LIMIT_TYPE_DEGRADED_HW_TYPE_CPU, hwErrorsLimitMetric)
					)
				)
				.build();

			new CpuMetricNormalizer(STRATEGY_TIME, HOSTNAME).normalizeErrorsLimitMetric(monitorWithoutHwErrorsMetric);
			assertNull(
				monitorWithoutHwErrorsMetric.getMetric(HW_ERRORS_LIMIT_LIMIT_TYPE_CRITICAL_HW_TYPE_CPU, NumberMetric.class)
			);
			assertNotNull(
				monitorWithoutHwErrorsMetric.getMetric(HW_ERRORS_LIMIT_LIMIT_TYPE_DEGRADED_HW_TYPE_CPU, NumberMetric.class)
			);
		}

		{
			final NumberMetric hwErrorsMetric = NumberMetric.builder().value(1.0).name("hw.errors").build();
			hwErrorsMetric.setCollectTime(STRATEGY_TIME);
			hwErrorsMetric.setPreviousCollectTime(STRATEGY_TIME - 1000 * 60 * 2);
			final NumberMetric hwErrorsLimitCriticalMetric = NumberMetric
				.builder()
				.value(2.0)
				.name(HW_ERRORS_LIMIT_LIMIT_TYPE_CRITICAL_HW_TYPE_CPU)
				.attributes(Map.of("limit_type", "critical", "hw.type", "cpu"))
				.build();
			final NumberMetric hwErrorsLimitDegradedMetric = NumberMetric
				.builder()
				.value(1.0)
				.name(HW_ERRORS_LIMIT_LIMIT_TYPE_DEGRADED_HW_TYPE_CPU)
				.attributes(Map.of("limit_type", "degraded", "hw.type", "cpu"))
				.build();

			final Monitor monitorWithoutHwErrorsMetric = Monitor
				.builder()
				.id("monitorOne")
				.type("cpu")
				.metrics(
					new HashMap<>(
						Map.of(
							"hw.errors",
							hwErrorsMetric,
							HW_ERRORS_LIMIT_LIMIT_TYPE_CRITICAL_HW_TYPE_CPU,
							hwErrorsLimitCriticalMetric,
							HW_ERRORS_LIMIT_LIMIT_TYPE_DEGRADED_HW_TYPE_CPU,
							hwErrorsLimitDegradedMetric
						)
					)
				)
				.build();

			new CpuMetricNormalizer(STRATEGY_TIME, HOSTNAME).normalizeErrorsLimitMetric(monitorWithoutHwErrorsMetric);
			assertEquals(
				2.0,
				monitorWithoutHwErrorsMetric
					.getMetric(HW_ERRORS_LIMIT_LIMIT_TYPE_CRITICAL_HW_TYPE_CPU, NumberMetric.class)
					.getValue()
			);
			assertEquals(
				1.0,
				monitorWithoutHwErrorsMetric
					.getMetric(HW_ERRORS_LIMIT_LIMIT_TYPE_DEGRADED_HW_TYPE_CPU, NumberMetric.class)
					.getValue()
			);
		}

		{
			final NumberMetric hwErrorsMetric = NumberMetric.builder().value(1.0).name("hw.errors").build();
			hwErrorsMetric.setCollectTime(STRATEGY_TIME);
			hwErrorsMetric.setPreviousCollectTime(STRATEGY_TIME - 1000 * 60 * 2);
			final NumberMetric hwErrorsLimitCriticalMetric = NumberMetric
				.builder()
				.value(1.0)
				.name(HW_ERRORS_LIMIT_LIMIT_TYPE_CRITICAL_HW_TYPE_CPU)
				.attributes(Map.of("limit_type", "critical", "hw.type", "cpu"))
				.build();
			final NumberMetric hwErrorsLimitDegradedMetric = NumberMetric
				.builder()
				.value(2.0)
				.name(HW_ERRORS_LIMIT_LIMIT_TYPE_DEGRADED_HW_TYPE_CPU)
				.attributes(Map.of("limit_type", "degraded", "hw.type", "cpu"))
				.build();

			final Monitor monitorWithoutHwErrorsMetric = Monitor
				.builder()
				.id("monitorOne")
				.type("cpu")
				.metrics(
					new HashMap<>(
						Map.of(
							"hw.errors",
							hwErrorsMetric,
							HW_ERRORS_LIMIT_LIMIT_TYPE_CRITICAL_HW_TYPE_CPU,
							hwErrorsLimitCriticalMetric,
							HW_ERRORS_LIMIT_LIMIT_TYPE_DEGRADED_HW_TYPE_CPU,
							hwErrorsLimitDegradedMetric
						)
					)
				)
				.build();

			new CpuMetricNormalizer(STRATEGY_TIME, HOSTNAME).normalizeErrorsLimitMetric(monitorWithoutHwErrorsMetric);
			assertEquals(
				2.0,
				monitorWithoutHwErrorsMetric
					.getMetric(HW_ERRORS_LIMIT_LIMIT_TYPE_CRITICAL_HW_TYPE_CPU, NumberMetric.class)
					.getValue()
			);
			assertEquals(
				1.0,
				monitorWithoutHwErrorsMetric
					.getMetric(HW_ERRORS_LIMIT_LIMIT_TYPE_DEGRADED_HW_TYPE_CPU, NumberMetric.class)
					.getValue()
			);
		}
	}*/
	}
}