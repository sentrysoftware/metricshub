package com.sentrysoftware.hardware.agent.mapping.opentelemetry;

import static com.sentrysoftware.hardware.agent.mapping.opentelemetry.MappingConstants.*;

import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;

import com.sentrysoftware.hardware.agent.mapping.opentelemetry.dto.MetricInfo;
import com.sentrysoftware.hardware.agent.mapping.opentelemetry.dto.StaticIdentifyingAttribute;
import com.sentrysoftware.hardware.agent.mapping.opentelemetry.dto.MetricInfo.MetricType;
import com.sentrysoftware.matrix.common.meta.monitor.Blade;
import com.sentrysoftware.matrix.common.meta.monitor.IMetaMonitor;

import lombok.AccessLevel;
import lombok.NoArgsConstructor;

@NoArgsConstructor(access = AccessLevel.PRIVATE)
public class BladeMapping {

	private static final String STATUS_METRIC_NAME = "hw.blade.status";
	private static final String POWER_STATE_METRIC_NAME = "hw.blade.power_state";
	private static final String MONITOR_TYPE = "blade";
	private static final String POWER_STATE_METRIC_DESCRIPTION = createPowerStateDescription(
		MONITOR_TYPE,
		STATE_ATTRIBUTE_KEY,
		OFF_ATTRIBUTE_VALUE, SUSPENDED_ATTRIBUTE_VALUE, ON_ATTRIBUTE_VALUE
	);
	private static final String STATUS_METRIC_DESCRIPTION = createStatusDescription(
		MONITOR_TYPE,
		STATE_ATTRIBUTE_KEY,
		OK_ATTRIBUTE_VALUE, DEGRADED_ATTRIBUTE_VALUE, FAILED_ATTRIBUTE_VALUE, PRESENT_ATTRIBUTE_VALUE
	);

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
					.description(
						STATUS_METRIC_DESCRIPTION
					)
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
			Blade.POWER_STATE.getName(),
			List.of(
				MetricInfo
					.builder()
					.name(POWER_STATE_METRIC_NAME)
					.description(POWER_STATE_METRIC_DESCRIPTION)
					.identifyingAttribute(
						StaticIdentifyingAttribute
						.builder()
							.key(STATE_ATTRIBUTE_KEY)
							.value(OFF_ATTRIBUTE_VALUE)
							.build()
						)
					.predicate(OFF_POWER_STATE_PREDICATE)
					.type(MetricType.UP_DOWN_COUNTER)
					.build(),
				MetricInfo
					.builder()
					.name(POWER_STATE_METRIC_NAME)
					.description(POWER_STATE_METRIC_DESCRIPTION)
					.identifyingAttribute(
						StaticIdentifyingAttribute
							.builder()
							.key(STATE_ATTRIBUTE_KEY)
							.value(SUSPENDED_ATTRIBUTE_VALUE)
							.build()
						)
					.predicate(SUSPENDED_POWER_STATE_PREDICATE)
					.type(MetricType.UP_DOWN_COUNTER)
					.build(),
				MetricInfo
					.builder()
					.name(POWER_STATE_METRIC_NAME)
					.description(POWER_STATE_METRIC_DESCRIPTION)
					.identifyingAttribute(
						StaticIdentifyingAttribute
							.builder()
							.key(STATE_ATTRIBUTE_KEY)
							.value(ON_ATTRIBUTE_VALUE)
							.build()
						)
					.predicate(ON_POWER_STATE_PREDICATE)
					.type(MetricType.UP_DOWN_COUNTER)
					.build()
			)	
		);

		return map;
	}
}
