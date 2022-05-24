package com.sentrysoftware.hardware.agent.service.opentelemetry.mapping;

import static com.sentrysoftware.hardware.agent.service.opentelemetry.mapping.MappingConstants.*;
import static com.sentrysoftware.hardware.agent.service.opentelemetry.mapping.MappingConstants.OK_STATUS_PREDICATE;
import static com.sentrysoftware.hardware.agent.service.opentelemetry.mapping.MappingConstants.STATE_ATTRIBUTE_KEY;

import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;

import com.sentrysoftware.hardware.agent.dto.metric.MetricInfo;
import com.sentrysoftware.hardware.agent.dto.metric.StaticIdentifyingAttribute;
import com.sentrysoftware.matrix.common.meta.monitor.Battery;
import com.sentrysoftware.matrix.common.meta.monitor.IMetaMonitor;

import lombok.AccessLevel;
import lombok.NoArgsConstructor;

@NoArgsConstructor(access = AccessLevel.PRIVATE)
public class BatteryMapping {

	private static final String BATTERY_STATUS_METRIC_NAME = "hw.battery.status";

	/**
	 * Build battery metrics map
	 *
	 * @return {@link Map} where the metrics are indexed by the matrix parameter name
	 */
	static Map<String, List<MetricInfo>> buildBatteryMetricsMapping() {
		final Map<String, List<MetricInfo>> map = new TreeMap<>(String.CASE_INSENSITIVE_ORDER);

		map.put(
			IMetaMonitor.STATUS.getName(),
			List.of(
				MetricInfo
					.builder()
					.name(BATTERY_STATUS_METRIC_NAME)
					.description("Whether the battery status is ok or not.")
					.identifyingAttribute(
						StaticIdentifyingAttribute
							.builder()
							.key(STATE_ATTRIBUTE_KEY)
							.value(OK_ATTRIBUTE_VALUE)
							.build()
						)
					.predicate(OK_STATUS_PREDICATE)
					.build(),
				MetricInfo
					.builder()
					.name(BATTERY_STATUS_METRIC_NAME)
					.description("Whether the battery status is degraded or not.")
					.identifyingAttribute(
						StaticIdentifyingAttribute
							.builder()
							.key(STATE_ATTRIBUTE_KEY)
							.value(DEGRADED_ATTRIBUTE_VALUE)
							.build()
						)
					.predicate(DEGRADED_STATUS_PREDICATE)
					.build(),
				MetricInfo
					.builder()
					.name(BATTERY_STATUS_METRIC_NAME)
					.description("Whether the battery status is failed or not.")
					.identifyingAttribute(
						StaticIdentifyingAttribute
							.builder()
							.key(STATE_ATTRIBUTE_KEY)
							.value(FAILED_ATTRIBUTE_VALUE)
							.build()
						)
					.predicate(FAILED_STATUS_PREDICATE)
					.build()
			)
		);

		map.put(
			IMetaMonitor.PRESENT.getName(), 
			Collections.singletonList(
				MetricInfo
					.builder()
					.name(BATTERY_STATUS_METRIC_NAME)
					.description("Whether the battery is found or not.")
					.identifyingAttribute(
							StaticIdentifyingAttribute
								.builder()
								.key(STATE_ATTRIBUTE_KEY)
								.value(PRESENT_ATTRIBUTE_VALUE)
								.build()
					)
					.predicate(PRESENT_PREDICATE)
					.build()
			)
		);

		map.put(
			Battery.CHARGE.getName(),
			Collections.singletonList(
				MetricInfo
					.builder()
					.name("hw.battery.charge")
					.factor(0.01)
					.unit("1")
					.description("Battery charge ratio.")
					.build()
			)
		);

		map.put(
			Battery.TIME_LEFT.getName(),
			Collections.singletonList(
				MetricInfo
					.builder()
					.name("hw.battery.time_left")
					.unit(SECONDS_UNIT)
					.description("Number of seconds left before recharging the battery.")
					.identifyingAttribute(
						StaticIdentifyingAttribute
							.builder()
							.key(STATE_ATTRIBUTE_KEY)
							.value("discharging")
							.build()
					)
					.build()
			)
		);

		return map;
	}
}
