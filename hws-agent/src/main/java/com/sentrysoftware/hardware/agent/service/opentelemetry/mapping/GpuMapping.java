package com.sentrysoftware.hardware.agent.service.opentelemetry.mapping;

import static com.sentrysoftware.hardware.agent.service.opentelemetry.mapping.MappingConstants.*;
import static com.sentrysoftware.matrix.common.helpers.HardwareConstants.*;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;

import com.sentrysoftware.hardware.agent.dto.metric.MetricInfo;
import com.sentrysoftware.hardware.agent.dto.metric.StaticIdentifyingAttribute;
import com.sentrysoftware.hardware.agent.dto.metric.MetricInfo.MetricType;
import com.sentrysoftware.matrix.common.meta.monitor.IMetaMonitor;
import com.sentrysoftware.matrix.common.meta.monitor.Gpu;

import lombok.AccessLevel;
import lombok.NoArgsConstructor;

@NoArgsConstructor(access = AccessLevel.PRIVATE)
public class GpuMapping {

	private static final String GPU_STATUS_METRIC_NAME = "hw.gpu.status";
	private static final String GPU_ERROR_METRIC_NAME = "hw.gpu.errors";
	private static final String GPU_IO_METRIC_NAME = "hw.gpu.io";
	private static final String GPU_UTILIZATION_METRIC_NAME = "hw.gpu.utilization";
	private static final String MONITOR_TYPE = "GPU";

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
					.name(GPU_STATUS_METRIC_NAME)
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
					.name(GPU_STATUS_METRIC_NAME)
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
					.name(GPU_STATUS_METRIC_NAME)
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
			IMetaMonitor.PREDICTED_FAILURE.getName(),
			Collections.singletonList(
				MetricInfo
					.builder()
					.name(GPU_STATUS_METRIC_NAME)
					.description(createStatusDescription(MONITOR_TYPE, PREDICTED_FAILURE_ATTRIBUTE_VALUE))
					.identifyingAttribute(
						StaticIdentifyingAttribute
							.builder()
							.key(STATE_ATTRIBUTE_KEY)
							.value(PREDICTED_FAILURE_ATTRIBUTE_VALUE)
							.build()	
					)
					.predicate(PREDICTED_FAILURE_PREDICATE)
					.build()
			)	
		);

		map.put(
			IMetaMonitor.PRESENT.getName(),
			Collections.singletonList(
				MetricInfo
					.builder()
					.name(GPU_STATUS_METRIC_NAME)
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
			Gpu.ERROR_COUNT.getName(),
			Collections.singletonList(
				MetricInfo
					.builder()
					.name(GPU_ERROR_METRIC_NAME)
					.unit(ERRORS_UNIT)
					.identifyingAttribute(
						StaticIdentifyingAttribute
							.builder()
							.key(TYPE_ATTRIBUTE_KEY)
							.value(ALL_ATTRIBUTE_VALUE)
							.build()
					)
					.type(MetricType.COUNTER)
					.description("Number of errors encountered by the GPU since the start of the Hardware Sentry Agent.")
					.build()
			)
		);

		map.put(
			Gpu.CORRECTED_ERROR_COUNT.getName(),
			Collections.singletonList(
				MetricInfo
					.builder()
					.name(GPU_ERROR_METRIC_NAME)
					.unit(ERRORS_UNIT)
					.identifyingAttribute(
						StaticIdentifyingAttribute
							.builder()
							.key(TYPE_ATTRIBUTE_KEY)
							.value(CORRECTED_ATTRIBUTE_VALUE)
							.build()
					)
					.type(MetricType.COUNTER)
					.description("Number of detected and corrected errors.")
					.build()
			)
		);

		map.put(
			Gpu.RECEIVED_BYTES.getName(),
			Collections.singletonList(
				MetricInfo
					.builder()
					.name(GPU_IO_METRIC_NAME)
					.unit(BYTES_UNIT)
					.identifyingAttribute(
						StaticIdentifyingAttribute
							.builder()
							.key(DIRECTION_ATTRIBUTE_KEY)
							.value(RECEIVE_ATTRIBUTE_VALUE)
							.build()
					)
					.type(MetricType.COUNTER)
					.description("Number of bytes received through the GPU.")
					.build()
			)
		);

		map.put(
			Gpu.TRANSMITTED_BYTES.getName(),
			Collections.singletonList(
				MetricInfo
					.builder()
					.name(GPU_IO_METRIC_NAME)
					.unit(BYTES_UNIT)
					.identifyingAttribute(
						StaticIdentifyingAttribute
							.builder()
							.key(DIRECTION_ATTRIBUTE_KEY)
							.value(TRANSMIT_ATTRIBUTE_VALUE)
							.build()
					)
					.type(MetricType.COUNTER)
					.description("Number of bytes transmitted through the GPU.")
					.build()
			)
		);

		map.put(
			Gpu.DECODER_USED_TIME.getName(),
			Collections.singletonList(
				MetricInfo
					.builder()
					.name(GPU_UTILIZATION_METRIC_NAME)
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
					.description("Ratio of time spent by the GPU decoding videos.")
					.build()
			)
		);

		map.put(
			Gpu.ENCODER_USED_TIME.getName(),
			Collections.singletonList(
				MetricInfo
					.builder()
					.name(GPU_UTILIZATION_METRIC_NAME)
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
					.description("Ratio of time spent by the GPU encoding videos.")
					.build()
			)
		);

		map.put(
			Gpu.USED_TIME.getName(),
			Collections.singletonList(
				MetricInfo
					.builder()
					.name(GPU_UTILIZATION_METRIC_NAME)
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
					.description("Ratio of time spent by the GPU doing any work.")
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
					.name("hw.gpu.energy")
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
					.name("hw.gpu.power")
					.unit(WATTS_UNIT)
					.description(createPowerConsumptionDescription(MONITOR_TYPE))
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
					.description("GPU memory utilization ratio that will generate an alarm when reached.")
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
					.description("GPU memory utilization ratio that will generate a warning when reached.")
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
					.description("GPU used time ratio that will generate an alarm when reached.")
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
					.description("GPU used time ratio that will generate a warning when reached.")
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
