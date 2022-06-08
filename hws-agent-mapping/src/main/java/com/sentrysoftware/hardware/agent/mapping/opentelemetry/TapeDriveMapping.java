package com.sentrysoftware.hardware.agent.mapping.opentelemetry;

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
import com.sentrysoftware.matrix.common.meta.monitor.TapeDrive;

import lombok.AccessLevel;
import lombok.NoArgsConstructor;

@NoArgsConstructor(access = AccessLevel.PRIVATE)
public class TapeDriveMapping {

	private static final String UNMOUNT_ATTRIBUTE_VALUE = "unmount";
	private static final String MOUNT_ATTRIBUTE_VALUE = "mount";
	private static final String NEEDS_CLEANING_ATTRIBUTE_VALUE = "needs_cleaning";
	private static final String OPERATIONS_METRIC_DESCRIPTION = createCustomDescriptionWithAttributes(
		"Number of mount or unmount operations that occurred during the last collect interval",
		TYPE_ATTRIBUTE_KEY,
		MOUNT_ATTRIBUTE_VALUE, UNMOUNT_ATTRIBUTE_VALUE
	);
	private static final String MONITOR_TYPE = "tape drive";
	private static final String STATUS_METRIC_NAME = "hw.tape_drive.status";
	private static final String OPERATIONS_METRIC_NAME = "hw.tape_drive.operations";
	private static final String STATUS_METRIC_DESCRIPTION = createStatusDescription(
		MONITOR_TYPE,
		STATE_ATTRIBUTE_KEY,
		OK_ATTRIBUTE_VALUE, DEGRADED_ATTRIBUTE_VALUE, FAILED_ATTRIBUTE_VALUE, PRESENT_ATTRIBUTE_VALUE,
		NEEDS_CLEANING_ATTRIBUTE_VALUE
	);

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
			TapeDrive.NEEDS_CLEANING.getName(),
			Collections.singletonList(
				MetricInfo
					.builder()
					.name(STATUS_METRIC_NAME)
					.description(STATUS_METRIC_DESCRIPTION)
					.identifyingAttribute(
						StaticIdentifyingAttribute
							.builder()
							.key(STATE_ATTRIBUTE_KEY)
							.value(NEEDS_CLEANING_ATTRIBUTE_VALUE)
							.build()
					)
					.predicate(CLEANING_NEEDED_PREDICATE)
					.type(MetricType.UP_DOWN_COUNTER)
					.build()
			)
		);

		map.put(
			TapeDrive.MOUNT_COUNT.getName(),
			Collections.singletonList(
				MetricInfo
					.builder()
					.name(OPERATIONS_METRIC_NAME)
					.unit(OPERATIONS_UNIT)
					.description(OPERATIONS_METRIC_DESCRIPTION)
					.identifyingAttribute(
						StaticIdentifyingAttribute
							.builder()
							.key(TYPE_ATTRIBUTE_KEY)
							.value(MOUNT_ATTRIBUTE_VALUE)
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
					.name(OPERATIONS_METRIC_NAME)
					.unit(OPERATIONS_UNIT)
					.description(OPERATIONS_METRIC_DESCRIPTION)
					.identifyingAttribute(
						StaticIdentifyingAttribute
							.builder()
							.key(TYPE_ATTRIBUTE_KEY)
							.value(UNMOUNT_ATTRIBUTE_VALUE)
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
					.name("hw.tape_drive.errors.limit")
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
