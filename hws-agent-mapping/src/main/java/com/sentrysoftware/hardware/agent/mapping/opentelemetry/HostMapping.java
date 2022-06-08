package com.sentrysoftware.hardware.agent.mapping.opentelemetry;

import static com.sentrysoftware.hardware.agent.mapping.opentelemetry.MappingConstants.*;

import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;

import com.sentrysoftware.hardware.agent.mapping.opentelemetry.dto.DynamicIdentifyingAttribute;
import com.sentrysoftware.hardware.agent.mapping.opentelemetry.dto.MetricInfo;
import com.sentrysoftware.hardware.agent.mapping.opentelemetry.dto.StaticIdentifyingAttribute;
import com.sentrysoftware.hardware.agent.mapping.opentelemetry.dto.MetricInfo.MetricType;
import com.sentrysoftware.matrix.common.helpers.HardwareConstants;
import com.sentrysoftware.matrix.common.meta.monitor.IMetaMonitor;
import com.sentrysoftware.matrix.common.meta.monitor.Host;

import lombok.AccessLevel;
import lombok.NoArgsConstructor;

@NoArgsConstructor(access = AccessLevel.PRIVATE)
public class HostMapping {

	private static final String CONFIGURED_METRIC_DESCRIPTION = "Whether the host is configured or not.";
	public static final String UP_METRIC_NAME = "hardware_sentry.host.up";
	private static final String UP_METRIC_DESCRIPTION = createCustomDescriptionWithAttributes(
		"Whether the configured protocol is up or not",
		PROTOCOL_ATTRIBUTE_KEY,
		SNMP_ATTRIBUTE_VALUE, HTTP_ATTRIBUTE_VALUE, WBEM_ATTRIBUTE_VALUE, SSH_ATTRIBUTE_VALUE, IPMI_ATTRIBUTE_VALUE, WMI_ATTRIBUTE_VALUE
	);

