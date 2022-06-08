package com.sentrysoftware.hardware.agent.mapping.opentelemetry;

import static com.sentrysoftware.hardware.agent.mapping.opentelemetry.MappingConstants.*;
import static com.sentrysoftware.matrix.common.helpers.HardwareConstants.USAGE_COUNT_ALARM_THRESHOLD;
import static com.sentrysoftware.matrix.common.helpers.HardwareConstants.USAGE_COUNT_WARNING_THRESHOLD;
import static com.sentrysoftware.matrix.common.helpers.HardwareConstants.VALUE_ALARM_THRESHOLD;
import static com.sentrysoftware.matrix.common.helpers.HardwareConstants.VALUE_WARNING_THRESHOLD;

import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;

import com.sentrysoftware.hardware.agent.mapping.opentelemetry.dto.MetricInfo;
import com.sentrysoftware.hardware.agent.mapping.opentelemetry.dto.StaticIdentifyingAttribute;
import com.sentrysoftware.hardware.agent.mapping.opentelemetry.dto.MetricInfo.MetricType;
import com.sentrysoftware.matrix.common.meta.monitor.IMetaMonitor;
import com.sentrysoftware.matrix.common.meta.monitor.OtherDevice;

import lombok.AccessLevel;
import lombok.NoArgsConstructor;

@NoArgsConstructor(access = AccessLevel.PRIVATE)
public class OtherDeviceMapping {


	private static final String MONITOR_TYPE = "other device";
	private static final String STATUS_METRIC_NAME = "hw.other_device.status";
	private static final String STATUS_METRIC_DESCRIPTION = createStatusDescription(
		MONITOR_TYPE,
		STATE_ATTRIBUTE_KEY,
		OK_ATTRIBUTE_VALUE, DEGRADED_ATTRIBUTE_VALUE, FAILED_ATTRIBUTE_VALUE, PRESENT_ATTRIBUTE_VALUE
	);
	private static final String USES_LIMIT_METRIC_DESCRIPTION = createCustomDescriptionWithAttributes(
		"Number of times the device has been used which will generate a warning or an alarm when reached",
		LIMIT_TYPE_ATTRIBUTE_KEY,
		CRITICAL_ATTRIBUTE_VALUE, DEGRADED_ATTRIBUTE_VALUE
	);
	private static final String VALUE_LIMIT_METRIC_DESCRIPTION = createCustomDescriptionWithAttributes(
		"Device reported value that will generate a warning or an alarm when reached",
		LIMIT_TYPE_ATTRIBUTE_KEY,
		CRITICAL_ATTRIBUTE_VALUE, DEGRADED_ATTRIBUTE_VALUE
	);

	/**
	 * Build other device metrics map
	 *
	 * @return  {@link Map} where the metrics are indexed by the matrix parameter name
	 */
	static Map<String, List<MetricInfo>> buildOtherDeviceMetricsMapping() {
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
			OtherDevice.USAGE_COUNT.getName(),
			Collections.singletonList(
				MetricInfo
					.builder()
					.name("hw.other_device.uses")
					.unit(USAGE_UNIT)
					.type(MetricType.COUNTER)
					.description("Number of times the device has been used.")
					.build()
			)
		);

		map.put(
			OtherDevice.VALUE.getName(),
			Collections.singletonList(
				MetricInfo
					.builder()
					.name("hw.other_device.value")
					.description("Currently reported value of the device.")
					.build()
			)
		);

		return map;
	}

	/**
	 * Create other device metadata to metrics map
	 * 
	 * @return {@link Map} of {@link MetricInfo} instances indexed by the matrix parameter names
	 */
	static Map<String, List<MetricInfo>> otherDeviceMetadataToMetrics() {
		final Map<String, List<MetricInfo>> map = new TreeMap<>(String.CASE_INSENSITIVE_ORDER);

		map.put(
			USAGE_COUNT_WARNING_THRESHOLD,
			Collections.singletonList(
				MetricInfo
					.builder()
					.name("hw.other_device.uses.limit")
					.description(USES_LIMIT_METRIC_DESCRIPTION)
					.unit(USAGE_UNIT)
					.identifyingAttribute(
						StaticIdentifyingAttribute
							.builder()
							.key(LIMIT_TYPE_ATTRIBUTE_KEY)
							.value(DEGRADED_ATTRIBUTE_VALUE)
							.build()
					)
					.build()
			)
		);

		map.put(
			USAGE_COUNT_ALARM_THRESHOLD,
			Collections.singletonList(
				MetricInfo
					.builder()
					.name("hw.other_device.uses.limit")
					.description(USES_LIMIT_METRIC_DESCRIPTION)
					.unit(USAGE_UNIT)
					.identifyingAttribute(
						StaticIdentifyingAttribute
							.builder()
							.key(LIMIT_TYPE_ATTRIBUTE_KEY)
							.value(CRITICAL_ATTRIBUTE_VALUE)
							.build()
					)
					.build()
			)
		);
		
		map.put(
			VALUE_WARNING_THRESHOLD,
			Collections.singletonList(
				MetricInfo
					.builder()
					.name("hw.other_device.value.limit")
					.description(VALUE_LIMIT_METRIC_DESCRIPTION)
					.identifyingAttribute(
						StaticIdentifyingAttribute
							.builder()
							.key(LIMIT_TYPE_ATTRIBUTE_KEY)
							.value(DEGRADED_ATTRIBUTE_VALUE)
							.build()
					)
					.build()
			)
		);

		map.put(
			VALUE_ALARM_THRESHOLD,
			Collections.singletonList(
				MetricInfo
					.builder()
					.name("hw.other_device.value.limit")
					.description(VALUE_LIMIT_METRIC_DESCRIPTION)
					.identifyingAttribute(
						StaticIdentifyingAttribute
							.builder()
							.key(LIMIT_TYPE_ATTRIBUTE_KEY)
							.value(CRITICAL_ATTRIBUTE_VALUE)
							.build()
					)
					.build()
			)
		);

		return map;
	}
}