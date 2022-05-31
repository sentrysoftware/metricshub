package com.sentrysoftware.hardware.agent.service.opentelemetry.mapping;

import static com.sentrysoftware.hardware.agent.service.opentelemetry.mapping.MappingConstants.ALL_ATTRIBUTE_VALUE;
import static com.sentrysoftware.hardware.agent.service.opentelemetry.mapping.MappingConstants.BYTES_UNIT;
import static com.sentrysoftware.hardware.agent.service.opentelemetry.mapping.MappingConstants.DEGRADED_ATTRIBUTE_VALUE;
import static com.sentrysoftware.hardware.agent.service.opentelemetry.mapping.MappingConstants.DEGRADED_STATUS_PREDICATE;
import static com.sentrysoftware.hardware.agent.service.opentelemetry.mapping.MappingConstants.DIRECTION_ATTRIBUTE_KEY;
import static com.sentrysoftware.hardware.agent.service.opentelemetry.mapping.MappingConstants.DUPLEX_MODE_PREDICATE;
import static com.sentrysoftware.hardware.agent.service.opentelemetry.mapping.MappingConstants.ERRORS_UNIT;
import static com.sentrysoftware.hardware.agent.service.opentelemetry.mapping.MappingConstants.FAILED_ATTRIBUTE_VALUE;
import static com.sentrysoftware.hardware.agent.service.opentelemetry.mapping.MappingConstants.FAILED_STATUS_PREDICATE;
import static com.sentrysoftware.hardware.agent.service.opentelemetry.mapping.MappingConstants.JOULES_UNIT;
import static com.sentrysoftware.hardware.agent.service.opentelemetry.mapping.MappingConstants.LINK_SPEED_FACTOR;
import static com.sentrysoftware.hardware.agent.service.opentelemetry.mapping.MappingConstants.LINK_STATUS_PREDICATE;
import static com.sentrysoftware.hardware.agent.service.opentelemetry.mapping.MappingConstants.OK_ATTRIBUTE_VALUE;
import static com.sentrysoftware.hardware.agent.service.opentelemetry.mapping.MappingConstants.OK_STATUS_PREDICATE;
import static com.sentrysoftware.hardware.agent.service.opentelemetry.mapping.MappingConstants.PACKETS_UNIT;
import static com.sentrysoftware.hardware.agent.service.opentelemetry.mapping.MappingConstants.PRESENT_ATTRIBUTE_VALUE;
import static com.sentrysoftware.hardware.agent.service.opentelemetry.mapping.MappingConstants.PRESENT_PREDICATE;
import static com.sentrysoftware.hardware.agent.service.opentelemetry.mapping.MappingConstants.RATIO_FACTOR;
import static com.sentrysoftware.hardware.agent.service.opentelemetry.mapping.MappingConstants.RATIO_UNIT;
import static com.sentrysoftware.hardware.agent.service.opentelemetry.mapping.MappingConstants.RECEIVE_ATTRIBUTE_VALUE;
import static com.sentrysoftware.hardware.agent.service.opentelemetry.mapping.MappingConstants.STATE_ATTRIBUTE_KEY;
import static com.sentrysoftware.hardware.agent.service.opentelemetry.mapping.MappingConstants.TRANSMIT_ATTRIBUTE_VALUE;
import static com.sentrysoftware.hardware.agent.service.opentelemetry.mapping.MappingConstants.TYPE_ATTRIBUTE_KEY;
import static com.sentrysoftware.hardware.agent.service.opentelemetry.mapping.MappingConstants.WATTS_UNIT;
import static com.sentrysoftware.hardware.agent.service.opentelemetry.mapping.MappingConstants.ZERO_BUFFER_CREDIT_ATTRIBUTE_VALUE;
import static com.sentrysoftware.hardware.agent.service.opentelemetry.mapping.MappingConstants.createEnergyDescription;
import static com.sentrysoftware.hardware.agent.service.opentelemetry.mapping.MappingConstants.createPowerConsumptionDescription;
import static com.sentrysoftware.hardware.agent.service.opentelemetry.mapping.MappingConstants.createPresentDescription;
import static com.sentrysoftware.hardware.agent.service.opentelemetry.mapping.MappingConstants.createStatusDescription;
import static com.sentrysoftware.matrix.common.helpers.HardwareConstants.ERROR_PERCENT_ALARM_THRESHOLD;
import static com.sentrysoftware.matrix.common.helpers.HardwareConstants.ERROR_PERCENT_WARNING_THRESHOLD;