	/**
	 * Build host metrics map
	 *
	 * @return  {@link Map} where the metrics are indexed by the matrix parameter name
	 */
	static Map<String, List<MetricInfo>> buildHostMetricsMapping() {
		final Map<String, List<MetricInfo>> map = new TreeMap<>(String.CASE_INSENSITIVE_ORDER);

		map.put(
			Host.AMBIENT_TEMPERATURE.getName(),
			Collections.singletonList(
				 MetricInfo
					.builder()
					.name("hw.host.ambient_temperature")
					.unit(CELSIUS_UNIT)
					.description("Host's current ambient temperature in degrees Celsius (°C).")
					.build()
			)
		);

		map.put(
			IMetaMonitor.ENERGY.getName(),
			Collections.singletonList(
				MetricInfo
					.builder()
					.name("hw.host.energy")
					.unit(JOULES_UNIT)
					.type(MetricType.COUNTER)
					.description("Energy consumed by the components since the start of the Hardware Sentry agent.")
					.identifyingAttribute(
						DynamicIdentifyingAttribute
							.builder()
							.key("quality")
							.value(HardwareConstants.POWER_METER)
							.build()
					)
					.build()
			)
		);

		map.put(
			IMetaMonitor.HEATING_MARGIN.getName(),
			Collections.singletonList(
				MetricInfo
					.builder()
					.name("hw.host.heating_margin")
					.unit(CELSIUS_UNIT)
					.description("Number of degrees Celsius (°C) remaining before the temperature reaches the closest warning threshold.")
					.build()
			)
		);

		map.put(
			IMetaMonitor.POWER_CONSUMPTION.getName(), 
			Collections.singletonList(
				MetricInfo
					.builder()
					.name("hw.host.power")
					.unit(WATTS_UNIT)
					.description("Energy consumed by all the components discovered for the monitored host.")
					.identifyingAttribute(
						DynamicIdentifyingAttribute
							.builder()
							.key("quality")
							.value(HardwareConstants.POWER_METER)
							.build()
					)
					.build()
			)
		);

		map.put(
			IMetaMonitor.PRESENT.getName(),
			List.of(
				MetricInfo
					.builder()
					.name("hw.host.configured")
					.description(CONFIGURED_METRIC_DESCRIPTION)
					.predicate(PRESENT_PREDICATE)
					.type(MetricType.UP_DOWN_COUNTER)
					.build(),
				MetricInfo
					.builder()
					.name("hardware_sentry.host.configured")
					.description(CONFIGURED_METRIC_DESCRIPTION)
					.predicate(PRESENT_PREDICATE)
					.type(MetricType.UP_DOWN_COUNTER)
					.build()
			)
		);

		map.put(
			Host.SNMP_UP.getName(),
			Collections.singletonList(
				MetricInfo
					.builder()
					.name(UP_METRIC_NAME)
					.description(UP_METRIC_DESCRIPTION)
					.identifyingAttribute(
						StaticIdentifyingAttribute
							.builder()
							.key(PROTOCOL_ATTRIBUTE_KEY)
							.value(SNMP_ATTRIBUTE_VALUE)
							.build()
					)
					.predicate(UP_PREDICATE)
					.type(MetricType.UP_DOWN_COUNTER)
					.build()
			)
		);

		map.put(
			Host.WMI_UP.getName(),
			Collections.singletonList(
				MetricInfo
					.builder()
					.name(UP_METRIC_NAME)
					.description(UP_METRIC_DESCRIPTION)
					.identifyingAttribute(
						StaticIdentifyingAttribute
							.builder()
							.key(PROTOCOL_ATTRIBUTE_KEY)
							.value(WMI_ATTRIBUTE_VALUE)
							.build()
					)
					.predicate(UP_PREDICATE)
					.type(MetricType.UP_DOWN_COUNTER)
					.build()
			)
		);

		map.put(
			Host.WBEM_UP.getName(),
			Collections.singletonList(
				MetricInfo
					.builder()
					.name(UP_METRIC_NAME)
					.description(UP_METRIC_DESCRIPTION)
					.identifyingAttribute(
						StaticIdentifyingAttribute
							.builder()
							.key(PROTOCOL_ATTRIBUTE_KEY)
							.value(WBEM_ATTRIBUTE_VALUE)
							.build()
					)
					.predicate(UP_PREDICATE)
					.type(MetricType.UP_DOWN_COUNTER)
					.build()
				)
		);

		map.put(
			Host.SSH_UP.getName(),
			Collections.singletonList(
				MetricInfo
					.builder()
					.name(UP_METRIC_NAME)
					.description(UP_METRIC_DESCRIPTION)
					.identifyingAttribute(
						StaticIdentifyingAttribute
							.builder()
							.key(PROTOCOL_ATTRIBUTE_KEY)
							.value(SSH_ATTRIBUTE_VALUE)
							.build()
					)
					.predicate(UP_PREDICATE)
					.type(MetricType.UP_DOWN_COUNTER)
					.build()
				)
		);

		map.put(
			Host.HTTP_UP.getName(),
			Collections.singletonList(
				MetricInfo
					.builder()
					.name(UP_METRIC_NAME)
					.description(UP_METRIC_DESCRIPTION)
					.identifyingAttribute(
						StaticIdentifyingAttribute
							.builder()
							.key(PROTOCOL_ATTRIBUTE_KEY)
							.value(HTTP_ATTRIBUTE_VALUE)
							.build()
					)
					.predicate(UP_PREDICATE)
					.type(MetricType.UP_DOWN_COUNTER)
					.build()
			)
		);

		map.put(
			Host.IPMI_UP.getName(),
			Collections.singletonList(
				MetricInfo
					.builder()
					.name(UP_METRIC_NAME)
					.description(UP_METRIC_DESCRIPTION)
					.identifyingAttribute(
						StaticIdentifyingAttribute
							.builder()
							.key(PROTOCOL_ATTRIBUTE_KEY)
							.value(IPMI_ATTRIBUTE_VALUE)
							.build()
					)
					.predicate(UP_PREDICATE)
					.type(MetricType.UP_DOWN_COUNTER)
					.build()
			)
		);

		return map;
	}
}
