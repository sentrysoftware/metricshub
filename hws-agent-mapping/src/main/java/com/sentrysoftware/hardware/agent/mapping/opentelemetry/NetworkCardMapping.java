package com.sentrysoftware.hardware.agent.mapping.opentelemetry;

import static com.sentrysoftware.hardware.agent.mapping.opentelemetry.MappingConstants.*;
import static com.sentrysoftware.matrix.common.helpers.HardwareConstants.ERROR_PERCENT_ALARM_THRESHOLD;
import static com.sentrysoftware.matrix.common.helpers.HardwareConstants.ERROR_PERCENT_WARNING_THRESHOLD;

import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;

import com.sentrysoftware.hardware.agent.mapping.opentelemetry.dto.MetricInfo;
import com.sentrysoftware.hardware.agent.mapping.opentelemetry.dto.StaticIdentifyingAttribute;
import com.sentrysoftware.hardware.agent.mapping.opentelemetry.dto.MetricInfo.MetricType;
import com.sentrysoftware.matrix.common.meta.monitor.IMetaMonitor;
import com.sentrysoftware.matrix.common.meta.monitor.NetworkCard;

import lombok.AccessLevel;
import lombok.NoArgsConstructor;

@NoArgsConstructor(access = AccessLevel.PRIVATE)
public class NetworkCardMapping {

	public static final String HW_TYPE_ATTRIBUTE_VALUE = "network";
	private static final String ERRORS_METRIC_DESCRIPTION = createCustomDescriptionWithAttributes(
		"Number of errors encountered by the network interface since the start of the Hardware Sentry Agent",
		TYPE_ATTRIBUTE_KEY,
		ALL_ATTRIBUTE_VALUE, ZERO_BUFFER_CREDIT_ATTRIBUTE_VALUE
	);
	private static final String IO_METRIC_DESCRIPTION = createCustomDescriptionWithAttributes(
		"Total number of bytes received or transmitted through the network interface",
		DIRECTION_ATTRIBUTE_KEY,
		RECEIVE_ATTRIBUTE_VALUE, TRANSMIT_ATTRIBUTE_VALUE
	);
	private static final String PACKET_METRIC_DESCRIPTION = createCustomDescriptionWithAttributes(
		"Total number of packets received or transmitted through the network interface",
		DIRECTION_ATTRIBUTE_KEY,
		RECEIVE_ATTRIBUTE_VALUE, TRANSMIT_ATTRIBUTE_VALUE
	);
	private static final String ERROR_RATIO_METRIC_DESCRIPTION = createCustomDescriptionWithAttributes(
		"Network interface error ratio that will generate a warning or an alarm when reached",
		LIMIT_TYPE_ATTRIBUTE_KEY,
		CRITICAL_ATTRIBUTE_VALUE, DEGRADED_ATTRIBUTE_VALUE
	);

	/**
	 * 
	 * Build network card metrics map
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
			NetworkCard.BANDWIDTH_UTILIZATION.getName(),
			Collections.singletonList(
				MetricInfo
					.builder()
					.name("hw.network.bandwidth.utilization")
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
					.description("Whether the port is configured to operate in full-duplex mode.")
					.predicate(FULL_DUPLEX_MODE_PREDICATE)
					.type(MetricType.UP_DOWN_COUNTER)
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
					.description(ERRORS_METRIC_DESCRIPTION)
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
					.name("hw.network.bandwidth.limit")
					.factor(MEGABITS_TO_BYTES_FACTOR)
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
					.predicate(PLUGGED_LINK_STATUS_PREDICATE)
					.type(MetricType.UP_DOWN_COUNTER)
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
					.description(IO_METRIC_DESCRIPTION)
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
					.description(PACKET_METRIC_DESCRIPTION)
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
					.description(IO_METRIC_DESCRIPTION)
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
					.description(PACKET_METRIC_DESCRIPTION)
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
					.description(ERRORS_METRIC_DESCRIPTION)
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
	 * Create NetworkCard Metadata to metrics map
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
					.name("hw.network.error_ratio.limit")
					.factor(RATIO_FACTOR)
					.description(ERROR_RATIO_METRIC_DESCRIPTION)
					.unit(RATIO_UNIT)
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
			ERROR_PERCENT_ALARM_THRESHOLD,
			Collections.singletonList(
				MetricInfo
					.builder()
					.name("hw.network.error_ratio.limit")
					.factor(RATIO_FACTOR)
					.description(ERROR_RATIO_METRIC_DESCRIPTION)
					.unit(RATIO_UNIT)
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

		return map;
	}
}
