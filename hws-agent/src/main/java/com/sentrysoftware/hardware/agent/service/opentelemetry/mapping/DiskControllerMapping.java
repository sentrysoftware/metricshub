package com.sentrysoftware.hardware.agent.service.opentelemetry.mapping;

import static com.sentrysoftware.hardware.agent.service.opentelemetry.mapping.MappingConstants.*;

import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;

import com.sentrysoftware.hardware.agent.dto.metric.MetricInfo;
import com.sentrysoftware.hardware.agent.dto.metric.MetricInfo.MetricType;
import com.sentrysoftware.hardware.agent.dto.metric.StaticIdentifyingAttribute;
import com.sentrysoftware.matrix.common.meta.monitor.DiskController;
import com.sentrysoftware.matrix.common.meta.monitor.IMetaMonitor;

import lombok.AccessLevel;
import lombok.NoArgsConstructor;

@NoArgsConstructor(access = AccessLevel.PRIVATE)
public class DiskControllerMapping {

	private static final String DISK_CONTROLLER_ADDITIONAL_ID = "1";
	private static final String DISK_CONTROLLER_STATUS_METRIC_NAME = "hw.disk_controller.status";
	private static final String DISK_CONTROLLER_NAME = "disk controller";
	private static final String DISK_CONTROLLER_BATTERY_NAME = DISK_CONTROLLER_NAME + " " + "battery";

	/**
	 * Build disk controller metrics map
	 *
	 * @return  {@link Map} where the metrics are indexed by the matrix parameter name
	 */
	static Map<String, List<MetricInfo>> buildDiskControllerMetricsMapping() {
		final Map<String, List<MetricInfo>> map = new TreeMap<>(String.CASE_INSENSITIVE_ORDER);

		map.put(
			IMetaMonitor.STATUS.getName(),
			List.of(
				MetricInfo
					.builder()
					.name(DISK_CONTROLLER_STATUS_METRIC_NAME)
					.description(createStatusDescription(DISK_CONTROLLER_NAME, OK_ATTRIBUTE_VALUE))
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
					.name(DISK_CONTROLLER_STATUS_METRIC_NAME)
					.description(createStatusDescription(DISK_CONTROLLER_NAME, DEGRADED_ATTRIBUTE_VALUE))
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
					.name(DISK_CONTROLLER_STATUS_METRIC_NAME)
					.description(createStatusDescription(DISK_CONTROLLER_NAME, FAILED_ATTRIBUTE_VALUE))
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
					.name(DISK_CONTROLLER_STATUS_METRIC_NAME)
					.description(createPresentDescription(DISK_CONTROLLER_NAME))
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
			DiskController.BATTERY_STATUS.getName(),
			List.of(
				MetricInfo
					.builder()
					.name(DISK_CONTROLLER_STATUS_METRIC_NAME)
					.description(createStatusDescription(DISK_CONTROLLER_BATTERY_NAME, OK_ATTRIBUTE_VALUE))
					.identifyingAttribute(
						StaticIdentifyingAttribute
							.builder()
							.key(BATTERY_STATE_ATTRIBUTE_KEY)
							.value(OK_ATTRIBUTE_VALUE)
							.build()
					)
					.predicate(OK_STATUS_PREDICATE)
					.build(),
				MetricInfo
					.builder()
					.name(DISK_CONTROLLER_STATUS_METRIC_NAME)
					.description(createStatusDescription(DISK_CONTROLLER_BATTERY_NAME, DEGRADED_ATTRIBUTE_VALUE))
					.identifyingAttribute(
						StaticIdentifyingAttribute
							.builder()
							.key(BATTERY_STATE_ATTRIBUTE_KEY)
							.value(DEGRADED_ATTRIBUTE_VALUE)
							.build()
					)
					.predicate(DEGRADED_STATUS_PREDICATE)
					.build(),
				MetricInfo
					.builder()
					.name(DISK_CONTROLLER_STATUS_METRIC_NAME)
					.description(createStatusDescription(DISK_CONTROLLER_BATTERY_NAME, FAILED_ATTRIBUTE_VALUE))
					.identifyingAttribute(
						StaticIdentifyingAttribute
							.builder()
							.key(BATTERY_STATE_ATTRIBUTE_KEY)
							.value(FAILED_ATTRIBUTE_VALUE)
							.build()
					)
					.predicate(FAILED_STATUS_PREDICATE)
					.build()
			)
		);

		map.put(
			DiskController.CONTROLLER_STATUS.getName(),
			List.of(
				MetricInfo
					.builder()
					.name(DISK_CONTROLLER_STATUS_METRIC_NAME)
					.description(createStatusDescription(DISK_CONTROLLER_NAME, OK_ATTRIBUTE_VALUE))
					.identifyingAttribute(
						StaticIdentifyingAttribute
							.builder()
							.key(STATE_ATTRIBUTE_KEY)
							.value(OK_ATTRIBUTE_VALUE)
							.build()
					)
					.predicate(OK_STATUS_PREDICATE)
					.additionalId(DISK_CONTROLLER_ADDITIONAL_ID)
					.build(),
				MetricInfo
					.builder()
					.name(DISK_CONTROLLER_STATUS_METRIC_NAME)
					.description(createStatusDescription(DISK_CONTROLLER_NAME, DEGRADED_ATTRIBUTE_VALUE))
					.identifyingAttribute(
						StaticIdentifyingAttribute
							.builder()
							.key(STATE_ATTRIBUTE_KEY)
							.value(DEGRADED_ATTRIBUTE_VALUE)
							.build()
					)
					.predicate(DEGRADED_STATUS_PREDICATE)
					.additionalId(DISK_CONTROLLER_ADDITIONAL_ID)
					.build(),
				MetricInfo
					.builder()
					.name(DISK_CONTROLLER_STATUS_METRIC_NAME)
					.description(createStatusDescription(DISK_CONTROLLER_NAME, FAILED_ATTRIBUTE_VALUE))
					.identifyingAttribute(
						StaticIdentifyingAttribute
							.builder()
							.key(STATE_ATTRIBUTE_KEY)
							.value(FAILED_ATTRIBUTE_VALUE)
							.build()
					)
					.predicate(FAILED_STATUS_PREDICATE)
					.additionalId(DISK_CONTROLLER_ADDITIONAL_ID)
					.build()
			)
		);

		map.put(
			IMetaMonitor.ENERGY.getName(),
			Collections.singletonList(
				MetricInfo
					.builder()
					.name("hw.disk_controller.energy")
					.unit(JOULES_UNIT)
					.type(MetricType.COUNTER)
					.description(createEnergyDescription(DISK_CONTROLLER_NAME))
					.build()
			)
		);

		map.put(
			IMetaMonitor.POWER_CONSUMPTION.getName(),
			Collections.singletonList(
				MetricInfo
					.builder()
					.name("hw.disk_controller.power")
					.unit(WATTS_UNIT)
					.description(createPowerConsumptionDescription(DISK_CONTROLLER_NAME))
					.build()
			)
		);

		return map;
	}

}
