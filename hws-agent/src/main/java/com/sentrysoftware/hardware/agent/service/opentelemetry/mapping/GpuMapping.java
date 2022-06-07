package com.sentrysoftware.hardware.agent.service.opentelemetry.mapping;

import static com.sentrysoftware.hardware.agent.service.opentelemetry.mapping.MappingConstants.*;
import static com.sentrysoftware.matrix.common.helpers.HardwareConstants.*;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;

import com.sentrysoftware.hardware.agent.dto.metric.MetricInfo;
import com.sentrysoftware.hardware.agent.dto.metric.MetricInfo.MetricType;
import com.sentrysoftware.hardware.agent.dto.metric.StaticIdentifyingAttribute;
import com.sentrysoftware.matrix.common.meta.monitor.IMetaMonitor;
import com.sentrysoftware.matrix.common.meta.monitor.Gpu;

import lombok.AccessLevel;
import lombok.NoArgsConstructor;

@NoArgsConstructor(access = AccessLevel.PRIVATE)
public class GpuMapping {

	private static final String STATUS_METRIC_NAME = "hw.gpu.status";
	private static final String ERROR_METRIC_NAME = "hw.gpu.errors";
	private static final String IO_METRIC_NAME = "hw.gpu.io";
	private static final String UTILIZATION_METRIC_NAME = "hw.gpu.utilization";
	private static final String MONITOR_TYPE = "GPU";
	private static final String STATUS_METRIC_DESCRIPTION = createStatusDescription(
		MONITOR_TYPE,
		STATE_ATTRIBUTE_KEY,
		OK_ATTRIBUTE_VALUE, DEGRADED_ATTRIBUTE_VALUE, FAILED_ATTRIBUTE_VALUE, PRESENT_ATTRIBUTE_VALUE, PREDICTED_FAILURE_ATTRIBUTE_VALUE
	);
	private static final String ERROR_METRIC_DESCRIPTION = createCustomDescriptionWithAttributes(
		"Number of errors encountered by the GPU since the start of the Hardware Sentry Agent",
		TYPE_ATTRIBUTE_KEY,
		ALL_ATTRIBUTE_VALUE, CORRECTED_ATTRIBUTE_VALUE
	);
	private static final String IO_METRIC_DESCRIPTION = createCustomDescriptionWithAttributes(
		"Number of bytes received or transmitted through the GPU",
		DIRECTION_ATTRIBUTE_KEY,
		TRANSMIT_ATTRIBUTE_VALUE, RECEIVE_ATTRIBUTE_VALUE
	);
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
					.name(ERROR_METRIC_NAME)
					.unit(ERRORS_UNIT)
					.identifyingAttribute(
						StaticIdentifyingAttribute
							.builder()
							.key(TYPE_ATTRIBUTE_KEY)
							.value(ALL_ATTRIBUTE_VALUE)
							.build()
					)
					.type(MetricType.COUNTER)
					.description(ERROR_METRIC_DESCRIPTION)
					.build()
			)
		);

		map.put(
			Gpu.CORRECTED_ERROR_COUNT.getName(),
			Collections.singletonList(
				MetricInfo
					.builder()
					.name(ERROR_METRIC_NAME)
					.unit(ERRORS_UNIT)
					.identifyingAttribute(
						StaticIdentifyingAttribute
							.builder()
							.key(TYPE_ATTRIBUTE_KEY)
							.value(CORRECTED_ATTRIBUTE_VALUE)
							.build()
					)
					.type(MetricType.COUNTER)
					.description(ERROR_METRIC_DESCRIPTION)
					.build()
			)
		);

		map.put(
			Gpu.RECEIVED_BYTES.getName(),
			Collections.singletonList(
				MetricInfo
					.builder()
					.name(IO_METRIC_NAME)
					.unit(BYTES_UNIT)
					.identifyingAttribute(
						StaticIdentifyingAttribute
							.builder()
							.key(DIRECTION_ATTRIBUTE_KEY)
							.value(RECEIVE_ATTRIBUTE_VALUE)
							.build()
					)
					.type(MetricType.COUNTER)
					.description(IO_METRIC_DESCRIPTION)
					.build()
			)
		);

		map.put(
			Gpu.TRANSMITTED_BYTES.getName(),
			Collections.singletonList(
				MetricInfo
					.builder()
					.name(IO_METRIC_NAME)
					.unit(BYTES_UNIT)
					.identifyingAttribute(
						StaticIdentifyingAttribute
							.builder()
							.key(DIRECTION_ATTRIBUTE_KEY)
							.value(TRANSMIT_ATTRIBUTE_VALUE)
							.build()
					)
					.type(MetricType.COUNTER)
					.description(IO_METRIC_DESCRIPTION)
					.build()
			)
		);

		map.put(
			Gpu.DECODER_USED_TIME.getName(),
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
			Gpu.ENCODER_USED_TIME.getName(),
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
			Gpu.USED_TIME.getName(),
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
