package com.sentrysoftware.hardware.agent.service.opentelemetry.mapping;

import static com.sentrysoftware.hardware.agent.service.opentelemetry.mapping.MappingConstants.*;

import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;

import com.sentrysoftware.hardware.agent.dto.metric.MetricInfo;
import com.sentrysoftware.hardware.agent.dto.metric.StaticIdentifyingAttribute;
import com.sentrysoftware.hardware.agent.dto.metric.MetricInfo.MetricType;
import com.sentrysoftware.matrix.common.meta.monitor.IMetaMonitor;
import com.sentrysoftware.matrix.common.meta.monitor.CpuCore;

import lombok.AccessLevel;
import lombok.NoArgsConstructor;

@NoArgsConstructor(access = AccessLevel.PRIVATE)
public class CpuCoreMapping {

	private static final String CPU_CORE_STATUS_METRIC_NAME = "hw.cpu_core.status";
	private static final String CPU_CORE_NAME = "CPU core";

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
					.name(CPU_CORE_STATUS_METRIC_NAME)
					.description(createStatusDescription(CPU_CORE_NAME, OK_ATTRIBUTE_VALUE))
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
					.name(CPU_CORE_STATUS_METRIC_NAME)
					.description(createStatusDescription(CPU_CORE_NAME, DEGRADED_ATTRIBUTE_VALUE))
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
					.name(CPU_CORE_STATUS_METRIC_NAME)
					.description(createStatusDescription(CPU_CORE_NAME, FAILED_ATTRIBUTE_VALUE))
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
					.name(CPU_CORE_STATUS_METRIC_NAME)
					.description(createPresentDescription(CPU_CORE_NAME))
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
					.type(MetricType.COUNTER)
					.description("Current speed of the CPU core.")
					.build()
			)
		);

		return map;
	}
}
