package com.sentrysoftware.hardware.agent.service.opentelemetry.mapping;

import static com.sentrysoftware.hardware.agent.service.opentelemetry.mapping.MappingConstants.*;
import static com.sentrysoftware.matrix.common.helpers.HardwareConstants.ERROR_COUNT_ALARM_THRESHOLD;
import static com.sentrysoftware.matrix.common.helpers.HardwareConstants.ERROR_COUNT_WARNING_THRESHOLD;
import static com.sentrysoftware.matrix.common.helpers.HardwareConstants.SIZE;

import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;

import com.sentrysoftware.hardware.agent.dto.metric.MetricInfo;
import com.sentrysoftware.hardware.agent.dto.metric.StaticIdentifyingAttribute;
import com.sentrysoftware.hardware.agent.dto.metric.MetricInfo.MetricType;
import com.sentrysoftware.matrix.common.meta.monitor.IMetaMonitor;
import com.sentrysoftware.matrix.common.meta.monitor.PhysicalDisk;

import lombok.AccessLevel;
import lombok.NoArgsConstructor;

@NoArgsConstructor(access = AccessLevel.PRIVATE)
public class PhysicalDiskMapping {

	private static final String PHYSICAL_DISK_STATUS_METRIC_NAME = "hw.physical_disk.status";

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
					.name(PHYSICAL_DISK_STATUS_METRIC_NAME)
					.description("Whether the physical disk status is ok or not.")
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
					.name(PHYSICAL_DISK_STATUS_METRIC_NAME)
					.description("Whether the physical disk status is degraded or not.")
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
					.name(PHYSICAL_DISK_STATUS_METRIC_NAME)
					.description("Whether the physical disk status is failed or not.")
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
					.name(PHYSICAL_DISK_STATUS_METRIC_NAME)
					.description("Whether the physical disk is found or not.")
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
			PhysicalDisk.ENDURANCE_REMAINING.getName(),
			Collections.singletonList(
				MetricInfo
					.builder()
					.name("hw.physical_disk.endurance_utilization")
					.factor(0.01)
					.description("Physical disk remaining endurance ratio.")
					.unit("1")
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
					.name("hw.physical_disk.errors")
					.unit(ERRORS_UNIT)
					.type(MetricType.COUNTER)
					.description("Number of errors encountered by the physical disk since the start of the Hardware Sentry Agent.")
					.build()
			)
		);

		map.put(
			IMetaMonitor.PREDICTED_FAILURE.getName(),
			Collections.singletonList(
				MetricInfo
					.builder()
					.name(PHYSICAL_DISK_STATUS_METRIC_NAME)
					.description("Informs if a failure is predicted.")
					.identifyingAttribute(
						StaticIdentifyingAttribute
							.builder()
							.key(STATE_ATTRIBUTE_KEY)
							.value(PREDICTED_FAILURE_ATTRIBUTE_VALUE)
							.build()
					)
					.build()
			)
		);

		map.put(
			IMetaMonitor.ENERGY.getName(),
			Collections.singletonList(
				MetricInfo
					.builder()
					.name("hw.physical_disk.energy")
					.unit(JOULES_UNIT)
					.type(MetricType.COUNTER)
					.description("Energy consumed by the physical disk since the start of the Hardware Sentry Agent.")
					.build()
			)
		);

		map.put(
			IMetaMonitor.POWER_CONSUMPTION.getName(),
			Collections.singletonList(
				MetricInfo
					.builder()
					.name("hw.physical_disk.power")
					.unit(WATTS_UNIT)
					.description("Energy consumed by the physical disk.")
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
					.name("hw.physical_disk.errors_warning")
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
					.name("hw.physical_disk.errors_alarm")
					.description(ALARM_THRESHOLD_OF_ERRORS)
					.unit(ERRORS_UNIT)
					.build()
			)
		);

		return map;
	}
}
