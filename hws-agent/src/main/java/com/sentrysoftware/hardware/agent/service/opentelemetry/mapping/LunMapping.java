package com.sentrysoftware.hardware.agent.service.opentelemetry.mapping;

import static com.sentrysoftware.hardware.agent.service.opentelemetry.mapping.MappingConstants.*;

import static com.sentrysoftware.matrix.common.helpers.HardwareConstants.EXPECTED_PATH_COUNT;
import static com.sentrysoftware.matrix.common.helpers.HardwareConstants.AVAILABLE_PATH_WARNING;

import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;

import com.sentrysoftware.hardware.agent.dto.metric.MetricInfo;
import com.sentrysoftware.hardware.agent.dto.metric.StaticIdentifyingAttribute;
import com.sentrysoftware.matrix.common.meta.monitor.IMetaMonitor;
import com.sentrysoftware.matrix.common.meta.monitor.Lun;

import lombok.AccessLevel;
import lombok.NoArgsConstructor;

@NoArgsConstructor(access = AccessLevel.PRIVATE)
public class LunMapping {

	private static final String LUN_STATUS_METRIC_NAME = "hw.lun.status";
	private static final String LUN_PATHS_METRIC_NAME = "hw.lun.path";
	private static final String LUN_NAME = "LUN";

	/**
	 * Build LUN metrics map
	 *
	 * @return  {@link Map} where the metrics are indexed by the matrix parameter name
	 */
	static Map<String, List<MetricInfo>> buildLunMetricsMapping() {
		final Map<String, List<MetricInfo>> map = new TreeMap<>(String.CASE_INSENSITIVE_ORDER);

		map.put(
			IMetaMonitor.STATUS.getName(),
			List.of(
				MetricInfo
					.builder()
					.name(LUN_STATUS_METRIC_NAME)
					.description(createStatusDescription(LUN_NAME, OK_ATTRIBUTE_VALUE))
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
					.name(LUN_STATUS_METRIC_NAME)
					.description(createStatusDescription(LUN_NAME, DEGRADED_ATTRIBUTE_VALUE))
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
					.name(LUN_STATUS_METRIC_NAME)
					.description(createStatusDescription(LUN_NAME, FAILED_ATTRIBUTE_VALUE))
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
					.name(LUN_STATUS_METRIC_NAME)
					.description(createPresentDescription(LUN_NAME))
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
			Lun.AVAILABLE_PATH_COUNT.getName(),
			Collections.singletonList(
				MetricInfo
					.builder()
					.name(LUN_PATHS_METRIC_NAME)
					.description("Number of distinct paths available to the remote volume.")
					.unit(PATHS_UNIT)
					.identifyingAttribute(
						StaticIdentifyingAttribute
							.builder()
							.key(TYPE_ATTRIBUTE_KEY)
							.value(AVAILABLE_ATTRIBUTE_VALUE)
							.build()
					)
					.build()
			)
		);

		return map;
	}

	/**
	 * Create LUN Metadata to metrics map
	 * 
	 * @return {@link Map} of {@link MetricInfo} instances indexed by the matrix parameter names
	 */
	static Map<String, List<MetricInfo>> lunMetadataToMetrics() {
		final Map<String, List<MetricInfo>> map = new TreeMap<>(String.CASE_INSENSITIVE_ORDER);

		map.put(
			EXPECTED_PATH_COUNT,
			Collections.singletonList(
				MetricInfo
					.builder()
					.name(LUN_PATHS_METRIC_NAME)
					.unit(PATHS_UNIT)
					.identifyingAttribute(
						StaticIdentifyingAttribute
							.builder()
							.key(TYPE_ATTRIBUTE_KEY)
							.value(EXPECTED_ATTRIBUTE_VALUE)
							.build()
					)
					.description("Number of paths that are expected to be available to the remote volume.")
					.build()
			)
		);

		map.put(
			AVAILABLE_PATH_WARNING,
			Collections.singletonList(
				MetricInfo
					.builder()
					.name("hw.lun.paths_warning")
					.unit(PATHS_UNIT)
					.identifyingAttribute(
						StaticIdentifyingAttribute
							.builder()
							.key(TYPE_ATTRIBUTE_KEY)
							.value(AVAILABLE_ATTRIBUTE_VALUE)
							.build()
					)
					.description("Number of available paths that will generate a warning when reached.")
					.unit(ERRORS_UNIT)
					.build()
			)
		);

		return map;
	}
}
