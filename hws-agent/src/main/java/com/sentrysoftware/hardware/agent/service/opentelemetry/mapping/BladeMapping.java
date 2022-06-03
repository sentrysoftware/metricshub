package com.sentrysoftware.hardware.agent.service.opentelemetry.mapping;

import static com.sentrysoftware.hardware.agent.service.opentelemetry.mapping.MappingConstants.*;

import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;

import com.sentrysoftware.hardware.agent.dto.metric.MetricInfo;
import com.sentrysoftware.hardware.agent.dto.metric.StaticIdentifyingAttribute;

import com.sentrysoftware.matrix.common.meta.monitor.Blade;
import com.sentrysoftware.matrix.common.meta.monitor.IMetaMonitor;

import lombok.AccessLevel;
import lombok.NoArgsConstructor;

@NoArgsConstructor(access = AccessLevel.PRIVATE)
public class BladeMapping {

	private static final String BLADE_STATUS_METRIC_NAME = "hw.blade.status";
	private static final String BLADE_POWER_STATE_METRIC_NAME = "hw.blade.power_state";
	private static final String MONITOR_TYPE = "blade";

	/**
	 * Build battery metrics map
	 *
	 * @return {@link Map} where the metrics are indexed by the matrix parameter name
	 */
	static Map<String, List<MetricInfo>> buildBladeMetricsMapping() {
		final Map<String, List<MetricInfo>> map = new TreeMap<>(String.CASE_INSENSITIVE_ORDER);

		map.put(
			IMetaMonitor.STATUS.getName(),
			List.of(
				MetricInfo
					.builder()
					.name(BLADE_STATUS_METRIC_NAME)
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
					.name(BLADE_STATUS_METRIC_NAME)
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
					.name(BLADE_STATUS_METRIC_NAME)
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
					.name(BLADE_STATUS_METRIC_NAME)
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
			Blade.POWER_STATE.getName(),
			List.of(
				MetricInfo
					.builder()
					.name(BLADE_POWER_STATE_METRIC_NAME)
					.description(createPowerStateDescription(MONITOR_TYPE, OFF_ATTRIBUTE_VALUE))
					.identifyingAttribute(
						StaticIdentifyingAttribute
						.builder()
							.key(STATE_ATTRIBUTE_KEY)
							.value(OFF_ATTRIBUTE_VALUE)
							.build()
						)
					.predicate(OFF_POWER_STATE_PREDICATE)
					.build(),
				MetricInfo
					.builder()
					.name(BLADE_POWER_STATE_METRIC_NAME)
					.description(createPowerStateDescription(MONITOR_TYPE, SUSPENDED_ATTRIBUTE_VALUE))
					.identifyingAttribute(
						StaticIdentifyingAttribute
							.builder()
							.key(STATE_ATTRIBUTE_KEY)
							.value(SUSPENDED_ATTRIBUTE_VALUE)
							.build()
						)
					.predicate(SUSPENDED_POWER_STATE_PREDICATE)
					.build(),
				MetricInfo
					.builder()
					.name(BLADE_POWER_STATE_METRIC_NAME)
					.description(createPowerStateDescription(MONITOR_TYPE, ON_ATTRIBUTE_VALUE))
					.identifyingAttribute(
						StaticIdentifyingAttribute
							.builder()
							.key(STATE_ATTRIBUTE_KEY)
							.value(ON_ATTRIBUTE_VALUE)
							.build()
						)
					.predicate(ON_POWER_STATE_PREDICATE)
					.build()
			)	
		);

		return map;
	}
}
