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
import com.sentrysoftware.matrix.common.meta.monitor.TapeDrive;

import lombok.AccessLevel;
import lombok.NoArgsConstructor;

@NoArgsConstructor(access = AccessLevel.PRIVATE)
public class TapeDriveMapping {

	private static final String MONITOR_TYPE = "tape drive";
	private static final String TAPE_DRIVE_STATUS_METRIC_NAME = "hw.tape_drive.status";
	private static final String TAPE_DRIVE_OPERATIONS_METRIC_NAME = "hw.tape_drive.operations";

	/**
	 * Build tape drive metrics map
	 *
	 * @return  {@link Map} where the metrics are indexed by the matrix parameter name
	 */
	static Map<String, List<MetricInfo>> buildTapeDriveMetricsMapping() {
		final Map<String, List<MetricInfo>> map = new TreeMap<>(String.CASE_INSENSITIVE_ORDER);

		map.put(
			IMetaMonitor.STATUS.getName(),
			List.of(
				MetricInfo
					.builder()
					.name(TAPE_DRIVE_STATUS_METRIC_NAME)
					.description(createStatusDescription(MONITOR_TYPE, OK_ATTRIBUTE_VALUE))
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
					.name(TAPE_DRIVE_STATUS_METRIC_NAME)
					.description(createStatusDescription(MONITOR_TYPE, DEGRADED_ATTRIBUTE_VALUE))
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
					.name(TAPE_DRIVE_STATUS_METRIC_NAME)
					.description(createStatusDescription(MONITOR_TYPE, FAILED_ATTRIBUTE_VALUE))
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
					.name(TAPE_DRIVE_STATUS_METRIC_NAME)
					.description(createPresentDescription(MONITOR_TYPE))
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
			TapeDrive.NEEDS_CLEANING.getName(),
			List.of(
				MetricInfo
					.builder()
					.name(TAPE_DRIVE_STATUS_METRIC_NAME)
					.description("Whether the tape drive doesn't need cleaning.")
					.identifyingAttribute(
						StaticIdentifyingAttribute
							.builder()
							.key(STATE_ATTRIBUTE_KEY)
							.value("no_needs_cleaning")
							.build()
					)
					.predicate(NO_NEEDS_CLEANING_PREDICATE)
					.build(),
				MetricInfo
					.builder()
					.name(TAPE_DRIVE_STATUS_METRIC_NAME)
					.description("Whether the tape drive needs cleaning.")
					.identifyingAttribute(
						StaticIdentifyingAttribute
							.builder()
							.key(STATE_ATTRIBUTE_KEY)
							.value("needs_cleaning")
							.build()
					)
					.predicate(CLEANING_NEEDED_PREDICATE)
					.build(),
				MetricInfo
					.builder()
					.name(TAPE_DRIVE_STATUS_METRIC_NAME)
					.description("Whether the tape drive needs cleaning immediately.")
					.identifyingAttribute(
						StaticIdentifyingAttribute
							.builder()
							.key(STATE_ATTRIBUTE_KEY)
							.value("needs_cleaning_immediately")
							.build()
					)
					.predicate(IMMEDIATEL_CLEANING_NEEDED_PREDICATE)
					.build()
			)
		);

		map.put(
			TapeDrive.MOUNT_COUNT.getName(),
			Collections.singletonList(
				MetricInfo
					.builder()
					.name(TAPE_DRIVE_OPERATIONS_METRIC_NAME)
					.unit(OPERATIONS_UNIT)
					.description("Number of mount operations that occurred during the last collect interval.")
					.identifyingAttribute(
						StaticIdentifyingAttribute
							.builder()
							.key(TYPE_ATTRIBUTE_KEY)
							.value("mount")
							.build()
					)
					.type(MetricType.COUNTER)
					.build()
			)
		);

		map.put(
			TapeDrive.UNMOUNT_COUNT.getName(),
			Collections.singletonList(
				MetricInfo
					.builder()
					.name(TAPE_DRIVE_OPERATIONS_METRIC_NAME)
					.unit(OPERATIONS_UNIT)
					.description("Number of unmount operations that occurred during the last collect interval.")
					.identifyingAttribute(
						StaticIdentifyingAttribute
							.builder()
							.key(TYPE_ATTRIBUTE_KEY)
							.value("unmount")
							.build()
					)
					.type(MetricType.COUNTER)
					.build()
			)
		);

		map.put(
			IMetaMonitor.ERROR_COUNT.getName(),
			Collections.singletonList(
				MetricInfo
					.builder()
					.name("hw.tape_drive.errors")
					.unit(ERRORS_UNIT)
					.type(MetricType.COUNTER)
					.description("Number of errors encountered by the tape drive since the start of the Hardware Sentry Agent.")
					.build()
			)
		);

		map.put(
			IMetaMonitor.ENERGY.getName(),
			Collections.singletonList(
				MetricInfo
					.builder()
					.name("hw.tape_drive.energy")
					.unit(JOULES_UNIT)
					.type(MetricType.COUNTER)
					.description(createEnergyDescription(MONITOR_TYPE))
					.build()
			)
		);

		map.put(
			IMetaMonitor.POWER_CONSUMPTION.getName(),
			Collections.singletonList(
				MetricInfo
					.builder()
					.name("hw.tape_drive.power")
					.unit(WATTS_UNIT)
					.description(createPowerConsumptionDescription(MONITOR_TYPE))
					.build()
			)
		);

		return map;
	}

	/**
	 * Create TapeDrive Metadata to metrics map
	 * 
	 * @return {@link Map} of {@link MetricInfo} instances indexed by the matrix parameter names
	 */
	static Map<String, List<MetricInfo>> tapeDriveMetadataToMetrics() {
		final Map<String, List<MetricInfo>> map = new TreeMap<>(String.CASE_INSENSITIVE_ORDER);

		map.put(
			ERROR_COUNT_WARNING_THRESHOLD,
			Collections.singletonList(
				MetricInfo
					.builder()
					.name("hw.tape_drive.errors.limit")
					.description(WARNING_THRESHOLD_OF_ERRORS)
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
					.name("hw.tape_drive.errors.limit")
					.description(ALARM_THRESHOLD_OF_ERRORS)
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
