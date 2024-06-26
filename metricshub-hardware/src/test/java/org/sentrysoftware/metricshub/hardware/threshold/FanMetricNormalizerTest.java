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
				.name("hw.speed{hw.type=\"fan\"}")
				.attributes(Map.of("hw.type", "fan"))
				.build();

			final Monitor monitorDiskType = Monitor
				.builder()
				.id("monitorOne")
				.type("disk")
				.metrics(new HashMap<>(Map.of("hw.speed{hw.type=\"fan\"}", hwErrorsMetric)))
				.build();

			new CpuMetricNormalizer(STRATEGY_TIME, HOSTNAME).normalizeErrorsLimitMetric(monitorDiskType);
			assertNull(
				monitorDiskType.getMetric(HW_FAN_SPEED_LIMIT_LIMIT_TYPE_LOW_CRITICAL_HW_TYPE_FAN, NumberMetric.class)
			);
		}

	}
	
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
		
			final Monitor monitorWithhwFanSpeedMetric = Monitor
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
		
			new FanMetricNormalizer(STRATEGY_TIME, HOSTNAME).normalize(monitorWithhwFanSpeedMetric);
			assertEquals(
				1.0,
				monitorWithhwFanSpeedMetric
					.getMetric(HW_FAN_SPEED_LIMIT_LIMIT_TYPE_LOW_CRITICAL_HW_TYPE_FAN, NumberMetric.class)
					.getValue()
			);
			assertEquals(
				2.0,
				monitorWithhwFanSpeedMetric
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
		
			final Monitor monitorWithhwFanSpeedMetric = Monitor
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
		
			new FanMetricNormalizer(STRATEGY_TIME, HOSTNAME).normalize(monitorWithhwFanSpeedMetric);
			assertEquals(
				1.0,
				monitorWithhwFanSpeedMetric
					.getMetric(HW_FAN_SPEED_LIMIT_LIMIT_TYPE_LOW_CRITICAL_HW_TYPE_FAN, NumberMetric.class)
					.getValue()
			);
			assertEquals(
				2.0,
				monitorWithhwFanSpeedMetric
					.getMetric(HW_FAN_SPEED_LIMIT_LIMIT_TYPE_LOW_DEGRADED_HW_TYPE_FAN, NumberMetric.class)
					.getValue()
			);
		}
		
		{
			final NumberMetric hwFanSpeedMetric = NumberMetric.builder().value(1.0).name("hw.fan.speed").build();
			hwFanSpeedMetric.setCollectTime(STRATEGY_TIME);
			hwFanSpeedMetric.setPreviousCollectTime(STRATEGY_TIME - 1000 * 60 * 2);

			final Monitor monitorWithoutHwhwFanSpeedLimit = Monitor
				.builder()
				.id("monitorOne")
				.type("fan")
				.metrics(new HashMap<>(Map.of("hw.fan.speed", hwFanSpeedMetric)))
				.build();

			new FanMetricNormalizer(STRATEGY_TIME, HOSTNAME).normalize(monitorWithoutHwhwFanSpeedLimit);
			
			assertEquals(0.0,
					monitorWithoutHwhwFanSpeedLimit.getMetric(HW_FAN_SPEED_LIMIT_LIMIT_TYPE_LOW_CRITICAL_HW_TYPE_FAN, NumberMetric.class).getValue()
			);
			
			assertEquals(500,
					monitorWithoutHwhwFanSpeedLimit.getMetric(HW_FAN_SPEED_LIMIT_LIMIT_TYPE_LOW_DEGRADED_HW_TYPE_FAN, NumberMetric.class).getValue()
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
								hwFanSpeedLimitCriticalMetric
							)
						)
					)
					.build();
			new FanMetricNormalizer(STRATEGY_TIME, HOSTNAME).normalize(monitorWithhwFanSpeedLimitMetric);
			assertEquals(2.2,
					monitorWithhwFanSpeedLimitMetric.getMetric(HW_FAN_SPEED_LIMIT_LIMIT_TYPE_LOW_DEGRADED_HW_TYPE_FAN, NumberMetric.class).getValue()
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
			final Monitor monitorWithhwFanSpeedLimitMetric = Monitor
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
			new FanMetricNormalizer(STRATEGY_TIME, HOSTNAME).normalize(monitorWithhwFanSpeedLimitMetric);
			assertEquals(1.8,
					monitorWithhwFanSpeedLimitMetric.getMetric(HW_FAN_SPEED_LIMIT_LIMIT_TYPE_LOW_CRITICAL_HW_TYPE_FAN, NumberMetric.class).getValue()
				);
			
		}	
		
	}
}