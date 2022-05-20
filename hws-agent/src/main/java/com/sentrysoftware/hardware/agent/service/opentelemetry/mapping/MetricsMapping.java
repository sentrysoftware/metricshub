package com.sentrysoftware.hardware.agent.service.opentelemetry.mapping;

import static com.sentrysoftware.hardware.agent.service.opentelemetry.mapping.MappingConstants.DEFAULT_ATTRIBUTE_NAMES;
import static com.sentrysoftware.hardware.agent.service.opentelemetry.mapping.MappingConstants.VM_HOST_NAME;
import static com.sentrysoftware.matrix.common.helpers.HardwareConstants.HOSTNAME;

import java.util.Collections;
import java.util.EnumMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.function.Function;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import com.sentrysoftware.hardware.agent.dto.metric.MetricInfo;
import com.sentrysoftware.hardware.agent.dto.metric.MetricInfo.MetricType;
import com.sentrysoftware.hardware.agent.service.ServiceHelper;
import com.sentrysoftware.matrix.connector.model.monitor.MonitorType;

import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;

@NoArgsConstructor(access = AccessLevel.PRIVATE)
public class MetricsMapping {

	static final Map<MonitorType, Map<String, String>> monitorTypeToOverriddenAttributeMap;

	static final Map<MonitorType, Map<String, String>> monitorTypeToAttributeMap;

	@Getter
	static final Map<MonitorType, Map<String, List<MetricInfo>>> matrixParamToMetricMap;
	@Getter
	static final Map<MonitorType, Map<String, List<MetricInfo>>> matrixMetadataToMetricMap;

	public static final MetricInfo AGENT_METRIC_INFO = MetricInfo
			.builder()
			.name("hardware_sentry.agent.info")
			.description("Agent information.")
			.type(MetricType.GAUGE)
			.build();

	static {

		final Map<MonitorType, Map<String, String>> overriddenAttributeNames = new EnumMap<>(MonitorType.class);

		overriddenAttributeNames.put(MonitorType.VM, Map.of(HOSTNAME, VM_HOST_NAME));

		monitorTypeToOverriddenAttributeMap = Collections.unmodifiableMap(overriddenAttributeNames);

		final Map<MonitorType, Map<String, List<MetricInfo>>> matrixParamToMetric = new EnumMap<>(MonitorType.class);

		matrixParamToMetric.put(MonitorType.CONNECTOR, ConnectorMapping.buildConnectorMetricsMapping());
		matrixParamToMetric.put(MonitorType.TARGET, HostMapping.buildHostMetricsMapping());
		matrixParamToMetric.put(MonitorType.ENCLOSURE, EnclosureMapping.buildEnclosureMetricsMapping());

		matrixParamToMetricMap = Collections.unmodifiableMap(matrixParamToMetric);

		final Map<MonitorType, Map<String, List<MetricInfo>>> metadataToMetric = new EnumMap<>(MonitorType.class);

		matrixMetadataToMetricMap = Collections.unmodifiableMap(metadataToMetric);
	
		final Map<MonitorType, Map<String, String>> attributesMap = new EnumMap<>(MonitorType.class);

		attributesMap.put(MonitorType.BATTERY, concatDefaultAttributesWithMetadata(MonitorType.BATTERY));
		attributesMap.put(MonitorType.BLADE, concatDefaultAttributesWithMetadata(MonitorType.BLADE));
		attributesMap.put(MonitorType.CONNECTOR, concatDefaultAttributesWithMetadata(MonitorType.CONNECTOR));
		attributesMap.put(MonitorType.CPU_CORE, concatDefaultAttributesWithMetadata(MonitorType.CPU_CORE));
		attributesMap.put(MonitorType.CPU, concatDefaultAttributesWithMetadata(MonitorType.CPU));
		attributesMap.put(MonitorType.DISK_CONTROLLER, concatDefaultAttributesWithMetadata(MonitorType.DISK_CONTROLLER));
		attributesMap.put(MonitorType.ENCLOSURE, concatDefaultAttributesWithMetadata(MonitorType.ENCLOSURE));
		attributesMap.put(MonitorType.FAN, concatDefaultAttributesWithMetadata(MonitorType.FAN));
		attributesMap.put(MonitorType.GPU, concatDefaultAttributesWithMetadata(MonitorType.GPU));
		attributesMap.put(MonitorType.LED, concatDefaultAttributesWithMetadata(MonitorType.LED));
		attributesMap.put(MonitorType.LOGICAL_DISK, concatDefaultAttributesWithMetadata(MonitorType.LOGICAL_DISK));
		attributesMap.put(MonitorType.LUN, concatDefaultAttributesWithMetadata(MonitorType.LUN));
		attributesMap.put(MonitorType.TARGET, concatDefaultAttributesWithMetadata(MonitorType.TARGET));
		attributesMap.put(MonitorType.MEMORY, concatDefaultAttributesWithMetadata(MonitorType.MEMORY));
		attributesMap.put(MonitorType.NETWORK_CARD, concatDefaultAttributesWithMetadata(MonitorType.NETWORK_CARD));
		attributesMap.put(MonitorType.OTHER_DEVICE, concatDefaultAttributesWithMetadata(MonitorType.OTHER_DEVICE));
		attributesMap.put(MonitorType.PHYSICAL_DISK, concatDefaultAttributesWithMetadata(MonitorType.PHYSICAL_DISK));
		attributesMap.put(MonitorType.POWER_SUPPLY, concatDefaultAttributesWithMetadata(MonitorType.POWER_SUPPLY));
		attributesMap.put(MonitorType.ROBOTICS, concatDefaultAttributesWithMetadata(MonitorType.ROBOTICS));
		attributesMap.put(MonitorType.TAPE_DRIVE, concatDefaultAttributesWithMetadata(MonitorType.TAPE_DRIVE));
		attributesMap.put(MonitorType.TEMPERATURE, concatDefaultAttributesWithMetadata(MonitorType.TEMPERATURE));
		attributesMap.put(MonitorType.VOLTAGE, concatDefaultAttributesWithMetadata(MonitorType.VOLTAGE));
		attributesMap.put(MonitorType.VM, concatDefaultAttributesWithMetadata(MonitorType.VM));

		monitorTypeToAttributeMap = Collections.unmodifiableMap(attributesMap);
	}

