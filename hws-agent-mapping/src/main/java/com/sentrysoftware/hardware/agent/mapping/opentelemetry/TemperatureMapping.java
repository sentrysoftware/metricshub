package com.sentrysoftware.hardware.agent.mapping.opentelemetry;

import static com.sentrysoftware.hardware.agent.mapping.opentelemetry.MappingConstants.*;
import static com.sentrysoftware.matrix.common.helpers.HardwareConstants.*;

import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;

import com.sentrysoftware.hardware.agent.mapping.opentelemetry.dto.MetricInfo;
import com.sentrysoftware.hardware.agent.mapping.opentelemetry.dto.StaticIdentifyingAttribute;
import com.sentrysoftware.hardware.agent.mapping.opentelemetry.dto.MetricInfo.MetricType;
import com.sentrysoftware.matrix.common.meta.monitor.IMetaMonitor;
import com.sentrysoftware.matrix.common.meta.monitor.Temperature;

import lombok.AccessLevel;
import lombok.NoArgsConstructor;

@NoArgsConstructor(access = AccessLevel.PRIVATE)
public class TemperatureMapping {

	private static final String STATUS_METRIC_NAME = "hw.temperature.status";
	private static final String MONITOR_TYPE = "temperature";
	private static final String STATUS_METRIC_DESCRIPTION = createStatusDescription(
		MONITOR_TYPE,
		STATE_ATTRIBUTE_KEY,
		OK_ATTRIBUTE_VALUE, DEGRADED_ATTRIBUTE_VALUE, FAILED_ATTRIBUTE_VALUE, PRESENT_ATTRIBUTE_VALUE
	);
	private static final String LIMIT_METRIC_DESCRIPTION = createCustomDescriptionWithAttributes(
		"Current temperature in degrees Celsius (°C) that will generate a warning or an alarm when reached",
		LIMIT_TYPE_ATTRIBUTE_KEY,
		HIGH_DEGRADED_ATTRIBUTE_VALUE, HIGH_CRITICAL_ATTRIBUTE_VALUE
	);

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
					.name(STATUS_METRIC_NAME)
					.description(STATUS_METRIC_DESCRIPTION)
					.identifyingAttribute(
						StaticIdentifyingAttribute
							.builder()
							.key(STATE_ATTRIBUTE_KEY)
							.value(OK_ATTRIBUTE_VALUE)
							.build()
					)
					.predicate(OK_STATUS_PREDICATE)
					.type(MetricType.UP_DOWN_COUNTER)
					.build(),
				MetricInfo
					.builder()
					.name(STATUS_METRIC_NAME)
					.description(STATUS_METRIC_DESCRIPTION)
					.identifyingAttribute(
						StaticIdentifyingAttribute
							.builder()
							.key(STATE_ATTRIBUTE_KEY)
							.value(DEGRADED_ATTRIBUTE_VALUE)
							.build()
					)
					.predicate(DEGRADED_STATUS_PREDICATE)
					.type(MetricType.UP_DOWN_COUNTER)
					.build(),
				MetricInfo
					.builder()
					.name(STATUS_METRIC_NAME)
					.description(STATUS_METRIC_DESCRIPTION)
					.identifyingAttribute(
						StaticIdentifyingAttribute
							.builder()
							.key(STATE_ATTRIBUTE_KEY)
							.value(FAILED_ATTRIBUTE_VALUE)
							.build()
					)
					.predicate(FAILED_STATUS_PREDICATE)
					.type(MetricType.UP_DOWN_COUNTER)
					.build()
			)
		);

		map.put(
			IMetaMonitor.PRESENT.getName(),
			Collections.singletonList(
				MetricInfo
					.builder()
					.name(STATUS_METRIC_NAME)
					.description(STATUS_METRIC_DESCRIPTION)
					.identifyingAttribute(
						StaticIdentifyingAttribute
							.builder()
							.key(STATE_ATTRIBUTE_KEY)
							.value(PRESENT_ATTRIBUTE_VALUE)
							.build()
					)
					.predicate(PRESENT_PREDICATE)
					.type(MetricType.UP_DOWN_COUNTER)
					.build()
			)
		);

		map.put(
			Temperature._TEMPERATURE.getName(),
			Collections.singletonList(
				MetricInfo
					.builder()
					.name("hw.temperature.temperature")
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
					.name("hw.temperature.limit")
					.description(LIMIT_METRIC_DESCRIPTION)
					.unit(CELSIUS_UNIT)
					.identifyingAttribute(
						StaticIdentifyingAttribute
							.builder()
							.key(LIMIT_TYPE_ATTRIBUTE_KEY)
							.value(HIGH_DEGRADED_ATTRIBUTE_VALUE)
							.build()
					)
					.build()
			)
		);

		map.put(
			ALARM_THRESHOLD,
			Collections.singletonList(
				MetricInfo
					.builder()
					.name("hw.temperature.limit")
					.description(LIMIT_METRIC_DESCRIPTION)
					.unit(CELSIUS_UNIT)
					.identifyingAttribute(
						StaticIdentifyingAttribute
							.builder()
							.key(LIMIT_TYPE_ATTRIBUTE_KEY)
							.value(HIGH_CRITICAL_ATTRIBUTE_VALUE)
							.build()
					)
					.build()
			)
		);

		return map;
	}

	/**
	 * Build the overridden attribute names to bypass the automatic renaming of the
	 * internal matrix metadata key
	 * 
	 * @return lookup indexed by the internal matrix metadata key
	 */
	public static Map<String, String> buildOverriddenAttributeNames() {

		Map<String, String> map = new HashMap<>();

		// Put the temperature's specific attribute
		map.put(TEMPERATURE_TYPE, SENSOR_LOCATION_ATTRIBUTE_KEY);

		// Put all the default attributes
		map.putAll(MetricsMapping.buildDefaultOverriddenAttributeNames());

		return map;
	}
}
