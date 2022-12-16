package com.sentrysoftware.hardware.agent.mapping.opentelemetry;

import static com.sentrysoftware.hardware.agent.mapping.opentelemetry.MappingConstants.*;
import static com.sentrysoftware.matrix.common.helpers.HardwareConstants.*;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;

import com.sentrysoftware.matrix.common.meta.monitor.IMetaMonitor;
import com.sentrysoftware.hardware.agent.mapping.opentelemetry.dto.MetricInfo;
import com.sentrysoftware.hardware.agent.mapping.opentelemetry.dto.StaticIdentifyingAttribute;
import com.sentrysoftware.hardware.agent.mapping.opentelemetry.dto.MetricInfo.MetricType;
import com.sentrysoftware.matrix.common.meta.monitor.Gpu;

import lombok.AccessLevel;
import lombok.NoArgsConstructor;

@NoArgsConstructor(access = AccessLevel.PRIVATE)
public class GpuMapping {

	private static final String RECEIVED_AND_TRANSMITTED_BYTES_BY_THE_GPU = "Total number of bytes transmitted and received through the GPU. Attribute: direction = `transmit` and `receive`.";

	private static final String UTILIZATION_METRIC_NAME = "hw.gpu.utilization";
	public static final String HW_TYPE_ATTRIBUTE_VALUE = "gpu";
	private static final String UTILIZATION_METRIC_DESCRIPTION = createCustomDescriptionWithAttributes(
		"Ratio of time spent by the GPU",
		TASK_ATTRIBUTE_KEY,
		DECODER_ATTRIBUTE_VALUE, ENCODER_ATTRIBUTE_VALUE, GENERAL_ATTRIBUTE_VALUE
		);
	private static final String MEMORY_UTILIZATION_LIMIT_METRIC_DESCRIPTION = createCustomDescriptionWithAttributes(
		"GPU memory utilization ratio that will generate a warning or an alarm when reached",
		LIMIT_TYPE_ATTRIBUTE_KEY,
		CRITICAL_ATTRIBUTE_VALUE, DEGRADED_ATTRIBUTE_VALUE
	);
	private static final String UTILIZATION_LIMIT_METRIC_DESCRIPTION = createCustomDescriptionWithAttributes(
		"GPU used time ratio that will generate a warning or an alarm when reached",
		LIMIT_TYPE_ATTRIBUTE_KEY,
		CRITICAL_ATTRIBUTE_VALUE, DEGRADED_ATTRIBUTE_VALUE
	);

