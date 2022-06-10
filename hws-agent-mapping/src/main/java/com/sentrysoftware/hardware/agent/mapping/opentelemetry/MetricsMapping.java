package com.sentrysoftware.hardware.agent.mapping.opentelemetry;

import static com.sentrysoftware.hardware.agent.mapping.opentelemetry.MappingConstants.*;
import static com.sentrysoftware.matrix.common.helpers.HardwareConstants.*;

import java.util.Collections;
import java.util.EnumMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.function.Function;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import com.sentrysoftware.hardware.agent.mapping.MappingHelper;
import com.sentrysoftware.hardware.agent.mapping.opentelemetry.dto.MetricInfo;
import com.sentrysoftware.hardware.agent.mapping.opentelemetry.dto.MetricInfo.MetricType;
import com.sentrysoftware.matrix.connector.model.monitor.MonitorType;

import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;

@NoArgsConstructor(access = AccessLevel.PRIVATE)
public class MetricsMapping {

	static final Map<MonitorType, Map<String, String>> monitorTypeToOverriddenAttributeMap;

	@Getter
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

		overriddenAttributeNames.put(MonitorType.CONNECTOR, ConnectorMapping.buildOverriddenAttributeNames());
		overriddenAttributeNames.put(MonitorType.VM, VmMapping.buildOverriddenAttributeNames());
		overriddenAttributeNames.put(MonitorType.ENCLOSURE, buildDefaultOverriddenAttributeNames());
		overriddenAttributeNames.put(MonitorType.BATTERY, buildDefaultOverriddenAttributeNames());
		overriddenAttributeNames.put(MonitorType.MEMORY, buildDefaultOverriddenAttributeNames());
		overriddenAttributeNames.put(MonitorType.OTHER_DEVICE, buildDefaultOverriddenAttributeNames());
		overriddenAttributeNames.put(MonitorType.PHYSICAL_DISK, buildDefaultOverriddenAttributeNames());
		overriddenAttributeNames.put(MonitorType.POWER_SUPPLY, buildDefaultOverriddenAttributeNames());
		overriddenAttributeNames.put(MonitorType.ROBOTICS, buildDefaultOverriddenAttributeNames());
		overriddenAttributeNames.put(MonitorType.TAPE_DRIVE, buildDefaultOverriddenAttributeNames());
		overriddenAttributeNames.put(MonitorType.GPU, buildDefaultOverriddenAttributeNames());
		overriddenAttributeNames.put(MonitorType.CPU, buildDefaultOverriddenAttributeNames());
		overriddenAttributeNames.put(MonitorType.VOLTAGE, VoltageMapping.buildOverriddenAttributeNames());
		overriddenAttributeNames.put(MonitorType.TEMPERATURE, TemperatureMapping.buildOverriddenAttributeNames());
		overriddenAttributeNames.put(MonitorType.CPU_CORE, buildDefaultOverriddenAttributeNames());
		overriddenAttributeNames.put(MonitorType.DISK_CONTROLLER, buildDefaultOverriddenAttributeNames());
		overriddenAttributeNames.put(MonitorType.FAN, FanMapping.buildOverriddenAttributeNames());
		overriddenAttributeNames.put(MonitorType.LUN, buildDefaultOverriddenAttributeNames());
		overriddenAttributeNames.put(MonitorType.LED, buildDefaultOverriddenAttributeNames());
		overriddenAttributeNames.put(MonitorType.LOGICAL_DISK, buildDefaultOverriddenAttributeNames());
		overriddenAttributeNames.put(MonitorType.BLADE, buildDefaultOverriddenAttributeNames());
		overriddenAttributeNames.put(MonitorType.NETWORK_CARD, buildDefaultOverriddenAttributeNames());

		monitorTypeToOverriddenAttributeMap = Collections.unmodifiableMap(overriddenAttributeNames);

		final Map<MonitorType, Map<String, List<MetricInfo>>> matrixParamToMetric = new EnumMap<>(MonitorType.class);

