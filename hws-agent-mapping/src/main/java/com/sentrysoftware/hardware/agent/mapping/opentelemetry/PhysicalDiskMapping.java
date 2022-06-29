package com.sentrysoftware.hardware.agent.mapping.opentelemetry;

import static com.sentrysoftware.hardware.agent.mapping.opentelemetry.MappingConstants.*;
import static com.sentrysoftware.matrix.common.helpers.HardwareConstants.ERROR_COUNT_ALARM_THRESHOLD;
import static com.sentrysoftware.matrix.common.helpers.HardwareConstants.ERROR_COUNT_WARNING_THRESHOLD;
import static com.sentrysoftware.matrix.common.helpers.HardwareConstants.SIZE;

import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;

import com.sentrysoftware.hardware.agent.mapping.opentelemetry.dto.MetricInfo;
import com.sentrysoftware.hardware.agent.mapping.opentelemetry.dto.StaticIdentifyingAttribute;
import com.sentrysoftware.hardware.agent.mapping.opentelemetry.dto.MetricInfo.MetricType;
import com.sentrysoftware.matrix.common.meta.monitor.IMetaMonitor;
import com.sentrysoftware.matrix.common.meta.monitor.PhysicalDisk;

import lombok.AccessLevel;
import lombok.NoArgsConstructor;

@NoArgsConstructor(access = AccessLevel.PRIVATE)
public class PhysicalDiskMapping {

	public static final String HW_TYPE_ATTRIBUTE_VALUE = "physical_disk";

