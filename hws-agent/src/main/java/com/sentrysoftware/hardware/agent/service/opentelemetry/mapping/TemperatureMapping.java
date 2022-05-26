package com.sentrysoftware.hardware.agent.service.opentelemetry.mapping;

import static com.sentrysoftware.hardware.agent.service.opentelemetry.mapping.MappingConstants.*;
import static com.sentrysoftware.matrix.common.helpers.HardwareConstants.ALARM_THRESHOLD;
import static com.sentrysoftware.matrix.common.helpers.HardwareConstants.WARNING_THRESHOLD;

import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;

import com.sentrysoftware.hardware.agent.dto.metric.MetricInfo;
import com.sentrysoftware.hardware.agent.dto.metric.StaticIdentifyingAttribute;
import com.sentrysoftware.matrix.common.meta.monitor.IMetaMonitor;
import com.sentrysoftware.matrix.common.meta.monitor.Temperature;

import lombok.AccessLevel;
import lombok.NoArgsConstructor;

@NoArgsConstructor(access = AccessLevel.PRIVATE)
public class TemperatureMapping {

	private static final String TEMPERATURE_STATUS_METRIC_NAME = "hw.temperature.status";

	/**
	 * Build temperature metrics map
	 *
	 * @return  {@link Map} where the metrics are indexed by the matrix parameter name
	 */
	static Map<String, List<MetricInfo>> buildTemperatureMetricsMapping() {
		final Map<String, List<MetricInfo>> map = new TreeMap<>(String.CASE_INSENSITIVE_ORDER);

		map.put(
			IMetaMonitor.STATUS.getName(),
			List.of(
				MetricInfo
					.builder()
					.name(TEMPERATURE_STATUS_METRIC_NAME)
					.description("Whether the temperature status is ok or not.")
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
					.name(TEMPERATURE_STATUS_METRIC_NAME)
					.description("Whether the temperature status is degraded or not.")
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
					.name(TEMPERATURE_STATUS_METRIC_NAME)
					.description("Whether the temperature status is failed or not.")
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
					.name(TEMPERATURE_STATUS_METRIC_NAME)
					.description("Whether the temperature sensor is found or not.")
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
			Temperature._TEMPERATURE.getName(),
			Collections.singletonList(
				MetricInfo
					.builder()
					.name("hw.temperature.temperature_celsius")
					.description("Current temperature reading in Celsius degrees.")
					.unit(CELSIUS_UNIT)
					.build()
			)
		);

		return map;
	}

	/**
	 * Create temperature Metadata to metrics map
	 * 
	 * @return {@link Map} of {@link MetricInfo} instances indexed by the matrix parameter names
	 */
	static Map<String, List<MetricInfo>> temperatureMetadataToMetrics() {
		final Map<String, List<MetricInfo>> map = new TreeMap<>(String.CASE_INSENSITIVE_ORDER);

		map.put(
			WARNING_THRESHOLD,
			Collections.singletonList(
				MetricInfo
					.builder()
					.name("hw.temperature.errors_warning")
					.description("Current temperature in degrees Celsius (°C) that will generate a warning when reached.")
					.unit(CELSIUS_UNIT)
					.build()
			)
		);

		map.put(
			ALARM_THRESHOLD,
			Collections.singletonList(
				MetricInfo
					.builder()
					.name("hw.temperature.errors_alarm")
					.description("Current temperature in degrees Celsius (°C) that will generate an alarm when reached.")
					.unit(CELSIUS_UNIT)
					.build()
			)
		);

		return map;
	}
}