		matrixParamToMetric.put(MonitorType.CONNECTOR, ConnectorMapping.buildConnectorMetricsMapping());
		matrixParamToMetric.put(MonitorType.HOST, HostMapping.buildHostMetricsMapping());
		matrixParamToMetric.put(MonitorType.ENCLOSURE, EnclosureMapping.buildEnclosureMetricsMapping());
		matrixParamToMetric.put(MonitorType.BATTERY, BatteryMapping.buildBatteryMetricsMapping());
		matrixParamToMetric.put(MonitorType.MEMORY, MemoryMapping.buildMemoryMetricsMapping());
		matrixParamToMetric.put(MonitorType.OTHER_DEVICE, OtherDeviceMapping.buildOtherDeviceMetricsMapping());
		matrixParamToMetric.put(MonitorType.PHYSICAL_DISK, PhysicalDiskMapping.buildPhysicalDiskMetricsMapping());
		matrixParamToMetric.put(MonitorType.POWER_SUPPLY, PowerSupplyMapping.buildPowerSupplyMetricsMapping());
		matrixParamToMetric.put(MonitorType.ROBOTICS, RoboticsMapping.buildRoboticsMetricsMapping());
		matrixParamToMetric.put(MonitorType.TAPE_DRIVE, TapeDriveMapping.buildTapeDriveMetricsMapping());
		matrixParamToMetric.put(MonitorType.GPU, GpuMapping.buildGpuMetricsMapping());
		matrixParamToMetric.put(MonitorType.CPU, CpuMapping.buildCpuMetricsMapping());
		matrixParamToMetric.put(MonitorType.VOLTAGE, VoltageMapping.buildVoltageMetricsMapping());
		matrixParamToMetric.put(MonitorType.VM, VmMapping.buildVmMetricsMapping());
		matrixParamToMetric.put(MonitorType.TEMPERATURE, TemperatureMapping.buildTemperatureMetricsMapping());
		matrixParamToMetric.put(MonitorType.CPU_CORE, CpuCoreMapping.buildCpuCoreMetricsMapping());
		matrixParamToMetric.put(MonitorType.DISK_CONTROLLER, DiskControllerMapping.buildDiskControllerMetricsMapping());
		matrixParamToMetric.put(MonitorType.FAN, FanMapping.buildFanMetricsMapping());
		matrixParamToMetric.put(MonitorType.LUN, LunMapping.buildLunMetricsMapping());
		matrixParamToMetric.put(MonitorType.LED, LedMapping.buildLedMetricsMapping());
		matrixParamToMetric.put(MonitorType.LOGICAL_DISK, LogicalDiskMapping.buildLogicalDiskMetricsMapping());
		matrixParamToMetric.put(MonitorType.BLADE, BladeMapping.buildBladeMetricsMapping());
		matrixParamToMetric.put(MonitorType.DISK_CONTROLLER, DiskControllerMapping.buildDiskControllerMetricsMapping());
		matrixParamToMetric.put(MonitorType.NETWORK_CARD, NetworkCardMapping.buildNetworkCardMetricsMapping());

		matrixParamToMetricMap = Collections.unmodifiableMap(matrixParamToMetric);

		final Map<MonitorType, Map<String, List<MetricInfo>>> metadataToMetric = new EnumMap<>(MonitorType.class);

		metadataToMetric.put(MonitorType.OTHER_DEVICE, OtherDeviceMapping.otherDeviceMetadataToMetrics());
		metadataToMetric.put(MonitorType.PHYSICAL_DISK, PhysicalDiskMapping.physicalDiskMetadataToMetrics());
		metadataToMetric.put(MonitorType.ROBOTICS, RoboticsMapping.roboticsMetadataToMetrics());
		metadataToMetric.put(MonitorType.TAPE_DRIVE, TapeDriveMapping.tapeDriveMetadataToMetrics());
		metadataToMetric.put(MonitorType.GPU, GpuMapping.gpuMetadataToMetrics());
		metadataToMetric.put(MonitorType.CPU, CpuMapping.cpuMetadataToMetrics());
		metadataToMetric.put(MonitorType.TEMPERATURE, TemperatureMapping.temperatureMetadataToMetrics());
		metadataToMetric.put(MonitorType.MEMORY, MemoryMapping.memoryMetadataToMetrics());
		metadataToMetric.put(MonitorType.FAN, FanMapping.fanMetadataToMetrics());
		metadataToMetric.put(MonitorType.LUN, LunMapping.lunMetadataToMetrics());
		metadataToMetric.put(MonitorType.VOLTAGE, VoltageMapping.voltageMetadataToMetrics());
		metadataToMetric.put(MonitorType.NETWORK_CARD, NetworkCardMapping.networkCardMetadataToMetrics());
		metadataToMetric.put(MonitorType.LOGICAL_DISK, LogicalDiskMapping.logicalDiskMetadataToMetrics());
		metadataToMetric.put(MonitorType.POWER_SUPPLY, PowerSupplyMapping.powerSupplyMetadataToMetrics());

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
		attributesMap.put(MonitorType.HOST, concatDefaultAttributesWithMetadata(MonitorType.HOST));
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
											.getOrDefault(metadataKey, MappingHelper.camelCaseToSnakeCase(metadataKey)),
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

	/**
	 * Build the default overridden attribute names to bypass the automatic renaming of the
	 * internal matrix metadata key
	 * 
	 * @return lookup indexed by the internal matrix metadata key
	 */
	public static Map<String, String> buildDefaultOverriddenAttributeNames() {
		return Map.of(
			IDENTIFYING_INFORMATION, INFO_ATTRIBUTE_KEY
		);
	}

}
