package com.sentrysoftware.hardware.agent.service.opentelemetry.mapping;

import static com.sentrysoftware.hardware.agent.service.opentelemetry.mapping.MappingConstants.*;

import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;

import com.sentrysoftware.hardware.agent.dto.metric.MetricInfo;
import com.sentrysoftware.hardware.agent.dto.metric.StaticIdentifyingAttribute;
import com.sentrysoftware.hardware.agent.dto.metric.MetricInfo.MetricType;
import com.sentrysoftware.matrix.common.meta.monitor.Enclosure;
import com.sentrysoftware.matrix.common.meta.monitor.IMetaMonitor;

import lombok.AccessLevel;
import lombok.NoArgsConstructor;

@NoArgsConstructor(access = AccessLevel.PRIVATE)
public class EnclosureMapping {

	private static final String ENCLOSURE_STATUS_METRIC_NAME = "hw.enclosure.status";

	/**
	 * Build enclosure metrics map
	 *
	 * @return {@link Map} where the metrics are indexed by the matrix parameter name
	 */
	static Map<String, List<MetricInfo>> buildEnclosureMetricsMapping() {
		final Map<String, List<MetricInfo>> map = new TreeMap<>(String.CASE_INSENSITIVE_ORDER);

		map.put(
			IMetaMonitor.STATUS.getName(),
			List.of(
				MetricInfo
					.builder()
					.name(ENCLOSURE_STATUS_METRIC_NAME)
					.description("Whether the enclosure status is ok or not.")
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
					.name(ENCLOSURE_STATUS_METRIC_NAME)
					.description("Whether the enclosure status is degraded or not.")
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
					.name(ENCLOSURE_STATUS_METRIC_NAME)
					.description("Whether the enclosure status is failed or not.")
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
					.name(ENCLOSURE_STATUS_METRIC_NAME)
					.description("Whether the enclosure is found or not.")
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
			Enclosure.INTRUSION_STATUS.getName(),
			Collections.singletonList(
				MetricInfo
					.builder()
					.name(ENCLOSURE_STATUS_METRIC_NAME)
					.description("Enclosure intrusion status. If the enclosure is open or not properly closed, it is set to 1.")
					.identifyingAttribute(
						StaticIdentifyingAttribute
							.builder()
							.key(STATE_ATTRIBUTE_KEY)
							.value(INTRUSION_ATTRIBUTE_VALUE)
							.build()
					)
					.predicate(INTRUSION_STATUS_PREDICATE)
					.build()
			)
		);

		map.put(
			IMetaMonitor.ENERGY.getName(),
			Collections.singletonList(
				MetricInfo
					.builder()
					.name("hw.enclosure.energy")
					.unit(JOULES_UNIT)
					.type(MetricType.COUNTER)
					.description("Energy consumed by the enclosure since the start of the Hardware Sentry Agent.")
					.build()
			)
		);

		map.put(
			IMetaMonitor.POWER_CONSUMPTION.getName(), 
			Collections.singletonList(
				MetricInfo
				.builder()
				.name("hw.enclosure.power")
				.unit(WATTS_UNIT)
				.type(MetricType.GAUGE)
				.description("Energy consumed by the enclosure.")
				.build()
			)
		);

		return map;
	}
}
