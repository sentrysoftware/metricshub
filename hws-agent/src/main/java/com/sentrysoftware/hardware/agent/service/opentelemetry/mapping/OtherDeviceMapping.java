package com.sentrysoftware.hardware.agent.service.opentelemetry.mapping;

import static com.sentrysoftware.hardware.agent.service.opentelemetry.mapping.MappingConstants.ALARM_THRESHOLD_OF_USAGE_COUNT;
import static com.sentrysoftware.hardware.agent.service.opentelemetry.mapping.MappingConstants.ALARM_THRESHOLD_OF_VALUE;
import static com.sentrysoftware.hardware.agent.service.opentelemetry.mapping.MappingConstants.DEGRADED_ATTRIBUTE_VALUE;
import static com.sentrysoftware.hardware.agent.service.opentelemetry.mapping.MappingConstants.DEGRADED_STATUS_PREDICATE;
import static com.sentrysoftware.hardware.agent.service.opentelemetry.mapping.MappingConstants.FAILED_ATTRIBUTE_VALUE;
import static com.sentrysoftware.hardware.agent.service.opentelemetry.mapping.MappingConstants.FAILED_STATUS_PREDICATE;
import static com.sentrysoftware.hardware.agent.service.opentelemetry.mapping.MappingConstants.OK_ATTRIBUTE_VALUE;
import static com.sentrysoftware.hardware.agent.service.opentelemetry.mapping.MappingConstants.OK_STATUS_PREDICATE;
import static com.sentrysoftware.hardware.agent.service.opentelemetry.mapping.MappingConstants.PRESENT_ATTRIBUTE_VALUE;
import static com.sentrysoftware.hardware.agent.service.opentelemetry.mapping.MappingConstants.PRESENT_PREDICATE;
import static com.sentrysoftware.hardware.agent.service.opentelemetry.mapping.MappingConstants.STATE_ATTRIBUTE_KEY;
import static com.sentrysoftware.hardware.agent.service.opentelemetry.mapping.MappingConstants.USAGE_UNIT;
import static com.sentrysoftware.hardware.agent.service.opentelemetry.mapping.MappingConstants.WARNING_THRESHOLD_OF_USAGE_COUNT;
import static com.sentrysoftware.hardware.agent.service.opentelemetry.mapping.MappingConstants.WARNING_THRESHOLD_OF_VALUE;
import static com.sentrysoftware.matrix.common.helpers.HardwareConstants.USAGE_COUNT_ALARM_THRESHOLD;
import static com.sentrysoftware.matrix.common.helpers.HardwareConstants.USAGE_COUNT_WARNING_THRESHOLD;
import static com.sentrysoftware.matrix.common.helpers.HardwareConstants.VALUE_ALARM_THRESHOLD;
import static com.sentrysoftware.matrix.common.helpers.HardwareConstants.VALUE_WARNING_THRESHOLD;

import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;

import com.sentrysoftware.hardware.agent.dto.metric.MetricInfo;
import com.sentrysoftware.hardware.agent.dto.metric.MetricInfo.MetricType;
import com.sentrysoftware.hardware.agent.dto.metric.StaticIdentifyingAttribute;
import com.sentrysoftware.matrix.common.meta.monitor.IMetaMonitor;
import com.sentrysoftware.matrix.common.meta.monitor.OtherDevice;

import lombok.AccessLevel;
import lombok.NoArgsConstructor;

@NoArgsConstructor(access = AccessLevel.PRIVATE)
public class OtherDeviceMapping {

	private static final String OTHER_DEVICE_TYPE = "other device";
	private static final String OTHER_DEVICE_STATUS_METRIC_NAME = "hw.other_device.status";

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
					.name(OTHER_DEVICE_STATUS_METRIC_NAME)
					.description(MappingConstants.createStatusDescription(OTHER_DEVICE_TYPE, OK_ATTRIBUTE_VALUE))
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
					.name(OTHER_DEVICE_STATUS_METRIC_NAME)
					.description(MappingConstants.createStatusDescription(OTHER_DEVICE_TYPE, DEGRADED_ATTRIBUTE_VALUE))
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
					.name(OTHER_DEVICE_STATUS_METRIC_NAME)
					.description(MappingConstants.createStatusDescription(OTHER_DEVICE_TYPE, FAILED_ATTRIBUTE_VALUE))
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
					.name(OTHER_DEVICE_STATUS_METRIC_NAME)
					.description(MappingConstants.createPresentDescription(OTHER_DEVICE_TYPE))
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
					.name("hw.other_device.uses_warning")
					.description(WARNING_THRESHOLD_OF_USAGE_COUNT)
					.unit(USAGE_UNIT)
					.build()
			)
		);

		map.put(
			USAGE_COUNT_ALARM_THRESHOLD,
			Collections.singletonList(
				MetricInfo
					.builder()
					.name("hw.other_device.uses_alarm")
					.description(ALARM_THRESHOLD_OF_USAGE_COUNT)
					.unit(USAGE_UNIT)
					.build()
			)
		);
		
		map.put(
			VALUE_WARNING_THRESHOLD,
			Collections.singletonList(
				MetricInfo
					.builder()
					.name("hw.other_device.value_warning")
					.description(WARNING_THRESHOLD_OF_VALUE)
					.build()
			)
		);

		map.put(
			VALUE_ALARM_THRESHOLD,
			Collections.singletonList(
				MetricInfo
					.builder()
					.name("hw.other_device.value_alarm")
					.description(ALARM_THRESHOLD_OF_VALUE)
					.build()
			)
		);

		return map;
	}
}