	/**
	 * Build physical disk metrics map
	 *
	 * @return  {@link Map} where the metrics are indexed by the matrix parameter name
	 */
	static Map<String, List<MetricInfo>> buildPhysicalDiskMetricsMapping() {
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
					.identifyingAttribute(
						StaticIdentifyingAttribute
							.builder()
							.key(HW_TYPE_ATTRIBUTE_KEY)
							.value(HW_TYPE_ATTRIBUTE_VALUE)
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
					.identifyingAttribute(
						StaticIdentifyingAttribute
							.builder()
							.key(HW_TYPE_ATTRIBUTE_KEY)
							.value(HW_TYPE_ATTRIBUTE_VALUE)
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
					.identifyingAttribute(
						StaticIdentifyingAttribute
							.builder()
							.key(HW_TYPE_ATTRIBUTE_KEY)
							.value(HW_TYPE_ATTRIBUTE_VALUE)
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
					.identifyingAttribute(
						StaticIdentifyingAttribute
							.builder()
							.key(HW_TYPE_ATTRIBUTE_KEY)
							.value(HW_TYPE_ATTRIBUTE_VALUE)
							.build()
					)
					.predicate(PRESENT_PREDICATE)
					.type(MetricType.UP_DOWN_COUNTER)
					.build()
			)
		);

		map.put(
			PhysicalDisk.ENDURANCE_REMAINING.getName(),
			Collections.singletonList(
				MetricInfo
					.builder()
					.name("hw.physical_disk.endurance_utilization")
					.factor(RATIO_FACTOR)
					.description("Physical disk remaining endurance ratio.")
					.unit(RATIO_UNIT)
					.identifyingAttribute(
						StaticIdentifyingAttribute
							.builder()
							.key(STATE_ATTRIBUTE_KEY)
							.value("remaining")
							.build()
					)
					.build()
			)
		);

		map.put(
			IMetaMonitor.ERROR_COUNT.getName(),
			Collections.singletonList(
				MetricInfo
					.builder()
					.name(ERRORS_METRIC_NAME)
					.type(MetricType.COUNTER)
					.unit(ERRORS_UNIT)
					.description(ERRORS_METRIC_DESCRIPTION)
					.identifyingAttribute(
						StaticIdentifyingAttribute
							.builder()
							.key(HW_TYPE_ATTRIBUTE_KEY)
							.value(HW_TYPE_ATTRIBUTE_VALUE)
							.build()
					)
					.build()
			)
		);

		map.put(
			IMetaMonitor.PREDICTED_FAILURE.getName(),
			Collections.singletonList(
				MetricInfo
					.builder()
					.name(STATUS_METRIC_NAME)
					.description(STATUS_METRIC_DESCRIPTION)
					.identifyingAttribute(
						StaticIdentifyingAttribute
							.builder()
							.key(STATE_ATTRIBUTE_KEY)
							.value(PREDICTED_FAILURE_ATTRIBUTE_VALUE)
							.build()
					)
					.identifyingAttribute(
						StaticIdentifyingAttribute
							.builder()
							.key(HW_TYPE_ATTRIBUTE_KEY)
							.value(HW_TYPE_ATTRIBUTE_VALUE)
							.build()
					)
					.predicate(PREDICTED_FAILURE_PREDICATE)
					.type(MetricType.UP_DOWN_COUNTER)
					.build()
			)
		);

		map.put(
			IMetaMonitor.ENERGY.getName(),
			Collections.singletonList(
				MetricInfo
					.builder()
					.name("hw.energy")
					.unit(JOULES_UNIT)
					.type(MetricType.COUNTER)
					.description(ENERGY_METRIC_DESCRIPTION)
					.identifyingAttribute(
						StaticIdentifyingAttribute
							.builder()
							.key(HW_TYPE_ATTRIBUTE_KEY)
							.value(HW_TYPE_ATTRIBUTE_VALUE)
							.build()
					)
					.build()
			)
		);

		map.put(
			IMetaMonitor.POWER_CONSUMPTION.getName(),
			Collections.singletonList(
				MetricInfo
					.builder()
					.name("hw.power")
					.unit(WATTS_UNIT)
					.type(MetricType.GAUGE)
					.description(POWER_METRIC_DESCRIPTION)
					.identifyingAttribute(
						StaticIdentifyingAttribute
							.builder()
							.key(HW_TYPE_ATTRIBUTE_KEY)
							.value(HW_TYPE_ATTRIBUTE_VALUE)
							.build()
					)
					.build()
			)
		);

		return map;
	}

	/**
	 * Create PhysicalDisk Metadata to metrics map
	 * 
	 * @return {@link Map} of {@link MetricInfo} instances indexed by the matrix parameter names
	 */
	static Map<String, List<MetricInfo>> physicalDiskMetadataToMetrics() {
		final Map<String, List<MetricInfo>> map = new TreeMap<>(String.CASE_INSENSITIVE_ORDER);

		map.put(
			SIZE,
			Collections.singletonList(
				MetricInfo
					.builder()
					.name("hw.physical_disk.size")
					.unit(BYTES_UNIT)
					.description("Physical disk size.")
					.build()
			)
		);

		map.put(
			ERROR_COUNT_WARNING_THRESHOLD,
			Collections.singletonList(
				MetricInfo
					.builder()
					.name(ERRORS_LIMIT_METRIC_NAME)
					.unit(ERRORS_UNIT)
					.description(ERRORS_LIMIT_METRIC_DESCRIPTION)
					.identifyingAttribute(
						StaticIdentifyingAttribute
							.builder()
							.key(HW_TYPE_ATTRIBUTE_KEY)
							.value(HW_TYPE_ATTRIBUTE_VALUE)
							.build()
					)
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
			ERROR_COUNT_ALARM_THRESHOLD,
			Collections.singletonList(
				MetricInfo
					.builder()
					.name(ERRORS_LIMIT_METRIC_NAME)
					.unit(ERRORS_UNIT)
					.description(ERRORS_LIMIT_METRIC_DESCRIPTION)
					.identifyingAttribute(
						StaticIdentifyingAttribute
							.builder()
							.key(HW_TYPE_ATTRIBUTE_KEY)
							.value(HW_TYPE_ATTRIBUTE_VALUE)
							.build()
					)
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
