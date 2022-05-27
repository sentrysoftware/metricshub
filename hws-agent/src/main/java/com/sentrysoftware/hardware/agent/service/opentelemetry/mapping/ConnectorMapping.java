package com.sentrysoftware.hardware.agent.service.opentelemetry.mapping;

import static com.sentrysoftware.hardware.agent.service.opentelemetry.mapping.MappingConstants.*;

import java.util.List;
import java.util.Map;
import java.util.TreeMap;

import com.sentrysoftware.hardware.agent.dto.metric.MetricInfo;
import com.sentrysoftware.hardware.agent.dto.metric.StaticIdentifyingAttribute;
import com.sentrysoftware.matrix.common.meta.monitor.IMetaMonitor;

import lombok.AccessLevel;
import lombok.NoArgsConstructor;

@NoArgsConstructor(access = AccessLevel.PRIVATE)
public class ConnectorMapping {

	private static final String CONNECTOR_TYPE = "connector";
	private static final String HARDWARE_SENTRY_CONNECTOR_STATUS_METRIC_NAME = "hardware_sentry.connector.status";

	/**
	 * Build connector metrics map
	 *
	 * @return {@link Map} where the metrics are indexed by the matrix parameter name
	 */
	static Map<String, List<MetricInfo>> buildConnectorMetricsMapping() {
		final Map<String, List<MetricInfo>> map = new TreeMap<>(String.CASE_INSENSITIVE_ORDER);

		map.put(
			IMetaMonitor.STATUS.getName(),
			List.of(
					MetricInfo
						.builder()
						.name(HARDWARE_SENTRY_CONNECTOR_STATUS_METRIC_NAME)
						.description(createStatusDescription(CONNECTOR_TYPE, OK_ATTRIBUTE_VALUE))
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
						.name(HARDWARE_SENTRY_CONNECTOR_STATUS_METRIC_NAME)
						.description(createStatusDescription(CONNECTOR_TYPE, DEGRADED_ATTRIBUTE_VALUE))
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
						.name(HARDWARE_SENTRY_CONNECTOR_STATUS_METRIC_NAME)
						.description(createStatusDescription(CONNECTOR_TYPE, FAILED_ATTRIBUTE_VALUE))
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

		return map;
	}
}
