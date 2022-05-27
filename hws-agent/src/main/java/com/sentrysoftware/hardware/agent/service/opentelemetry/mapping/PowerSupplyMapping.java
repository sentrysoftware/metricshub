package com.sentrysoftware.hardware.agent.service.opentelemetry.mapping;

import static com.sentrysoftware.hardware.agent.service.opentelemetry.mapping.MappingConstants.*;
import static com.sentrysoftware.matrix.common.helpers.HardwareConstants.POWER_SUPPLY_POWER;

import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;

import com.sentrysoftware.hardware.agent.dto.metric.MetricInfo;
import com.sentrysoftware.hardware.agent.dto.metric.StaticIdentifyingAttribute;
import com.sentrysoftware.matrix.common.meta.monitor.IMetaMonitor;
import com.sentrysoftware.matrix.common.meta.monitor.PowerSupply;

import lombok.AccessLevel;
import lombok.NoArgsConstructor;

@NoArgsConstructor(access = AccessLevel.PRIVATE)
public class PowerSupplyMapping {

	private static final String POWER_SUPPLY_TYPE = "power supply";
	private static final String POWER_SUPPLY_STATUS_METRIC_NAME = "hw.power_supply.status";

	/**
	 * Build power supply metrics map
	 *
	 * @return  {@link Map} where the metrics are indexed by the matrix parameter name
	 */
	static Map<String, List<MetricInfo>> buildPowerSupplyMetricsMapping() {
		final Map<String, List<MetricInfo>> map = new TreeMap<>(String.CASE_INSENSITIVE_ORDER);

		map.put(
			IMetaMonitor.STATUS.getName(),
			List.of(
				MetricInfo
					.builder()
					.name(POWER_SUPPLY_STATUS_METRIC_NAME)
					.description(MappingConstants.createStatusDescription(POWER_SUPPLY_TYPE, OK_ATTRIBUTE_VALUE))
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
					.name(POWER_SUPPLY_STATUS_METRIC_NAME)
					.description(MappingConstants.createStatusDescription(POWER_SUPPLY_TYPE, DEGRADED_ATTRIBUTE_VALUE))
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
					.name(POWER_SUPPLY_STATUS_METRIC_NAME)
					.description(MappingConstants.createStatusDescription(POWER_SUPPLY_TYPE, FAILED_ATTRIBUTE_VALUE))
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
					.name(POWER_SUPPLY_STATUS_METRIC_NAME)
					.description(MappingConstants.createPresentDescription(POWER_SUPPLY_TYPE))
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
			PowerSupply.USED_CAPACITY.getName(),
			Collections.singletonList(
				MetricInfo
					.builder()
					.name("hw.power_supply.utilization")
					.factor(RATIO_FACTOR)
					.unit(RATIO_UNIT)
					.description("Ratio of the power supply power currently in use.")
					.build()
			)
		);

		return map;
	}

	/**
	 * Create PowerSupply Metadata to metrics map
	 * 
	 * @return {@link Map} of {@link MetricInfo} instances indexed by the matrix parameter names
	 */
	static Map<String, List<MetricInfo>> powerSupplyMetadataToMetrics() {
		final Map<String, List<MetricInfo>> map = new TreeMap<>(String.CASE_INSENSITIVE_ORDER);

		map.put(
			POWER_SUPPLY_POWER,
			Collections.singletonList(
				MetricInfo
					.builder()
					.name("hw.power_supply.power")
					.unit(WATTS_UNIT)
					.description("Maximum power output.")
					.build()
			)
		);

		return map;
	}

}
