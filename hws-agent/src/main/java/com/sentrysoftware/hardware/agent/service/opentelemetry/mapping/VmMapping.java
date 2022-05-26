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
import com.sentrysoftware.matrix.common.meta.monitor.Vm;

import lombok.AccessLevel;
import lombok.NoArgsConstructor;

@NoArgsConstructor(access = AccessLevel.PRIVATE)
public class VmMapping {

	private static final String VM_STATUS_METRIC_NAME = "hw.vm.status";
	private static final String VM_POWER_STATE_METRIC_NAME = "hw.vm.power_state";

	/**
	 * Build virtual machine metrics map
	 *
	 * @return  {@link Map} where the metrics are indexed by the matrix parameter name
	 */
	static Map<String, List<MetricInfo>> buildVmMetricsMapping() {
		final Map<String, List<MetricInfo>> map = new TreeMap<>(String.CASE_INSENSITIVE_ORDER);

		map.put(
			IMetaMonitor.STATUS.getName(),
			List.of(
				MetricInfo
					.builder()
					.name(VM_STATUS_METRIC_NAME)
					.description("Whether the virtual machine status is ok or not.")
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
					.name(VM_STATUS_METRIC_NAME)
					.description("Whether the virtual machine status is degraded or not.")
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
					.name(VM_STATUS_METRIC_NAME)
					.description("Whether the virtual machine status is failed or not.")
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
					.name(VM_STATUS_METRIC_NAME)
					.description("Whether the virtual machine is found or not.")
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
			Vm.POWER_STATE.getName(),
			List.of(
				MetricInfo
					.builder()
					.name(VM_POWER_STATE_METRIC_NAME)
					.description("Whether the state of the virtual machine is currently on or not.")
					.identifyingAttribute(
						StaticIdentifyingAttribute
							.builder()
							.key(STATE_ATTRIBUTE_KEY)
							.value(ON_ATTRIBUTE_VALUE)
							.build()
					)
					.predicate(ON_POWER_STATE_PREDICATE)
					.build(),
				MetricInfo
					.builder()
					.name(VM_POWER_STATE_METRIC_NAME)
					.description("Whether the state of the virtual machine is currently off or not.")
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
					.name(VM_POWER_STATE_METRIC_NAME)
					.description("Whether the state of the virtual machine is currently standby or not.")
					.identifyingAttribute(
						StaticIdentifyingAttribute
							.builder()
							.key(STATE_ATTRIBUTE_KEY)
							.value(SUSPENDED_ATTRIBUTE_VALUE)
							.build()
					)
					.predicate(SUSPENDED_POWER_STATE_PREDICATE)
					.build()
			)
		);

		map.put(
			Vm.POWER_SHARE.getName(),
			Collections.singletonList(
				MetricInfo
					.builder()
					.name("hw.vm.power_ratio")
					.description("Ratio of host power consumed by the virtual machine.")
					.unit(RATIO_UNIT)
					.factor(RATIO_FACTOR)
					.build()
			)
		);

		map.put(
			IMetaMonitor.ENERGY.getName(),
			Collections.singletonList(
				MetricInfo
					.builder()
					.name("hw.vm.energy")
					.unit(JOULES_UNIT)
					.type(MetricType.COUNTER)
					.description("Energy consumed by the virtual machine since the start of the Hardware Sentry Agent.")
					.build()
			)
		);

		map.put(
			IMetaMonitor.POWER_CONSUMPTION.getName(),
			Collections.singletonList(
				MetricInfo
					.builder()
					.name("hw.vm.power")
					.unit(WATTS_UNIT)
					.description("Energy consumed by the virtual machine.")
					.build()
			)
		);

		return map;
	}
}