	/**
	 * Build GPU metrics map
	 *
	 * @return  {@link Map} where the metrics are indexed by the matrix parameter name
	 */
	static Map<String, List<MetricInfo>> buildGpuMetricsMapping() {
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
			IMetaMonitor.PREDICTED_FAILURE.getName(),
			Collections.singletonList(
				MetricInfo
					.builder()
					.name(STATUS_METRIC_NAME)
					.description(STATUS_METRIC_DESCRIPTION)
					.identifyingAttribute(
						StaticIdentifyingAttribute
							.builder()
							.key(STATE_ATTRIBUTE_KEY)
							.value(PREDICTED_FAILURE_ATTRIBUTE_VALUE)
							.build()	
					)
					.identifyingAttribute(
						StaticIdentifyingAttribute
							.builder()
							.key(HW_TYPE_ATTRIBUTE_KEY)
							.value(HW_TYPE_ATTRIBUTE_VALUE)
							.build()
					)
					.predicate(PREDICTED_FAILURE_PREDICATE)
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
			Gpu.ERROR_COUNT.getName(),
			Collections.singletonList(
				MetricInfo
					.builder()
					.name(ERRORS_METRIC_NAME)
					.type(MetricType.COUNTER)
					.unit(ERRORS_UNIT)
					.description(ERRORS_METRIC_DESCRIPTION)
					.identifyingAttribute(
						StaticIdentifyingAttribute
							.builder()
							.key(HW_TYPE_ATTRIBUTE_KEY)
							.value(HW_TYPE_ATTRIBUTE_VALUE)
							.build()
					)
					.identifyingAttribute(
						StaticIdentifyingAttribute
							.builder()
							.key(TYPE_ATTRIBUTE_KEY)
							.value(ALL_ATTRIBUTE_VALUE)
							.build()
					)
					.build()
			)
		);

		map.put(
			Gpu.CORRECTED_ERROR_COUNT.getName(),
			Collections.singletonList(
				MetricInfo
					.builder()
					.name(ERRORS_METRIC_NAME)
					.type(MetricType.COUNTER)
					.unit(ERRORS_UNIT)
					.description(ERRORS_METRIC_DESCRIPTION)
					.identifyingAttribute(
						StaticIdentifyingAttribute
							.builder()
							.key(HW_TYPE_ATTRIBUTE_KEY)
							.value(HW_TYPE_ATTRIBUTE_VALUE)
							.build()
					)
					.identifyingAttribute(
						StaticIdentifyingAttribute
							.builder()
							.key(TYPE_ATTRIBUTE_KEY)
							.value(CORRECTED_ATTRIBUTE_VALUE)
							.build()
					)
					.build()
			)
		);

		map.put(
			Gpu.RECEIVED_BYTES.getName(),
			List.of(
				MetricInfo
					.builder()
					.name("hw.gpu.io.receive")
					.unit(BYTES_UNIT)
					.type(MetricType.COUNTER)
					.description("Number of bytes received through the GPU.")
					.build(),
				MetricInfo
					.builder()
					.name("hw.gpu.io")
					.unit(BYTES_UNIT)
					.type(MetricType.COUNTER)
					.description(RECEIVED_AND_TRANSMITTED_BYTES_BY_THE_GPU)
					.identifyingAttribute(
						StaticIdentifyingAttribute
							.builder()
							.key(DIRECTION_ATTRIBUTE_KEY)
							.value(RECEIVE_ATTRIBUTE_VALUE)
							.build()
					)
					.build()
			)
		);

		map.put(
			Gpu.TRANSMITTED_BYTES.getName(),
			List.of(
				MetricInfo
					.builder()
					.name("hw.gpu.io.transmit")
					.unit(BYTES_UNIT)
					.type(MetricType.COUNTER)
					.description("Number of bytes transmitted through the GPU.")
					.build(),
				MetricInfo
					.builder()
					.name("hw.gpu.io")
					.unit(BYTES_UNIT)
					.type(MetricType.COUNTER)
					.description(RECEIVED_AND_TRANSMITTED_BYTES_BY_THE_GPU)
					.identifyingAttribute(
						StaticIdentifyingAttribute
							.builder()
							.key(DIRECTION_ATTRIBUTE_KEY)
							.value(TRANSMIT_ATTRIBUTE_VALUE)
							.build()
					)
					.build()
			)
		);

		map.put(
			Gpu.DECODER_USED_TIME_PERCENT.getName(),
			Collections.singletonList(
				MetricInfo
					.builder()
					.name(UTILIZATION_METRIC_NAME)
					.unit(RATIO_UNIT)
					.factor(RATIO_FACTOR)
					.identifyingAttribute(
						StaticIdentifyingAttribute
							.builder()
							.key(TASK_ATTRIBUTE_KEY)
							.value(DECODER_ATTRIBUTE_VALUE)
							.build()
					)
					.type(MetricType.GAUGE)
					.description(UTILIZATION_METRIC_DESCRIPTION)
					.build()
			)
		);

		map.put(
			Gpu.ENCODER_USED_TIME_PERCENT.getName(),
			Collections.singletonList(
				MetricInfo
					.builder()
					.name(UTILIZATION_METRIC_NAME)
					.unit(RATIO_UNIT)
					.factor(RATIO_FACTOR)
					.identifyingAttribute(
						StaticIdentifyingAttribute
							.builder()
							.key(TASK_ATTRIBUTE_KEY)
							.value(ENCODER_ATTRIBUTE_VALUE)
							.build()
					)
					.type(MetricType.GAUGE)
					.description(UTILIZATION_METRIC_DESCRIPTION)
					.build()
			)
		);

		map.put(
			Gpu.USED_TIME_PERCENT.getName(),
			Collections.singletonList(
				MetricInfo
					.builder()
					.name(UTILIZATION_METRIC_NAME)
					.unit(RATIO_UNIT)
					.factor(RATIO_FACTOR)
					.identifyingAttribute(
						StaticIdentifyingAttribute
							.builder()
							.key(TASK_ATTRIBUTE_KEY)
							.value(GENERAL_ATTRIBUTE_VALUE)
							.build()
					)
					.type(MetricType.GAUGE)
					.description(UTILIZATION_METRIC_DESCRIPTION)
					.build()
			)
		);

		map.put(
			Gpu.MEMORY_UTILIZATION.getName(),
			Collections.singletonList(
				MetricInfo
					.builder()
					.name("hw.gpu.memory.utilization")
					.unit(RATIO_UNIT)
					.factor(RATIO_FACTOR)
					.type(MetricType.GAUGE)
					.description("GPU memory utilization ratio.")
					.build()
			)
		);

		map.put(
			IMetaMonitor.ENERGY.getName(),
			Collections.singletonList(
				MetricInfo
					.builder()
					.name("hw.energy")
					.unit(JOULES_UNIT)
					.type(MetricType.COUNTER)
					.description(ENERGY_METRIC_DESCRIPTION)
					.identifyingAttribute(
						StaticIdentifyingAttribute
							.builder()
							.key(HW_TYPE_ATTRIBUTE_KEY)
							.value(HW_TYPE_ATTRIBUTE_VALUE)
							.build()
					)
					.build()
			)
		);

		map.put(
			IMetaMonitor.POWER_CONSUMPTION.getName(),
			Collections.singletonList(
				MetricInfo
					.builder()
					.name("hw.power")
					.unit(WATTS_UNIT)
					.type(MetricType.GAUGE)
					.description(POWER_METRIC_DESCRIPTION)
					.identifyingAttribute(
						StaticIdentifyingAttribute
							.builder()
							.key(HW_TYPE_ATTRIBUTE_KEY)
							.value(HW_TYPE_ATTRIBUTE_VALUE)
							.build()
					)
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
	static Map<String, List<MetricInfo>> gpuMetadataToMetrics() {
		final Map<String, List<MetricInfo>> map = new TreeMap<>(String.CASE_INSENSITIVE_ORDER);

		map.put(
			SIZE,
			Collections.singletonList(
				MetricInfo
					.builder()
					.name("hw.gpu.memory.limit")
					.unit(BYTES_UNIT)
					.factor(MEGABYTES_TO_BYTES_FACTOR)
					.description("GPU memory size.")
					.build()
			)
		);

		map.put(
			MEMORY_UTILIZATION_ALARM_THRESHOLD,
			Collections.singletonList(
				MetricInfo
					.builder()
					.name("hw.gpu.memory.utilization.limit")
					.unit(RATIO_UNIT)
					.factor(RATIO_FACTOR)
					.type(MetricType.GAUGE)
					.description(MEMORY_UTILIZATION_LIMIT_METRIC_DESCRIPTION)
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

		map.put(
			MEMORY_UTILIZATION_WARNING_THRESHOLD,
			Collections.singletonList(
				MetricInfo
					.builder()
					.name("hw.gpu.memory.utilization.limit")
					.unit(RATIO_UNIT)
					.factor(RATIO_FACTOR)
					.type(MetricType.GAUGE)
					.description(MEMORY_UTILIZATION_LIMIT_METRIC_DESCRIPTION)
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
			USED_TIME_PERCENT_ALARM_THRESHOLD,
			Collections.singletonList(
				MetricInfo
					.builder()
					.name("hw.gpu.utilization.limit")
					.unit(RATIO_UNIT)
					.factor(RATIO_FACTOR)
					.type(MetricType.GAUGE)
					.description(UTILIZATION_LIMIT_METRIC_DESCRIPTION)
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

		map.put(
			USED_TIME_PERCENT_WARNING_THRESHOLD,
			Collections.singletonList(
				MetricInfo
					.builder()
					.name("hw.gpu.utilization.limit")
					.unit(RATIO_UNIT)
					.factor(RATIO_FACTOR)
					.type(MetricType.GAUGE)
					.description(UTILIZATION_LIMIT_METRIC_DESCRIPTION)
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

		return map;
	}
}