	/**
	 * Get the corresponding MetricInfo list which gives the correct syntax to report from a matrix parameter.
	 * The value contains the metric  unit, conversion factor, predicate and its identifying attribute.
	 *
	 * @param monitorType         The type of monitor defined by matrix
	 * @param matrixParameterName The name of the matrix predefined parameter
	 * @return {@link Optional} value
	 */
	public static Optional<List<MetricInfo>> getMetricInfoList(final MonitorType monitorType, final String matrixParameterName) {
		final Map<String, List<MetricInfo>> parametersMap = matrixParamToMetricMap.get(monitorType);
		return parametersMap == null ? Optional.empty() : Optional.ofNullable(parametersMap.get(matrixParameterName));
	}

	/**
	 * Get the corresponding MetricInfo list which gives the correct syntax to report from a matrix metadata.
	 * The value contains the metric  unit, conversion factor, predicate and its identifying attribute.
	 *
	 * @param monitorType     The type of monitor defined by matrix
	 * @param matrixMetadataKey  The name of the matrix predefined metadata
	 * @return {@link Optional} value
	 */
	public static Optional<List<MetricInfo>> getMetadataAsMetricInfoList(final MonitorType monitorType, final String matrixMetadataKey) {
		final Map<String, List<MetricInfo>> parametersMap = matrixMetadataToMetricMap.get(monitorType);
		return (parametersMap == null || matrixMetadataKey == null) ? Optional.empty() : Optional.ofNullable(parametersMap.get(matrixMetadataKey));
	}

	/**
	 * Concatenate the predefined labels with the specific monitor metadata
	 *
	 * @param monitorType The monitor type we want to get its metadata
	 * 
	 * @return Map of attribute key to matrix metadata name
	 */
	private static Map<String, String> concatDefaultAttributesWithMetadata(final MonitorType monitorType) {

		return Stream
			.concat(DEFAULT_ATTRIBUTE_NAMES.stream(), monitorType.getMetaMonitor().getMetadata().stream())
			.filter(matrixMetadata -> !isMetadataMappedAsMetric(monitorType, matrixMetadata))
			.sorted()
			.collect(Collectors.toMap(
						metadataKey -> monitorTypeToOverriddenAttributeMap
											.getOrDefault(monitorType, Collections.emptyMap())
											.getOrDefault(metadataKey, ServiceHelper.camelCaseToSnakeCase(metadataKey)),
						Function.identity(),
						(k1, k2) -> k2
					)
			);
	}

	/**
	 * Checks if the given matrix metadata is mapped as metric
	 * 
	 * @param monitorType        The type of the monitor defined by matrix engine
	 * @param matrixMetadataName The name of the metadata (key)
	 * @return <code>true</code> if the metadata is mapped as metric otherwise <code>false</code>
	 */
	private static boolean isMetadataMappedAsMetric(final MonitorType monitorType, final String matrixMetadataName) {
		final Map<String, List<MetricInfo>> metadataToMetricMap = matrixMetadataToMetricMap.get(monitorType);

		return metadataToMetricMap != null && metadataToMetricMap.containsKey(matrixMetadataName);
	}

	/**
	 * Get the predefined attributes for the given monitor type
	 *
	 * @param monitorType The type of monitor
	 * @return Map of attribute key to matrix metadata name
	 */
	public static Map<String, String> getAttributesMap(final MonitorType monitorType) {
		return monitorTypeToAttributeMap.get(monitorType);
	}

}
