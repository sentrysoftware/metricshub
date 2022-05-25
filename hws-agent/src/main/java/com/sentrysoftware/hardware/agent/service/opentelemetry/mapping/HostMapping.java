package com.sentrysoftware.hardware.agent.service.opentelemetry.mapping;

import static com.sentrysoftware.hardware.agent.service.opentelemetry.mapping.MappingConstants.CELCIUS_UNIT;
import static com.sentrysoftware.hardware.agent.service.opentelemetry.mapping.MappingConstants.HTTP_ATTRIBUTE_VALUE;
import static com.sentrysoftware.hardware.agent.service.opentelemetry.mapping.MappingConstants.IPMI_ATTRIBUTE_VALUE;
import static com.sentrysoftware.hardware.agent.service.opentelemetry.mapping.MappingConstants.JOULES_UNIT;
import static com.sentrysoftware.hardware.agent.service.opentelemetry.mapping.MappingConstants.PRESENT_PREDICATE;
import static com.sentrysoftware.hardware.agent.service.opentelemetry.mapping.MappingConstants.PROTOCOL_ATTRIBUTE_KEY;
import static com.sentrysoftware.hardware.agent.service.opentelemetry.mapping.MappingConstants.SNMP_ATTRIBUTE_VALUE;
import static com.sentrysoftware.hardware.agent.service.opentelemetry.mapping.MappingConstants.SSH_ATTRIBUTE_VALUE;
import static com.sentrysoftware.hardware.agent.service.opentelemetry.mapping.MappingConstants.WATTS_UNIT;
import static com.sentrysoftware.hardware.agent.service.opentelemetry.mapping.MappingConstants.WBEM_ATTRIBUTE_VALUE;
import static com.sentrysoftware.hardware.agent.service.opentelemetry.mapping.MappingConstants.WMI_ATTRIBUTE_VALUE;

import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;

import com.sentrysoftware.hardware.agent.dto.metric.DynamicIdentifyingAttribute;
import com.sentrysoftware.hardware.agent.dto.metric.MetricInfo;
import com.sentrysoftware.hardware.agent.dto.metric.MetricInfo.MetricType;
import com.sentrysoftware.hardware.agent.dto.metric.StaticIdentifyingAttribute;
import com.sentrysoftware.matrix.common.helpers.HardwareConstants;
import com.sentrysoftware.matrix.common.meta.monitor.IMetaMonitor;
import com.sentrysoftware.matrix.common.meta.monitor.Target;

import lombok.AccessLevel;
import lombok.NoArgsConstructor;

@NoArgsConstructor(access = AccessLevel.PRIVATE)
public class HostMapping {

	public static final String HARDWARE_SENTRY_HOST_UP_METRIC_NAME = "hardware_sentry.host.up";

	/**
	 * Build host metrics map
	 *
	 * @return  {@link Map} where the metrics are indexed by the matrix parameter name
	 */
	static Map<String, List<MetricInfo>> buildHostMetricsMapping() {
		final Map<String, List<MetricInfo>> map = new TreeMap<>(String.CASE_INSENSITIVE_ORDER);

		map.put(
			Target.AMBIENT_TEMPERATURE.getName(),
			Collections.singletonList(
				 MetricInfo
					.builder()
					.name("hw.host.ambient_temperature")
					.unit(CELCIUS_UNIT)
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
					.unit(CELCIUS_UNIT)
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
					.description("Whether the host is configured or not.")
					.predicate(PRESENT_PREDICATE)
					.build(),
				MetricInfo
					.builder()
					.name("hardware_sentry.host.configured")
					.description("Whether the host is configured or not.")
					.predicate(PRESENT_PREDICATE)
					.build()
			)
		);

		map.put(
			Target.SNMP_UP.getName(),
			Collections.singletonList(
				MetricInfo
					.builder()
					.name(HARDWARE_SENTRY_HOST_UP_METRIC_NAME)
					.description("Whether the SNMP protocol is up or not.")
					.identifyingAttribute(
						StaticIdentifyingAttribute
							.builder()
							.key(PROTOCOL_ATTRIBUTE_KEY)
							.value(SNMP_ATTRIBUTE_VALUE)
							.build()
					)
					.build()
			)
		);

		map.put(
			Target.WMI_UP.getName(),
			Collections.singletonList(
				MetricInfo
					.builder()
					.name(HARDWARE_SENTRY_HOST_UP_METRIC_NAME)
					.description("Whether the WMI protocol is up or not.")
					.identifyingAttribute(
						StaticIdentifyingAttribute
							.builder()
							.key(PROTOCOL_ATTRIBUTE_KEY)
							.value(WMI_ATTRIBUTE_VALUE)
							.build()
					)
					.build()
			)
		);

		map.put(
			Target.WBEM_UP.getName(),
			Collections.singletonList(
				MetricInfo
					.builder()
					.name(HARDWARE_SENTRY_HOST_UP_METRIC_NAME)
					.description("Whether the WBEM protocol is up or not.")
					.identifyingAttribute(
						StaticIdentifyingAttribute
							.builder()
							.key(PROTOCOL_ATTRIBUTE_KEY)
							.value(WBEM_ATTRIBUTE_VALUE)
							.build()
					)
					.build()
				)
		);

		map.put(
			Target.SSH_UP.getName(),
			Collections.singletonList(
				MetricInfo
					.builder()
					.name(HARDWARE_SENTRY_HOST_UP_METRIC_NAME)
					.description("Whether the SSH protocol is up or not.")
					.identifyingAttribute(
						StaticIdentifyingAttribute
							.builder()
							.key(PROTOCOL_ATTRIBUTE_KEY)
							.value(SSH_ATTRIBUTE_VALUE)
							.build()
					)
					.build()
				)
		);

		map.put(
			Target.HTTP_UP.getName(),
			Collections.singletonList(
				MetricInfo
					.builder()
					.name(HARDWARE_SENTRY_HOST_UP_METRIC_NAME)
					.description("Whether the HTTP protocol is up or not.")
					.identifyingAttribute(
						StaticIdentifyingAttribute
							.builder()
							.key(PROTOCOL_ATTRIBUTE_KEY)
							.value(HTTP_ATTRIBUTE_VALUE)
							.build()
					)
					.build()
			)
		);

		map.put(
			Target.IPMI_UP.getName(),
			Collections.singletonList(
				MetricInfo
					.builder()
					.name(HARDWARE_SENTRY_HOST_UP_METRIC_NAME)
					.description("Whether the IPMI protocol is up or not.")
					.identifyingAttribute(
						StaticIdentifyingAttribute
							.builder()
							.key(PROTOCOL_ATTRIBUTE_KEY)
							.value(IPMI_ATTRIBUTE_VALUE)
							.build()
					)
					.build()
			)
		);

		return map;
	}
}
