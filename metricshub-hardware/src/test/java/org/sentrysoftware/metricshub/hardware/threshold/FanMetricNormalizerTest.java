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

	private static final String HW_FAN_SPEED_RATIO_LIMIT = "hw.fan.speed_ratio.limit";
	public static final String HW_FAN_SPEED_RATIO_LIMIT_LIMIT_TYPE_LOW_CRITICAL_HW_TYPE_FAN =
		HW_FAN_SPEED_RATIO_LIMIT + "{limit_type=\"low.critical\", hw.type=\"fan\"}";
	public static final String HW_FAN_SPEED_RATIO_LIMIT_LIMIT_TYPE_LOW_DEGRADED_HW_TYPE_FAN =
		HW_FAN_SPEED_RATIO_LIMIT + "{limit_type=\"low.degraded\", hw.type=\"fan\"}";

	@Test
	void testNormalize() {
		{
			final NumberMetric hwFanSpeedMetric = NumberMetric.builder().value(1.0).name("hw.fan.speed").build();
			hwFanSpeedMetric.setCollectTime(STRATEGY_TIME);
			hwFanSpeedMetric.setPreviousCollectTime(STRATEGY_TIME - 1000 * 60 * 2);
			final NumberMetric hwFanSpeedLimitCriticalMetric = NumberMetric
				.builder()
				.value(2.0)
				.name(HW_FAN_SPEED_LIMIT_LIMIT_TYPE_LOW_CRITICAL_HW_TYPE_FAN)
				.attributes(Map.of("limit_type", "low.critical", "hw.type", "fan"))
				.build();
			final NumberMetric hwFanSpeedLimitDegradedMetric = NumberMetric
				.builder()
				.value(1.0)
				.name(HW_FAN_SPEED_LIMIT_LIMIT_TYPE_LOW_DEGRADED_HW_TYPE_FAN)
				.attributes(Map.of("limit_type", "low.degraded", "hw.type", "fan"))
				.build();

			final Monitor monitorWithhwFanSpeedLimitMetric = Monitor
				.builder()
				.id("monitorOne")
				.type("fan")
				.metrics(
					new HashMap<>(
						Map.of(
							"hw.fan.speed",
							hwFanSpeedMetric,
							HW_FAN_SPEED_LIMIT_LIMIT_TYPE_LOW_CRITICAL_HW_TYPE_FAN,
							hwFanSpeedLimitCriticalMetric,
							HW_FAN_SPEED_LIMIT_LIMIT_TYPE_LOW_DEGRADED_HW_TYPE_FAN,
							hwFanSpeedLimitDegradedMetric
						)
					)
				)
				.build();

			new FanMetricNormalizer(STRATEGY_TIME, HOSTNAME).normalize(monitorWithhwFanSpeedLimitMetric);
			assertEquals(
				1.0,
				monitorWithhwFanSpeedLimitMetric
					.getMetric(HW_FAN_SPEED_LIMIT_LIMIT_TYPE_LOW_CRITICAL_HW_TYPE_FAN, NumberMetric.class)
					.getValue()
			);
			assertEquals(
				2.0,
				monitorWithhwFanSpeedLimitMetric
					.getMetric(HW_FAN_SPEED_LIMIT_LIMIT_TYPE_LOW_DEGRADED_HW_TYPE_FAN, NumberMetric.class)
					.getValue()
			);
		}

		{
			final NumberMetric hwFanSpeedMetric = NumberMetric.builder().value(1.0).name("hw.fan.speed").build();
			hwFanSpeedMetric.setCollectTime(STRATEGY_TIME);
			hwFanSpeedMetric.setPreviousCollectTime(STRATEGY_TIME - 1000 * 60 * 2);
			final NumberMetric hwFanSpeedLimitCriticalMetric = NumberMetric
				.builder()
				.value(1.0)
				.name(HW_FAN_SPEED_LIMIT_LIMIT_TYPE_LOW_CRITICAL_HW_TYPE_FAN)
				.attributes(Map.of("limit_type", "low.critical", "hw.type", "fan"))
				.build();
			final NumberMetric hwFanSpeedLimitDegradedMetric = NumberMetric
				.builder()
				.value(2.0)
				.name(HW_FAN_SPEED_LIMIT_LIMIT_TYPE_LOW_DEGRADED_HW_TYPE_FAN)
				.attributes(Map.of("limit_type", "low.degraded", "hw.type", "fan"))
				.build();

			final Monitor monitorWithHwFanSpeedLimitMetric = Monitor
				.builder()
				.id("monitorOne")
				.type("fan")
				.metrics(
					new HashMap<>(
						Map.of(
							"hw.fan.speed",
							hwFanSpeedMetric,
							HW_FAN_SPEED_LIMIT_LIMIT_TYPE_LOW_CRITICAL_HW_TYPE_FAN,
							hwFanSpeedLimitCriticalMetric,
							HW_FAN_SPEED_LIMIT_LIMIT_TYPE_LOW_DEGRADED_HW_TYPE_FAN,
							hwFanSpeedLimitDegradedMetric
						)
					)
				)
				.build();

			new FanMetricNormalizer(STRATEGY_TIME, HOSTNAME).normalize(monitorWithHwFanSpeedLimitMetric);
			assertEquals(
				1.0,
				monitorWithHwFanSpeedLimitMetric
					.getMetric(HW_FAN_SPEED_LIMIT_LIMIT_TYPE_LOW_CRITICAL_HW_TYPE_FAN, NumberMetric.class)
					.getValue()
			);
			assertEquals(
				2.0,
				monitorWithHwFanSpeedLimitMetric
					.getMetric(HW_FAN_SPEED_LIMIT_LIMIT_TYPE_LOW_DEGRADED_HW_TYPE_FAN, NumberMetric.class)
					.getValue()
			);
		}

		{
			final NumberMetric hwFanSpeedMetric = NumberMetric.builder().value(1.0).name("hw.fan.speed").build();
			hwFanSpeedMetric.setCollectTime(STRATEGY_TIME);
			hwFanSpeedMetric.setPreviousCollectTime(STRATEGY_TIME - 1000 * 60 * 2);

			final Monitor monitorWithoutHwFanSpeedLimit = Monitor
				.builder()
				.id("monitorOne")
				.type("fan")
				.metrics(new HashMap<>(Map.of("hw.fan.speed", hwFanSpeedMetric)))
				.build();

			new FanMetricNormalizer(STRATEGY_TIME, HOSTNAME).normalize(monitorWithoutHwFanSpeedLimit);

			assertEquals(
				0.0,
				monitorWithoutHwFanSpeedLimit
					.getMetric(HW_FAN_SPEED_LIMIT_LIMIT_TYPE_LOW_CRITICAL_HW_TYPE_FAN, NumberMetric.class)
					.getValue()
			);

			assertEquals(
				500,
				monitorWithoutHwFanSpeedLimit
					.getMetric(HW_FAN_SPEED_LIMIT_LIMIT_TYPE_LOW_DEGRADED_HW_TYPE_FAN, NumberMetric.class)
					.getValue()
			);
		}

		{
			final NumberMetric hwFanSpeedMetric = NumberMetric.builder().value(1.0).name("hw.fan.speed").build();
			hwFanSpeedMetric.setCollectTime(STRATEGY_TIME);
			hwFanSpeedMetric.setPreviousCollectTime(STRATEGY_TIME - 1000 * 60 * 2);
			final NumberMetric hwFanSpeedLimitCriticalMetric = NumberMetric
				.builder()
				.value(2.0)
				.name(HW_FAN_SPEED_LIMIT_LIMIT_TYPE_LOW_CRITICAL_HW_TYPE_FAN)
				.attributes(Map.of("limit_type", "low.critical", "hw.type", "fan"))
				.build();
			final Monitor monitorWithHwFanSpeedLimitMetric = Monitor
				.builder()
				.id("monitorOne")
				.type("fan")
				.metrics(
					new HashMap<>(
						Map.of(
							"hw.fan.speed",
							hwFanSpeedMetric,
							HW_FAN_SPEED_LIMIT_LIMIT_TYPE_LOW_CRITICAL_HW_TYPE_FAN,
							hwFanSpeedLimitCriticalMetric
						)
					)
				)
				.build();
			new FanMetricNormalizer(STRATEGY_TIME, HOSTNAME).normalize(monitorWithHwFanSpeedLimitMetric);
			assertEquals(
				2.2,
				monitorWithHwFanSpeedLimitMetric
					.getMetric(HW_FAN_SPEED_LIMIT_LIMIT_TYPE_LOW_DEGRADED_HW_TYPE_FAN, NumberMetric.class)
					.getValue()
			);
		}

		{
			final NumberMetric hwFanSpeedMetric = NumberMetric.builder().value(1.0).name("hw.fan.speed").build();
			hwFanSpeedMetric.setCollectTime(STRATEGY_TIME);
			hwFanSpeedMetric.setPreviousCollectTime(STRATEGY_TIME - 1000 * 60 * 2);
			final NumberMetric hwFanSpeedLimitDegradedMetric = NumberMetric
				.builder()
				.value(2.0)
				.name(HW_FAN_SPEED_LIMIT_LIMIT_TYPE_LOW_DEGRADED_HW_TYPE_FAN)
				.attributes(Map.of("limit_type", "low.degraded", "hw.type", "fan"))
				.build();
			final Monitor monitorWithHwFanSpeedLimitMetric = Monitor
				.builder()
				.id("monitorOne")
				.type("fan")
				.metrics(
					new HashMap<>(
						Map.of(
							"hw.fan.speed",
							hwFanSpeedMetric,
							HW_FAN_SPEED_LIMIT_LIMIT_TYPE_LOW_DEGRADED_HW_TYPE_FAN,
							hwFanSpeedLimitDegradedMetric
						)
					)
				)
				.build();
			new FanMetricNormalizer(STRATEGY_TIME, HOSTNAME).normalize(monitorWithHwFanSpeedLimitMetric);
			assertEquals(
				1.8,
				monitorWithHwFanSpeedLimitMetric
					.getMetric(HW_FAN_SPEED_LIMIT_LIMIT_TYPE_LOW_CRITICAL_HW_TYPE_FAN, NumberMetric.class)
					.getValue()
			);
		}

		{
			final NumberMetric hwFanSpeedRatioMetric = NumberMetric.builder().value(1.0).name("hw.fan.speed_ratio").build();
			hwFanSpeedRatioMetric.setCollectTime(STRATEGY_TIME);
			hwFanSpeedRatioMetric.setPreviousCollectTime(STRATEGY_TIME - 1000 * 60 * 2);

			final Monitor monitorWithoutHwFanSpeedRatioLimit = Monitor
				.builder()
				.id("monitorOne")
				.type("fan")
				.metrics(new HashMap<>(Map.of("hw.fan.speed_ratio", hwFanSpeedRatioMetric)))
				.build();

			new FanMetricNormalizer(STRATEGY_TIME, HOSTNAME).normalize(monitorWithoutHwFanSpeedRatioLimit);

			assertEquals(
				0.0,
				monitorWithoutHwFanSpeedRatioLimit
					.getMetric(HW_FAN_SPEED_RATIO_LIMIT_LIMIT_TYPE_LOW_CRITICAL_HW_TYPE_FAN, NumberMetric.class)
					.getValue()
			);

			assertEquals(
				0.05,
				monitorWithoutHwFanSpeedRatioLimit
					.getMetric(HW_FAN_SPEED_RATIO_LIMIT_LIMIT_TYPE_LOW_DEGRADED_HW_TYPE_FAN, NumberMetric.class)
					.getValue()
			);
		}

		{
			final NumberMetric hwFanSpeeRatiodMetric = NumberMetric.builder().value(1.0).name("hw.fan.speed_ratio").build();
			hwFanSpeeRatiodMetric.setCollectTime(STRATEGY_TIME);
			hwFanSpeeRatiodMetric.setPreviousCollectTime(STRATEGY_TIME - 1000 * 60 * 2);
			final NumberMetric hwFanSpeedLimitCriticalMetric = NumberMetric
				.builder()
				.value(2.0)
				.name(HW_FAN_SPEED_RATIO_LIMIT_LIMIT_TYPE_LOW_CRITICAL_HW_TYPE_FAN)
				.attributes(Map.of("limit_type", "low.critical", "hw.type", "fan"))
				.build();
			final Monitor monitorWithHwFanSpeedRatioLimitMetric = Monitor
				.builder()
				.id("monitorOne")
				.type("fan")
				.metrics(
					new HashMap<>(
						Map.of(
							"hw.fan.speed_ratio",
							hwFanSpeeRatiodMetric,
							HW_FAN_SPEED_RATIO_LIMIT_LIMIT_TYPE_LOW_CRITICAL_HW_TYPE_FAN,
							hwFanSpeedLimitCriticalMetric
						)
					)
				)
				.build();
			new FanMetricNormalizer(STRATEGY_TIME, HOSTNAME).normalize(monitorWithHwFanSpeedRatioLimitMetric);
			assertEquals(
				2.2,
				monitorWithHwFanSpeedRatioLimitMetric
					.getMetric(HW_FAN_SPEED_RATIO_LIMIT_LIMIT_TYPE_LOW_DEGRADED_HW_TYPE_FAN, NumberMetric.class)
					.getValue()
			);
		}

		{
			final NumberMetric hwFanSpeedRatioMetric = NumberMetric.builder().value(1.0).name("hw.fan.speed_ratio").build();
			hwFanSpeedRatioMetric.setCollectTime(STRATEGY_TIME);
			hwFanSpeedRatioMetric.setPreviousCollectTime(STRATEGY_TIME - 1000 * 60 * 2);

			final Monitor monitorWithoutHwFanSpeedRatioLimit = Monitor
				.builder()
				.id("monitorOne")
				.type("fan")
				.metrics(new HashMap<>(Map.of("hw.fan.speed_ratio", hwFanSpeedRatioMetric)))
				.build();

			new FanMetricNormalizer(STRATEGY_TIME, HOSTNAME).normalize(monitorWithoutHwFanSpeedRatioLimit);

			assertEquals(
				0.0,
				monitorWithoutHwFanSpeedRatioLimit
					.getMetric(HW_FAN_SPEED_RATIO_LIMIT_LIMIT_TYPE_LOW_CRITICAL_HW_TYPE_FAN, NumberMetric.class)
					.getValue()
			);

			assertEquals(
				0.05,
				monitorWithoutHwFanSpeedRatioLimit
					.getMetric(HW_FAN_SPEED_RATIO_LIMIT_LIMIT_TYPE_LOW_DEGRADED_HW_TYPE_FAN, NumberMetric.class)
					.getValue()
			);
		}
	}
}
