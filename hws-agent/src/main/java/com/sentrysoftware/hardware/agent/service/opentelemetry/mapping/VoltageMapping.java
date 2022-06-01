package com.sentrysoftware.hardware.agent.service.opentelemetry.mapping;

import static com.sentrysoftware.hardware.agent.service.opentelemetry.mapping.MappingConstants.*;
import static com.sentrysoftware.matrix.common.helpers.HardwareConstants.UPPER_THRESHOLD;
import static com.sentrysoftware.matrix.common.helpers.HardwareConstants.LOWER_THRESHOLD;

import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;

import com.sentrysoftware.hardware.agent.dto.metric.MetricInfo;
import com.sentrysoftware.hardware.agent.dto.metric.StaticIdentifyingAttribute;
import com.sentrysoftware.matrix.common.meta.monitor.IMetaMonitor;
import com.sentrysoftware.matrix.common.meta.monitor.Voltage;

import lombok.AccessLevel;
import lombok.NoArgsConstructor;

@NoArgsConstructor(access = AccessLevel.PRIVATE)
public class VoltageMapping {

	private static final String MONITOR_TYPE = "voltage";
	private static final String VOLTAGE_STATUS_METRIC_NAME = "hw.voltage.status";

	/**
	 * Build voltage metrics map
	 *
	 * @return  {@link Map} where the metrics are indexed by the matrix parameter name
	 */
	static Map<String, List<MetricInfo>> buildVoltageMetricsMapping() {
		final Map<String, List<MetricInfo>> map = new TreeMap<>(String.CASE_INSENSITIVE_ORDER);

		map.put(
			IMetaMonitor.STATUS.getName(),
			List.of(
				MetricInfo
					.builder()
					.name(VOLTAGE_STATUS_METRIC_NAME)
					.description(MappingConstants.createStatusDescription(MONITOR_TYPE, OK_ATTRIBUTE_VALUE))
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
					.name(VOLTAGE_STATUS_METRIC_NAME)
					.description(MappingConstants.createStatusDescription(MONITOR_TYPE, DEGRADED_ATTRIBUTE_VALUE))
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
					.name(VOLTAGE_STATUS_METRIC_NAME)
					.description(MappingConstants.createStatusDescription(MONITOR_TYPE, FAILED_ATTRIBUTE_VALUE))
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
					.name(VOLTAGE_STATUS_METRIC_NAME)
					.description(MappingConstants.createPresentDescription(MONITOR_TYPE))
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
			Voltage._VOLTAGE.getName(),
			Collections.singletonList(
				MetricInfo
					.builder()
					.name("hw.voltage.voltage")
					.factor(MILLIVOLTS_TO_VOLTS_FACTOR)
					.unit(VOLTS_UNIT)
					.description("Voltage output.")
					.build()
			)
		);

		return map;
	}

	/**
	 * Create voltage Metadata to metrics map
	 * 
	 * @return {@link Map} of {@link MetricInfo} instances indexed by the matrix parameter names
	 */
	static Map<String, List<MetricInfo>> voltageMetadataToMetrics() {
		final Map<String, List<MetricInfo>> map = new TreeMap<>(String.CASE_INSENSITIVE_ORDER);

		map.put(
			UPPER_THRESHOLD,
			Collections.singletonList(
				MetricInfo
					.builder()
					.name("hw.voltage.voltage.limit")
					.factor(MILLIVOLTS_TO_VOLTS_FACTOR)
					.unit(VOLTS_UNIT)
					.description("Upper threshold of the voltage.")
					.identifyingAttribute(
						StaticIdentifyingAttribute
							.builder()
							.key(LIMIT_TYPE_ATTRIBUTE_KEY)
							.value(HIGH_CRITICAL_ATTRIBUTE_VALUE)
							.build()
					)
					.build()
			)
		);

		map.put(
			LOWER_THRESHOLD,
			Collections.singletonList(
				MetricInfo
					.builder()
					.name("hw.voltage.voltage.limit")
					.description("Lower threshold of the voltage.")
					.factor(MILLIVOLTS_TO_VOLTS_FACTOR)
					.unit(VOLTS_UNIT)
					.identifyingAttribute(
						StaticIdentifyingAttribute
							.builder()
							.key(LIMIT_TYPE_ATTRIBUTE_KEY)
							.value(LOW_CRITICAL_ATTRIBUTE_VALUE)
							.build()
					)
					.build()
			)
		);

		return map;
	}
}
