package com.sentrysoftware.hardware.agent.mapping.opentelemetry;

import static com.sentrysoftware.hardware.agent.mapping.opentelemetry.MappingConstants.*;

import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;

import com.sentrysoftware.matrix.common.meta.monitor.IMetaMonitor;
import com.sentrysoftware.hardware.agent.mapping.opentelemetry.dto.MetricInfo;
import com.sentrysoftware.hardware.agent.mapping.opentelemetry.dto.StaticIdentifyingAttribute;
import com.sentrysoftware.hardware.agent.mapping.opentelemetry.dto.MetricInfo.MetricType;
import com.sentrysoftware.matrix.common.meta.monitor.CpuCore;

import lombok.AccessLevel;
import lombok.NoArgsConstructor;

@NoArgsConstructor(access = AccessLevel.PRIVATE)
public class CpuCoreMapping {

	public static final String HW_TYPE_ATTRIBUTE_VALUE = "cpu_core";

	/**
	 * Build cpu core metrics map
	 *
	 * @return  {@link Map} where the metrics are indexed by the matrix parameter name
	 */
	static Map<String, List<MetricInfo>> buildCpuCoreMetricsMapping() {
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
			CpuCore.USED_TIME_PERCENT.getName(),
			Collections.singletonList(
				MetricInfo
					.builder()
					.name("hw.cpu_core.utilization")
					.description("Ratio of the CPU core usage.")
					.unit(RATIO_UNIT)
					.factor(RATIO_FACTOR)
					.build()
			)
		);

		map.put(
			CpuCore.CURRENT_SPEED.getName(),
			Collections.singletonList(
				MetricInfo
					.builder()
					.name("hw.cpu_core.speed")
					.unit(HERTZ_UNIT)
					.factor(MHZ_TO_HZ_FACTOR)
					.description("Current speed of the CPU core.")
					.build()
			)
		);

		return map;
	}
}