import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;

import com.sentrysoftware.hardware.agent.dto.metric.MetricInfo;
import com.sentrysoftware.hardware.agent.dto.metric.MetricInfo.MetricType;
import com.sentrysoftware.hardware.agent.dto.metric.StaticIdentifyingAttribute;
import com.sentrysoftware.matrix.common.meta.monitor.IMetaMonitor;
import com.sentrysoftware.matrix.common.meta.monitor.NetworkCard;

import lombok.AccessLevel;
import lombok.NoArgsConstructor;

@NoArgsConstructor(access = AccessLevel.PRIVATE)
public class NetworkCardMapping {

	private static final String NETWORK_CARD_NAME = "network card";
	private static final String NETWORK_CARD_STATUS_METRIC_NAME = "hw.network.status";
	/**
	 * Build physical disk metrics map
	 *
	 * @return  {@link Map} where the metrics are indexed by the matrix parameter name
	 */
	static Map<String, List<MetricInfo>> buildNetworkCardMetricsMapping() {
		final Map<String, List<MetricInfo>> map = new TreeMap<>(String.CASE_INSENSITIVE_ORDER);

		map.put(
			IMetaMonitor.STATUS.getName(),
			List.of(
				MetricInfo
					.builder()
					.name(NETWORK_CARD_STATUS_METRIC_NAME)
					.description(createStatusDescription(NETWORK_CARD_NAME, OK_ATTRIBUTE_VALUE))
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
					.name(NETWORK_CARD_STATUS_METRIC_NAME)
					.description(createStatusDescription(NETWORK_CARD_NAME, DEGRADED_ATTRIBUTE_VALUE))
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
					.name(NETWORK_CARD_STATUS_METRIC_NAME)
					.description(createStatusDescription(NETWORK_CARD_NAME, FAILED_ATTRIBUTE_VALUE))
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
					.name(NETWORK_CARD_STATUS_METRIC_NAME)
					.description(createPresentDescription(NETWORK_CARD_NAME))
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
			NetworkCard.BANDWIDTH_UTILIZATION.getName(),
			Collections.singletonList(
				MetricInfo
					.builder()
					.name("hw.network.bandwidth_utilization")
					.factor(RATIO_FACTOR)
					.description("Ratio of the available bandwidth utilization.")
					.unit(RATIO_UNIT)
					.build()
			)
		);

		map.put(
			NetworkCard.DUPLEX_MODE.getName(),
			Collections.singletonList(
				MetricInfo
					.builder()
					.name("hw.network.full_duplex")
					.description("Whether the port is configured to operate in half-duplex or full-duplex mode.")
					.predicate(DUPLEX_MODE_PREDICATE)
					.build()
			)
		);

		map.put(
			IMetaMonitor.ERROR_COUNT.getName(),
			Collections.singletonList(
				MetricInfo
					.builder()
					.name("hw.network.errors")
					.unit(ERRORS_UNIT)
					.type(MetricType.COUNTER)
					.description("Number of errors encountered by the network since the start of the Hardware Sentry Agent.")
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
			NetworkCard.LINK_SPEED.getName(),
			Collections.singletonList(
				MetricInfo
					.builder()
					.name("hw.network.bandwidth_limit")
					.factor(LINK_SPEED_FACTOR)
					.description("Speed that the network adapter and its remote counterpart currently use to communicate with each other.")
					.unit(BYTES_UNIT)
					.build()
			)
		);

		map.put(
			NetworkCard.LINK_STATUS.getName(),
			Collections.singletonList(
				MetricInfo
					.builder()
					.name("hw.network.up")
					.description("Whether the network interface is plugged into the network or not.")
					.predicate(LINK_STATUS_PREDICATE)
					.build()
			)
		);

		map.put(
			NetworkCard.RECEIVED_BYTES.getName(),
			Collections.singletonList(
				MetricInfo
					.builder()
					.name("hw.network.io")
					.unit(BYTES_UNIT)
					.type(MetricType.COUNTER)
					.description("Total number of bytes received through the network interface.")
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
			NetworkCard.RECEIVED_PACKETS.getName(),
			Collections.singletonList(
				MetricInfo
					.builder()
					.name("hw.network.packets")
					.unit(PACKETS_UNIT)
					.type(MetricType.COUNTER)
					.description("Total number of packets received through the network interface.")
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
			NetworkCard.TRANSMITTED_BYTES.getName(),
			Collections.singletonList(
				MetricInfo
					.builder()
					.name("hw.network.io")
					.unit(BYTES_UNIT)
					.type(MetricType.COUNTER)
					.description("Total number of bytes transmitted through the network interface.")
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
			NetworkCard.TRANSMITTED_PACKETS.getName(),
			Collections.singletonList(
				MetricInfo
					.builder()
					.name("hw.network.packets")
					.unit(PACKETS_UNIT)
					.type(MetricType.COUNTER)
					.description("Total number of packets transmitted through the network interface.")
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
			NetworkCard.ZERO_BUFFER_CREDIT_COUNT.getName(),
			Collections.singletonList(
				MetricInfo
					.builder()
					.name("hw.network.errors")
					.type(MetricType.COUNTER)
					.description("Total number of zero buffer credits that occurred.")
					.identifyingAttribute(
							StaticIdentifyingAttribute
								.builder()
								.key(TYPE_ATTRIBUTE_KEY)
								.value(ZERO_BUFFER_CREDIT_ATTRIBUTE_VALUE)
								.build()
						)
					.build()
			)
		);

		map.put(
			IMetaMonitor.ENERGY.getName(),
			Collections.singletonList(
				MetricInfo
					.builder()
					.name("hw.network.energy")
					.unit(JOULES_UNIT)
					.type(MetricType.COUNTER)
					.description(createEnergyDescription(NETWORK_CARD_NAME))
					.build()
			)
		);

		map.put(
			IMetaMonitor.POWER_CONSUMPTION.getName(),
			Collections.singletonList(
				MetricInfo
					.builder()
					.name("hw.network.power")
					.unit(WATTS_UNIT)
					.description(createPowerConsumptionDescription(NETWORK_CARD_NAME))
					.build()
			)
		);

		map.put(
			NetworkCard.ERROR_PERCENT.getName(),
			Collections.singletonList(
				MetricInfo
					.builder()
					.name("hw.network.error_ratio")
					.factor(RATIO_FACTOR)
					.description("Ratio of sent and received packets that were in error.")
					.unit(RATIO_UNIT)
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
	static Map<String, List<MetricInfo>> networkCardMetadataToMetrics() {
		final Map<String, List<MetricInfo>> map = new TreeMap<>(String.CASE_INSENSITIVE_ORDER);

		map.put(
			ERROR_PERCENT_WARNING_THRESHOLD,
			Collections.singletonList(
				MetricInfo
					.builder()
					.name("hw.network.error_ratio_warning")
					.factor(RATIO_FACTOR)
					.description("Network interface error ratio that will generate a warning when reached.")
					.unit(ERRORS_UNIT)
					.build()
			)
		);

		map.put(
			ERROR_PERCENT_ALARM_THRESHOLD,
			Collections.singletonList(
				MetricInfo
					.builder()
					.name("hw.network.error_ratio_alarm")
					.factor(RATIO_FACTOR)
					.description("Network interface error ratio that will generate an alarm when reached.")
					.unit(ERRORS_UNIT)
					.build()
			)
		);

		return map;
	}
}
