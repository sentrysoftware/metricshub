package com.sentrysoftware.hardware.agent.service.opentelemetry.mapping;

import static com.sentrysoftware.hardware.agent.service.opentelemetry.mapping.MappingConstants.*;
import static com.sentrysoftware.matrix.common.helpers.HardwareConstants.ERROR_COUNT_ALARM_THRESHOLD;
import static com.sentrysoftware.matrix.common.helpers.HardwareConstants.ERROR_COUNT_WARNING_THRESHOLD;

import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;

import com.sentrysoftware.hardware.agent.dto.metric.MetricInfo;
import com.sentrysoftware.hardware.agent.dto.metric.StaticIdentifyingAttribute;
import com.sentrysoftware.hardware.agent.dto.metric.MetricInfo.MetricType;
import com.sentrysoftware.matrix.common.meta.monitor.IMetaMonitor;
import com.sentrysoftware.matrix.common.meta.monitor.Robotics;

import lombok.AccessLevel;
import lombok.NoArgsConstructor;

@NoArgsConstructor(access = AccessLevel.PRIVATE)
public class RoboticsMapping {

	private static final String ROBOTICS_STATUS_METRIC_NAME = "hw.robotics.status";

	/**
	 * Build robotics metrics map
	 *
	 * @return  {@link Map} where the metrics are indexed by the matrix parameter name
	 */
	static Map<String, List<MetricInfo>> buildRoboticsMetricsMapping() {
		final Map<String, List<MetricInfo>> map = new TreeMap<>(String.CASE_INSENSITIVE_ORDER);

		map.put(
			IMetaMonitor.STATUS.getName(),
			List.of(
				MetricInfo
					.builder()
					.name(ROBOTICS_STATUS_METRIC_NAME)
					.description("Whether the robotics status is ok or not.")
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
					.name(ROBOTICS_STATUS_METRIC_NAME)
					.description("Whether the robotics status is degraded or not.")
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
					.name(ROBOTICS_STATUS_METRIC_NAME)
					.description("Whether the robotics status is failed or not.")
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
					.name(ROBOTICS_STATUS_METRIC_NAME)
					.description("Whether the robotics is found or not.")
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
			Robotics.MOVE_COUNT.getName(),
			Collections.singletonList(
				MetricInfo
					.builder()
					.name("hw.robotics.moves")
					.unit(MOVES_UNIT)
					.type(MetricType.COUNTER)
					.description("Number of moves made by the robotics since the start of the Hardware Sentry Agent.")
					.build()
			)
		);

		map.put(
			IMetaMonitor.ERROR_COUNT.getName(),
			Collections.singletonList(
				MetricInfo
					.builder()
					.name("hw.robotics.errors")
					.unit(ERRORS_UNIT)
					.type(MetricType.COUNTER)
					.description("Number of errors encountered by the robotics since the start of the Hardware Sentry Agent.")
					.build()
			)
		);

		map.put(
			IMetaMonitor.ENERGY.getName(),
			Collections.singletonList(
				MetricInfo
					.builder()
					.name("hw.robotics.energy")
					.unit(JOULES_UNIT)
					.type(MetricType.COUNTER)
					.description("Energy consumed by the robotics since the start of the Hardware Sentry Agent.")
					.build()
			)
		);

		map.put(
			IMetaMonitor.POWER_CONSUMPTION.getName(),
			Collections.singletonList(
				MetricInfo
					.builder()
					.name("hw.robotics.power")
					.unit(WATTS_UNIT)
					.description("Energy consumed by the robotics.")
					.build()
			)
		);

		return map;
	}

	/**
	 * Create Robotics Metadata to metrics map
	 * 
	 * @return {@link Map} of {@link MetricInfo} instances indexed by the matrix parameter names
	 */
	static Map<String, List<MetricInfo>> roboticsMetadataToMetrics() {
		final Map<String, List<MetricInfo>> map = new TreeMap<>(String.CASE_INSENSITIVE_ORDER);

		map.put(
			ERROR_COUNT_WARNING_THRESHOLD,
			Collections.singletonList(
				MetricInfo
					.builder()
					.name("hw.robotics.errors_warning")
					.description(WARNING_THRESHOLD_OF_ERRORS)
					.unit(ERRORS_UNIT)
					.build()
			)
		);

		map.put(
			ERROR_COUNT_ALARM_THRESHOLD,
			Collections.singletonList(
				MetricInfo
					.builder()
					.name("hw.robotics.errors_alarm")
					.description(ALARM_THRESHOLD_OF_ERRORS)
					.unit(ERRORS_UNIT)
					.build()
			)
		);

		return map;
	}
}
