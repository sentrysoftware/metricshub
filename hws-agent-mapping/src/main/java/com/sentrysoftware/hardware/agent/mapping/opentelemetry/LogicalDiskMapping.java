package com.sentrysoftware.hardware.agent.mapping.opentelemetry;

import static com.sentrysoftware.matrix.common.helpers.HardwareConstants.SIZE;
import static com.sentrysoftware.hardware.agent.mapping.opentelemetry.MappingConstants.*;
import static com.sentrysoftware.matrix.common.helpers.HardwareConstants.ERROR_COUNT_ALARM_THRESHOLD;
import static com.sentrysoftware.matrix.common.helpers.HardwareConstants.ERROR_COUNT_WARNING_THRESHOLD;

import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;

import com.sentrysoftware.hardware.agent.mapping.opentelemetry.dto.MetricInfo;
import com.sentrysoftware.hardware.agent.mapping.opentelemetry.dto.StaticIdentifyingAttribute;
import com.sentrysoftware.hardware.agent.mapping.opentelemetry.dto.MetricInfo.MetricType;
import com.sentrysoftware.matrix.common.meta.monitor.IMetaMonitor;
import com.sentrysoftware.matrix.common.meta.monitor.LogicalDisk;

import lombok.AccessLevel;
import lombok.NoArgsConstructor;

@NoArgsConstructor(access = AccessLevel.PRIVATE)
public class LogicalDiskMapping {

	private static final String STATUS_METRIC_NAME = "hw.logical_disk.status";
	static final String USAGE_METRIC_NAME = "hw.logical_disk.usage";
	private static final String UTILIZATION_METRIC_NAME = "hw.logical_disk.utilization";
	private static final String MONITOR_TYPE = "logical disk";
	private static final String STATUS_METRIC_DESCRIPTION = createStatusDescription(
		MONITOR_TYPE,
		STATE_ATTRIBUTE_KEY,
		OK_ATTRIBUTE_VALUE, DEGRADED_ATTRIBUTE_VALUE, FAILED_ATTRIBUTE_VALUE, PRESENT_ATTRIBUTE_VALUE
	);
	private static final String USAGE_METRIC_DESCRIPTION = createCustomDescriptionWithAttributes(
		"Amount of used or unused space in the logical disk",
		STATE_ATTRIBUTE_KEY, FREE_ATTRIBUTE_VALUE, USED_ATTRIBUTE_VALUE
	);

	private static final String UTILIZATION_METRIC_DESCRIPTION = createCustomDescriptionWithAttributes(
		"Ratio of used or unused space in the logical disk",
		STATE_ATTRIBUTE_KEY, FREE_ATTRIBUTE_VALUE, USED_ATTRIBUTE_VALUE
	);

	/**
	 * Build logical disk metrics map
	 *
	 * @return  {@link Map} where the metrics are indexed by the matrix parameter name
	 */
	static Map<String, List<MetricInfo>> buildLogicalDiskMetricsMapping() {
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
			LogicalDisk.UNALLOCATED_SPACE.getName(),
			Collections.singletonList(
				MetricInfo
					.builder()
					.name(USAGE_METRIC_NAME)
					.description(USAGE_METRIC_DESCRIPTION)
					.unit(BYTES_UNIT)
					.factor(GIGABYTES_TO_BYTES_FACTOR)
					.type(MetricType.UP_DOWN_COUNTER)
					.identifyingAttribute(
						StaticIdentifyingAttribute
							.builder()
							.key(STATE_ATTRIBUTE_KEY)
							.value(FREE_ATTRIBUTE_VALUE)
							.build()
					)
					.build()
			)
		);

		map.put(
			LogicalDisk.ALLOCATED_SPACE.getName(),
			Collections.singletonList(
				MetricInfo
					.builder()
					.name(USAGE_METRIC_NAME)
					.description(USAGE_METRIC_DESCRIPTION)
					.unit(BYTES_UNIT)
					.factor(GIGABYTES_TO_BYTES_FACTOR)
					.type(MetricType.UP_DOWN_COUNTER)
					.identifyingAttribute(
						StaticIdentifyingAttribute
							.builder()
							.key(STATE_ATTRIBUTE_KEY)
							.value(USED_ATTRIBUTE_VALUE)
							.build()
					)
					.build()
			)
		);

		map.put(
			LogicalDisk.ALLOCATED_SPACE_PERCENT.getName(),
			Collections.singletonList(
				MetricInfo
					.builder()
					.name(UTILIZATION_METRIC_NAME)
					.description(UTILIZATION_METRIC_DESCRIPTION)
					.unit(RATIO_UNIT)
					.factor(RATIO_FACTOR)
					.identifyingAttribute(
						StaticIdentifyingAttribute
							.builder()
							.key(STATE_ATTRIBUTE_KEY)
							.value(USED_ATTRIBUTE_VALUE)
							.build()
					)
					.build()
			)
		);

		map.put(
			LogicalDisk.UNALLOCATED_SPACE_PERCENT.getName(),
			Collections.singletonList(
				MetricInfo
					.builder()
					.name(UTILIZATION_METRIC_NAME)
					.description(UTILIZATION_METRIC_DESCRIPTION)
					.unit(RATIO_UNIT)
					.factor(RATIO_FACTOR)
					.identifyingAttribute(
						StaticIdentifyingAttribute
							.builder()
							.key(STATE_ATTRIBUTE_KEY)
							.value(FREE_ATTRIBUTE_VALUE)
							.build()
					)
					.build()
			)
		);

		map.put(
			LogicalDisk.ERROR_COUNT.getName(),
			Collections.singletonList(
				MetricInfo
					.builder()
					.name("hw.logical_disk.errors")
					.description("Number of errors encountered by the logical disk since the start of the Hardware Sentry Agent.")
					.unit(ERRORS_UNIT)
					.type(MetricType.COUNTER)
					.build()
			)
		);

		return map;
	}

	/**
	 * Create LogicalDisk Metadata to metrics map
	 * 
	 * @return {@link Map} of {@link MetricInfo} instances indexed by the matrix parameter names
	 */
	static Map<String, List<MetricInfo>> logicalDiskMetadataToMetrics() {
		final Map<String, List<MetricInfo>> map = new TreeMap<>(String.CASE_INSENSITIVE_ORDER);

		map.put(
			SIZE,
			Collections.singletonList(
				MetricInfo
					.builder()
					.name("hw.logical_disk.limit")
					.unit(BYTES_UNIT)
					.description("Logical disk size.")
					.build()
			)
		);

		map.put(
			ERROR_COUNT_WARNING_THRESHOLD,
			Collections.singletonList(
				MetricInfo
					.builder()
					.name("hw.logical_disk.errors.limit")
					.description(WARNING_OR_ALARM_THRESHOLD_OF_ERRORS)
					.unit(ERRORS_UNIT)
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
					.name("hw.logical_disk.errors.limit")
					.description(WARNING_OR_ALARM_THRESHOLD_OF_ERRORS)
					.unit(ERRORS_UNIT)
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
