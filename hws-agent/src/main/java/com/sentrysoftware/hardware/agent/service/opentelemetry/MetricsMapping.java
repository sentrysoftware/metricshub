package com.sentrysoftware.hardware.agent.service.opentelemetry;

import static com.sentrysoftware.matrix.common.helpers.HardwareConstants.ALARM_THRESHOLD;
import static com.sentrysoftware.matrix.common.helpers.HardwareConstants.AVAILABLE_PATH_WARNING;
import static com.sentrysoftware.matrix.common.helpers.HardwareConstants.CORRECTED_ERROR_ALARM_THRESHOLD;
import static com.sentrysoftware.matrix.common.helpers.HardwareConstants.CORRECTED_ERROR_WARNING_THRESHOLD;
import static com.sentrysoftware.matrix.common.helpers.HardwareConstants.ERROR_COUNT_ALARM_THRESHOLD;
import static com.sentrysoftware.matrix.common.helpers.HardwareConstants.ERROR_COUNT_WARNING_THRESHOLD;
import static com.sentrysoftware.matrix.common.helpers.HardwareConstants.ERROR_PERCENT_ALARM_THRESHOLD;
import static com.sentrysoftware.matrix.common.helpers.HardwareConstants.ERROR_PERCENT_WARNING_THRESHOLD;
import static com.sentrysoftware.matrix.common.helpers.HardwareConstants.EXPECTED_PATH_COUNT;
import static com.sentrysoftware.matrix.common.helpers.HardwareConstants.HOSTNAME;
import static com.sentrysoftware.matrix.common.helpers.HardwareConstants.LOWER_THRESHOLD;
import static com.sentrysoftware.matrix.common.helpers.HardwareConstants.MAXIMUM_SPEED;
import static com.sentrysoftware.matrix.common.helpers.HardwareConstants.PERCENT_ALARM_THRESHOLD;
import static com.sentrysoftware.matrix.common.helpers.HardwareConstants.PERCENT_WARNING_THRESHOLD;
import static com.sentrysoftware.matrix.common.helpers.HardwareConstants.POWER_SUPPLY_POWER;
import static com.sentrysoftware.matrix.common.helpers.HardwareConstants.SIZE;
import static com.sentrysoftware.matrix.common.helpers.HardwareConstants.UPPER_THRESHOLD;
import static com.sentrysoftware.matrix.common.helpers.HardwareConstants.USAGE_COUNT_ALARM_THRESHOLD;
import static com.sentrysoftware.matrix.common.helpers.HardwareConstants.USAGE_COUNT_WARNING_THRESHOLD;
import static com.sentrysoftware.matrix.common.helpers.HardwareConstants.VALUE_ALARM_THRESHOLD;
import static com.sentrysoftware.matrix.common.helpers.HardwareConstants.VALUE_WARNING_THRESHOLD;
import static com.sentrysoftware.matrix.common.helpers.HardwareConstants.WARNING_THRESHOLD;
import static com.sentrysoftware.matrix.common.helpers.HardwareConstants.UP_PARAMETER_UNIT;

import java.util.Collections;
import java.util.EnumMap;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.TreeMap;
import java.util.function.Function;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import com.sentrysoftware.hardware.agent.dto.MetricInfo;
import com.sentrysoftware.hardware.agent.dto.MetricInfo.MetricType;
import com.sentrysoftware.hardware.agent.service.ServiceHelper;
import com.sentrysoftware.matrix.common.meta.monitor.Battery;
import com.sentrysoftware.matrix.common.meta.monitor.Blade;
import com.sentrysoftware.matrix.common.meta.monitor.Cpu;
import com.sentrysoftware.matrix.common.meta.monitor.CpuCore;
import com.sentrysoftware.matrix.common.meta.monitor.DiskController;
import com.sentrysoftware.matrix.common.meta.monitor.Enclosure;
import com.sentrysoftware.matrix.common.meta.monitor.Fan;
import com.sentrysoftware.matrix.common.meta.monitor.Gpu;
import com.sentrysoftware.matrix.common.meta.monitor.IMetaMonitor;
import com.sentrysoftware.matrix.common.meta.monitor.Led;
import com.sentrysoftware.matrix.common.meta.monitor.LogicalDisk;
import com.sentrysoftware.matrix.common.meta.monitor.Lun;
import com.sentrysoftware.matrix.common.meta.monitor.Memory;
import com.sentrysoftware.matrix.common.meta.monitor.NetworkCard;
import com.sentrysoftware.matrix.common.meta.monitor.OtherDevice;
import com.sentrysoftware.matrix.common.meta.monitor.PhysicalDisk;
import com.sentrysoftware.matrix.common.meta.monitor.PowerSupply;
import com.sentrysoftware.matrix.common.meta.monitor.Robotics;
import com.sentrysoftware.matrix.common.meta.monitor.TapeDrive;
import com.sentrysoftware.matrix.common.meta.monitor.Target;
import com.sentrysoftware.matrix.common.meta.monitor.Temperature;
import com.sentrysoftware.matrix.common.meta.monitor.Vm;
import com.sentrysoftware.matrix.common.meta.monitor.Voltage;
import com.sentrysoftware.matrix.connector.model.monitor.MonitorType;

import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;

@NoArgsConstructor(access = AccessLevel.PRIVATE)
public class MetricsMapping {

	public static final String LABEL = "label";
	public static final String PARENT = "parent";
	public static final String ID = "id";

	private static final String VOLTS = "V";
	private static final String HERTZ = "Hz";
	private static final String PACKETS = "packets";
	private static final String BYTES = "B";
	private static final String JOULES = "joules";
	private static final String WATTS = "W";
	private static final String BYTES_PER_SECOND = "B/s";
	private static final String CELSIUS = "°C";
	private static final String SECONDS = "s";
	private static final String RPM = "rpm";
	static final String VM_HOST_NAME = "vm.host.name";
	private static final String ALARM_THRESHOLD_OF_ERRORS = "Alarm threshold of the encountered errors.";
	private static final String WARNING_THRESHOLD_OF_ERRORS = "Warning threshold of the encountered errors.";


	protected static final Set<String> DEFAULT_ATTRIBUTE_NAMES = Set.of(ID, LABEL, PARENT);

	private static final Map<MonitorType, Map<String, String>> monitorTypeToOverriddenAttributeMap;

	private static final Map<MonitorType, Map<String, String>> monitorTypeToAttributeMap;

	@Getter
	private static final Map<MonitorType, Map<String, MetricInfo>> matrixParamToMetricMap;
	@Getter
	private static final Map<MonitorType, Map<String, MetricInfo>> matrixMetadataToMetricMap;

	public static final MetricInfo AGENT_METRIC_INFO = MetricInfo
			.builder()
			.name("hw.agent.info")
			.description("Agent information.")
			.type(MetricType.GAUGE)
			.build();

	static {

		final Map<MonitorType, Map<String, String>> overriddenAttributeNames = new EnumMap<>(MonitorType.class);

		overriddenAttributeNames.put(MonitorType.VM, Map.of(HOSTNAME, VM_HOST_NAME));

		monitorTypeToOverriddenAttributeMap = Collections.unmodifiableMap(overriddenAttributeNames);

		final Map<MonitorType, Map<String, MetricInfo>> matrixParamToMetric = new EnumMap<>(MonitorType.class);

		matrixParamToMetric.put(MonitorType.BATTERY, buildBatteryMetricsMapping());
		matrixParamToMetric.put(MonitorType.BLADE, buildBladeMetricsMapping());
		matrixParamToMetric.put(MonitorType.CONNECTOR, buildConnectorMetricsMapping());
		matrixParamToMetric.put(MonitorType.CPU, buildCpuMetricsMapping());
		matrixParamToMetric.put(MonitorType.CPU_CORE, buildCpuCoreMetricsMapping());
		matrixParamToMetric.put(MonitorType.DISK_CONTROLLER, buildDiskControllerMetricsMapping());
		matrixParamToMetric.put(MonitorType.ENCLOSURE, buildEnclosureMetricsMapping());
		matrixParamToMetric.put(MonitorType.FAN, buildFanMetricsMapping());
		matrixParamToMetric.put(MonitorType.GPU, buildGpuMetricsMapping());
		matrixParamToMetric.put(MonitorType.LED, buildLedMetricsMapping());
		matrixParamToMetric.put(MonitorType.LOGICAL_DISK, buildLogicalDiskMetricsMapping());
		matrixParamToMetric.put(MonitorType.LUN, buildLunMetricsMapping());
		matrixParamToMetric.put(MonitorType.MEMORY, buildMemoryMetricsMapping());
		matrixParamToMetric.put(MonitorType.NETWORK_CARD, buildNetworkCardMetricsMapping());
		matrixParamToMetric.put(MonitorType.OTHER_DEVICE, buildOtherDeviceMetricsMapping());
		matrixParamToMetric.put(MonitorType.PHYSICAL_DISK, buildPhysicalDiskMetricsMapping());
		matrixParamToMetric.put(MonitorType.POWER_SUPPLY, buildPowerSupplyMetricsMapping());
		matrixParamToMetric.put(MonitorType.ROBOTICS, buildRoboticsMetricsMapping());
		matrixParamToMetric.put(MonitorType.TAPE_DRIVE, buildTapeDriveMetricsMapping());
		matrixParamToMetric.put(MonitorType.TEMPERATURE, buildTemperatureMetricsMapping());
		matrixParamToMetric.put(MonitorType.VOLTAGE, buildVoltageMetricsMapping());
		matrixParamToMetric.put(MonitorType.VM, buildVmMetricsMapping());
		matrixParamToMetric.put(MonitorType.TARGET, buildTargetMetricsMapping());

		matrixParamToMetricMap = Collections.unmodifiableMap(matrixParamToMetric);

		final Map<MonitorType, Map<String, MetricInfo>> metadataToMetric = new EnumMap<>(MonitorType.class);

		metadataToMetric.put(MonitorType.CPU, cpuMetadataToMetrics());
		metadataToMetric.put(MonitorType.FAN, fanMetadataToMetrics());
		metadataToMetric.put(MonitorType.GPU, gpuMetadataToMetrics());
		metadataToMetric.put(MonitorType.LOGICAL_DISK, logicalDiskMetadataToMetrics());
		metadataToMetric.put(MonitorType.LUN, lunMetadataToMetrics());
		metadataToMetric.put(MonitorType.MEMORY, memoryMetadataToMetrics());
		metadataToMetric.put(MonitorType.PHYSICAL_DISK, physicalDiskMetadataToMetrics());
		metadataToMetric.put(MonitorType.NETWORK_CARD, networkCardMetadataToMetrics());
		metadataToMetric.put(MonitorType.OTHER_DEVICE, otherDeviceMetadataToMetrics());
		metadataToMetric.put(MonitorType.ROBOTICS, roboticsMetadataToMetrics());
		metadataToMetric.put(MonitorType.TAPE_DRIVE, tapeDriveMetadataToMetrics());
		metadataToMetric.put(MonitorType.TEMPERATURE, temperatureMetadataToMetrics());
		metadataToMetric.put(MonitorType.VOLTAGE, voltageMetadataToMetrics());
		metadataToMetric.put(MonitorType.POWER_SUPPLY, powerSupplyMetadataToMetrics());

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
	 * Build target metrics map
	 *
	 * @return  {@link Map} where the metrics are indexed by the matrix parameter name
	 */
	private static Map<String, MetricInfo> buildTargetMetricsMapping() {
		final Map<String, MetricInfo> map = new TreeMap<>(String.CASE_INSENSITIVE_ORDER);

		map.put(IMetaMonitor.STATUS.getName(), MetricInfo
				.builder()
				.name("hw.target.status")
				.unit(IMetaMonitor.STATUS.getUnit())
				.description("Target status.")
				.build());
		map.put(IMetaMonitor.ENERGY.getName(), MetricInfo
				.builder()
				.name("hw.target.energy_joules_total")
				.unit(JOULES)
				.type(MetricType.COUNTER)
				.description("Energy consumed by the components since the start of the Hardware Sentry agent.")
				.build());
		map.put(IMetaMonitor.POWER_CONSUMPTION.getName(), MetricInfo
				.builder()
				.name("hw.target.power_watts")
				.unit(WATTS)
				.type(MetricType.GAUGE)
				.description("Energy consumed by all the components discovered for the monitored target.")
				.build());
		map.put(IMetaMonitor.HEATING_MARGIN.getName(), MetricInfo
				.builder()
				.name("hw.target.heating_margin_celsius")
				.unit(CELSIUS)
				.description("Number of degrees Celsius (°C) remaining before the temperature reaches the closest warning threshold.")
				.build());
		map.put(Target.AMBIENT_TEMPERATURE.getName(), MetricInfo
				.builder()
				.name("hw.target.ambient_temperature_celsius")
				.unit(CELSIUS)
				.description("Target's current ambient temperature in degrees Celsius (°C).")
				.build());
		map.put(Target.CPU_TEMPERATURE.getName(), MetricInfo
				.builder()
				.name("hw.target.cpu_temperature_celsius")
				.unit(CELSIUS)
				.description("Target's CPU temperature in degrees Celsius (°C).")
				.build());
		map.put(IMetaMonitor.PRESENT.getName(), MetricInfo
				.builder()
				.name("hw.target.present")
				.unit(IMetaMonitor.PRESENT.getUnit())
				.description("Whether the target is found or not.")
				.build());
		map.put(Target.SNMP_UP.getName(), MetricInfo
				.builder()
				.name("hw.target.snmp.up")
				.unit(UP_PARAMETER_UNIT)
				.description("Whether the SNMP protocol is up or not")
				.build());
		map.put(Target.WMI_UP.getName(), MetricInfo
				.builder()
				.name("hw.target.wmi.up")
				.unit(UP_PARAMETER_UNIT)
				.description("Whether the WMI protocol is up or not")
				.build());
		map.put(Target.WBEM_UP.getName(), MetricInfo
				.builder()
				.name("hw.target.wbem.up")
				.unit(UP_PARAMETER_UNIT)
				.description("Whether the WBEM protocol is up or not")
				.build());
		map.put(Target.SSH_UP.getName(), MetricInfo
				.builder()
				.name("hw.target.ssh.up")
				.unit(UP_PARAMETER_UNIT)
				.description("Whether the SSH protocol is up or not")
				.build());
		map.put(Target.HTTP_UP.getName(), MetricInfo
				.builder()
				.name("hw.target.http.up")
				.unit(UP_PARAMETER_UNIT)
				.description("Whether the HTTP protocol is up or not")
				.build());
		map.put(Target.IPMI_UP.getName(), MetricInfo
				.builder()
				.name("hw.target.ipmi.up")
				.unit(UP_PARAMETER_UNIT)
				.description("Whether the IPMI protocol is up or not")
				.build());

		return map;
	}

	/**
	 * Create Robotics Metadata to metrics map
	 * 
	 * @return {@link Map} of {@link MetricInfo} instances indexed by the matrix parameter names
	 */
	private static Map<String, MetricInfo> roboticsMetadataToMetrics() {
		final Map<String, MetricInfo> map = new TreeMap<>(String.CASE_INSENSITIVE_ORDER);

		map.put(ERROR_COUNT_WARNING_THRESHOLD, MetricInfo
			.builder()
			.name("hw.robotics.errors_warning")
			.description(WARNING_THRESHOLD_OF_ERRORS)
			.build());

		map.put(ERROR_COUNT_ALARM_THRESHOLD, MetricInfo
			.builder()
			.name("hw.robotics.errors_alarm")
			.description(ALARM_THRESHOLD_OF_ERRORS)
			.build());

		return map;
	}

	/**
	 * Create Voltage Metadata to metrics map
	 * 
	 * @return {@link Map} of {@link MetricInfo} instances indexed by the matrix parameter names
	 */
	private static Map<String, MetricInfo> voltageMetadataToMetrics() {
		final Map<String, MetricInfo> map = new TreeMap<>(String.CASE_INSENSITIVE_ORDER);

		map.put(UPPER_THRESHOLD, MetricInfo
			.builder()
			.name("hw.voltage.voltage_volts_upper")
			.unit(VOLTS)
			.factor(0.001)
			.description("Upper threshold of the voltage.")
			.build());

		map.put(LOWER_THRESHOLD, MetricInfo
			.builder()
			.name("hw.voltage.voltage_volts_lower")
			.unit(VOLTS)
			.factor(0.001)
			.description("Lower threshold of the voltage.")
			.build());

		return map;
	}

	/**
	 * Create Temperature Metadata to metrics map
	 * 
	 * @return {@link Map} of {@link MetricInfo} instances indexed by the matrix parameter names
	 */
	private static Map<String, MetricInfo> temperatureMetadataToMetrics() {
		final Map<String, MetricInfo> map = new TreeMap<>(String.CASE_INSENSITIVE_ORDER);

		map.put(WARNING_THRESHOLD, MetricInfo
			.builder()
			.name("hw.temperature.temperature_celsius_warning")
			.unit(CELSIUS)
			.description("Current temperature in degrees Celsius (°C) that will generate a warning when reached.")
			.build());

		map.put(ALARM_THRESHOLD, MetricInfo
			.builder()
			.name("hw.temperature.temperature_celsius_alarm")
			.unit(CELSIUS)
			.description("Current temperature in degrees Celsius (°C) that will generate an alarm when reached.")
			.build());

		return map;
	}

	/**
	 * Create Tape Drive Metadata to metrics map
	 * 
	 * @return {@link Map} of {@link MetricInfo} instances indexed by the matrix parameter names
	 */
	private static Map<String, MetricInfo> tapeDriveMetadataToMetrics() {
		final Map<String, MetricInfo> map = new TreeMap<>(String.CASE_INSENSITIVE_ORDER);

		map.put(ERROR_COUNT_WARNING_THRESHOLD, MetricInfo
			.builder()
			.name("hw.tape_drive.errors_warning")
			.description(WARNING_THRESHOLD_OF_ERRORS)
			.build());

		map.put(ERROR_COUNT_ALARM_THRESHOLD, MetricInfo
			.builder()
			.name("hw.tape_drive.errors_alarm")
			.description(ALARM_THRESHOLD_OF_ERRORS)
			.build());

		return map;
	}

	/**
	 * Create Other Device Metadata to metrics map
	 * 
	 * @return {@link Map} of {@link MetricInfo} instances indexed by the matrix parameter names
	 */
	private static Map<String, MetricInfo> otherDeviceMetadataToMetrics() {
		final Map<String, MetricInfo> map = new TreeMap<>(String.CASE_INSENSITIVE_ORDER);

		map.put(USAGE_COUNT_WARNING_THRESHOLD, MetricInfo
			.builder()
			.name("hw.other_device.usage_times_warning")
			.description("Number of times the device has been used which will generate a warning when reached.")
			.build());
		map.put(USAGE_COUNT_ALARM_THRESHOLD, MetricInfo
			.builder()
			.name("hw.other_device.usage_times_alarm")
			.description("Number of times the device has been used which will generate an alarm when reached.")
			.build());

		map.put(VALUE_WARNING_THRESHOLD, MetricInfo
			.builder()
			.name("hw.other_device.value_warning")
			.description("Device reported value that will generate a warning when reached.")
			.build());
		map.put(VALUE_ALARM_THRESHOLD, MetricInfo
			.builder()
			.name("hw.other_device.value_alarm")
			.description("Device reported value that will generate an alarm when reached.")
			.build());

		return map;
	}

	/**
	 * Create NetworkCard Metadata to metrics map
	 * 
	 * @return {@link Map} of {@link MetricInfo} instances indexed by the matrix parameter names
	 */
	private static Map<String, MetricInfo> networkCardMetadataToMetrics() {
		final Map<String, MetricInfo> map = new TreeMap<>(String.CASE_INSENSITIVE_ORDER);

		map.put(ERROR_PERCENT_WARNING_THRESHOLD, MetricInfo
			.builder()
			.name("hw.network_card.error_ratio_warning")
			.factor(0.01)
			.description("Network interface error ratio that will generate a warning when reached.")
			.build());

		map.put(ERROR_PERCENT_ALARM_THRESHOLD, MetricInfo
			.builder()
			.name("hw.network_card.error_ratio_alarm")
			.factor(0.01)
			.description("Network interface error ratio that will generate an alarm when reached.")
			.build());

		return map;
	}

	/**
	 * Create LUN Metadata to metrics map
	 * 
	 * @return {@link Map} of {@link MetricInfo} instances indexed by the matrix parameter names
	 */
	private static Map<String, MetricInfo> lunMetadataToMetrics() {
		final Map<String, MetricInfo> map = new TreeMap<>(String.CASE_INSENSITIVE_ORDER);

		map.put(AVAILABLE_PATH_WARNING, MetricInfo
			.builder()
			.name("hw.lun.available_paths_warning")
			.description("Number of available paths that will generate a warning when reached.")
			.build());

		map.put(EXPECTED_PATH_COUNT, MetricInfo
			.builder()
			.name("hw.lun.expected_paths")
			.description("Number of paths that are expected to be available to the remote volume.")
			.build());

		return map;
	}

	/**
	 * Create Fan Metadata to metrics map
	 * 
	 * @return {@link Map} of {@link MetricInfo} instances indexed by the matrix parameter names
	 */
	private static Map<String, MetricInfo> fanMetadataToMetrics() {
		final Map<String, MetricInfo> map = new TreeMap<>(String.CASE_INSENSITIVE_ORDER);

		map.put(WARNING_THRESHOLD, MetricInfo
			.builder()
			.name("hw.fan.speed_rpm_warning")
			.unit(RPM)
			.description("Speed of the corresponding fan (in revolutions/minute) that will generate a warning when reached.")
			.build());

		map.put(ALARM_THRESHOLD, MetricInfo
			.builder()
			.name("hw.fan.speed_rpm_alarm")
			.unit(RPM)
			.description("Speed of the corresponding fan (in revolutions/minute) that will generate an alarm when reached.")
			.build());

		map.put(PERCENT_WARNING_THRESHOLD, MetricInfo
			.builder()
			.name("hw.fan.speed_ratio_warning")
			.factor(0.01)
			.description("Fan speed ratio that will generate a warning when reached.")
			.build());

		map.put(PERCENT_ALARM_THRESHOLD, MetricInfo
			.builder()
			.name("hw.fan.speed_ratio_alarm")
			.factor(0.01)
			.description("Fan speed ratio that will generate an alarm when reached.")
			.build());

		return map;
	}

	/**
	 * Create PhysicalDisk Metadata to metrics map
	 * 
	 * @return {@link Map} of {@link MetricInfo} instances indexed by the matrix parameter names
	 */
	private static Map<String, MetricInfo> physicalDiskMetadataToMetrics() {
		final Map<String, MetricInfo> map = new TreeMap<>(String.CASE_INSENSITIVE_ORDER);

		map.put(SIZE, MetricInfo
			.builder()
			.name("hw.physical_disk.size_bytes")
			.unit(BYTES)
			.description("Physical disk size.")
			.build());

		map.put(ERROR_COUNT_WARNING_THRESHOLD, MetricInfo
			.builder()
			.name("hw.physical_disk.errors_warning")
			.description(WARNING_THRESHOLD_OF_ERRORS)
			.build());

		map.put(ERROR_COUNT_ALARM_THRESHOLD, MetricInfo
			.builder()
			.name("hw.physical_disk.errors_alarm")
			.description(ALARM_THRESHOLD_OF_ERRORS)
			.build());

		return map;
	}

	/**
	 * Create Memory Metadata to metrics map
	 * 
	 * @return {@link Map} of {@link MetricInfo} instances indexed by the matrix parameter names
	 */
	private static Map<String, MetricInfo> memoryMetadataToMetrics() {
		final Map<String, MetricInfo> map = new TreeMap<>(String.CASE_INSENSITIVE_ORDER);

		map.put(SIZE, MetricInfo
			.builder()
			.name("hw.memory.size_bytes")
			.unit(BYTES)
			.factor(1000000.0) // MB to Bytes
			.description("Memory module size.")
			.build());

		map.put(ERROR_COUNT_WARNING_THRESHOLD, MetricInfo
			.builder()
			.name("hw.memory.errors_warning")
			.description(WARNING_THRESHOLD_OF_ERRORS)
			.build());

		map.put(ERROR_COUNT_ALARM_THRESHOLD, MetricInfo
			.builder()
			.name("hw.memory.errors_alarm")
			.description(ALARM_THRESHOLD_OF_ERRORS)
			.build());

		return map;
	}

	/**
	 * Create LogicalDisk Metadata to metrics map
	 * 
	 * @return {@link Map} of {@link MetricInfo} instances indexed by the matrix parameter names
	 */
	private static Map<String, MetricInfo> logicalDiskMetadataToMetrics() {
		final Map<String, MetricInfo> map = new TreeMap<>(String.CASE_INSENSITIVE_ORDER);

		map.put(SIZE, MetricInfo
			.builder()
			.name("hw.logical_disk.size_bytes")
			.unit(BYTES)
			.description("Logical disk size.")
			.build());

		map.put(ERROR_COUNT_WARNING_THRESHOLD, MetricInfo
			.builder()
			.name("hw.logical_disk.errors_warning")
			.description(WARNING_THRESHOLD_OF_ERRORS)
			.build());

		map.put(ERROR_COUNT_ALARM_THRESHOLD, MetricInfo
			.builder()
			.name("hw.logical_disk.errors_alarm")
			.description(ALARM_THRESHOLD_OF_ERRORS)
			.build());

		return map;
	}

	/**
	 * Build CPU Metadata to metrics
	 * 
	 * @return {@link Map} of {@link MetricInfo} instances indexed by the matrix parameter names
	 */
	private static Map<String, MetricInfo> cpuMetadataToMetrics() {
		final Map<String, MetricInfo> map = new TreeMap<>(String.CASE_INSENSITIVE_ORDER);

		map.put(MAXIMUM_SPEED, MetricInfo
			.builder()
			.name("hw.cpu.maximum_speed_hertz")
			.unit(HERTZ)
			.factor(1000000.0)
			.description("CPU maximum speed.")
			.build());

		map.put(CORRECTED_ERROR_WARNING_THRESHOLD, MetricInfo
			.builder()
			.name("hw.cpu.corrected_errors_warning")
			.description("Number of detected and corrected errors that will generate a warning.")
			.build());

		map.put(CORRECTED_ERROR_ALARM_THRESHOLD, MetricInfo
			.builder()
			.name("hw.cpu.corrected_errors_alarm")
			.description("Number of detected and corrected errors that will generate an alarm.")
			.build());

		return map;
	}

	/**
	 * Build GPU Metadata to metrics
	 *
	 * @return {@link Map} of {@link MetricInfo} instances indexed by the matrix parameter names
	 */
	private static Map<String, MetricInfo> gpuMetadataToMetrics() {
		final Map<String, MetricInfo> map = new TreeMap<>(String.CASE_INSENSITIVE_ORDER);

		map.put(SIZE, MetricInfo
			.builder()
			.name("hw.gpu.size_bytes")
			.unit(BYTES)
			.factor(1000000.0) // MB to Bytes
			.description("GPU memory size.")
			.build());

		map.put(CORRECTED_ERROR_WARNING_THRESHOLD, MetricInfo
			.builder()
			.name("hw.gpu.corrected_errors_warning")
			.description("Number of detected and corrected errors that will generate a warning.")
			.build());

		map.put(CORRECTED_ERROR_ALARM_THRESHOLD, MetricInfo
			.builder()
			.name("hw.gpu.corrected_errors_alarm")
			.description("Number of detected and corrected errors that will generate an alarm.")
			.build());

		return map;
	}

	/**
	 * Build Power Supply Metadata to metrics
	 *
	 * @return {@link Map} of {@link MetricInfo} instances indexed by the matrix parameter names
	 */
	private static Map<String, MetricInfo> powerSupplyMetadataToMetrics() {
		final Map<String, MetricInfo> map = new TreeMap<>(String.CASE_INSENSITIVE_ORDER);

		map.put(POWER_SUPPLY_POWER, MetricInfo
				.builder()
				.name("hw.power_supply.power_watts")
				.unit(WATTS)
				.description("Maximum power output.")
				.build());

		return map;
	}

	/**
	 * Build voltage metrics map
	 *
	 * @return  {@link Map} where the metrics are indexed by the matrix parameter name
	 */
	private static Map<String, MetricInfo> buildVoltageMetricsMapping() {
		final Map<String, MetricInfo> map = new TreeMap<>(String.CASE_INSENSITIVE_ORDER);

		map.put(IMetaMonitor.STATUS.getName(), MetricInfo
			.builder()
			.name("hw.voltage.status")
			.unit(IMetaMonitor.STATUS.getUnit())
			.description("Voltage status.")
			.build());
		map.put(Voltage._VOLTAGE.getName(), MetricInfo
			.builder()
			.name("hw.voltage.voltage_volts")
			.unit(VOLTS)
			.factor(0.001)
			.description("Voltage output.")
			.build());
		map.put(IMetaMonitor.PRESENT.getName(), MetricInfo
			.builder()
			.name("hw.voltage.present")
			.unit(IMetaMonitor.PRESENT.getUnit())
			.description("Whether the voltage sensor is found or not.")
			.build());

		return map;
	}

	/**
	 * Build temperature metrics map
	 *
	 * @return  {@link Map} where the metrics are indexed by the matrix parameter name
	 */
	private static Map<String, MetricInfo> buildTemperatureMetricsMapping() {
		final Map<String, MetricInfo> map = new TreeMap<>(String.CASE_INSENSITIVE_ORDER);

		map.put(IMetaMonitor.STATUS.getName(), MetricInfo
			.builder()
			.name("hw.temperature.status")
			.unit(IMetaMonitor.STATUS.getUnit())
			.description("Temperature status.")
			.build());
		map.put(Temperature._TEMPERATURE.getName(), MetricInfo
			.builder()
			.name("hw.temperature.temperature_celsius")
			.unit(CELSIUS)
			.description("Current temperature reading in Celsius degrees.")
			.build());
		map.put(IMetaMonitor.PRESENT.getName(), MetricInfo
			.builder()
			.name("hw.temperature.present")
			.unit(IMetaMonitor.PRESENT.getUnit())
			.description("Whether the temperature sensor is found or not.")
			.build());

		return map;
	}

	/**
	 * Build tape drive metrics map
	 *
	 * @return  {@link Map} where the metrics are indexed by the matrix parameter name
	 */
	private static Map<String, MetricInfo> buildTapeDriveMetricsMapping() {
		final Map<String, MetricInfo> map = new TreeMap<>(String.CASE_INSENSITIVE_ORDER);

		map.put(IMetaMonitor.STATUS.getName(), MetricInfo
			.builder()
			.name("hw.tape_drive.status")
			.unit(IMetaMonitor.STATUS.getUnit())
			.description("Tape drive status.")
			.build());
		map.put(IMetaMonitor.PRESENT.getName(), MetricInfo
			.builder()
			.name("hw.tape_drive.present")
			.unit(IMetaMonitor.PRESENT.getUnit())
			.description("Whether the tape drive is found or not.")
			.build());
		map.put(IMetaMonitor.ERROR_COUNT.getName(), MetricInfo
			.builder()
			.name("hw.tape_drive.errors_total")
			.type(MetricType.COUNTER)
			.description("Number of errors encountered by the tape drive since the start of the Hardware Sentry Agent.")
			.build());
		map.put(TapeDrive.MOUNT_COUNT.getName(), MetricInfo
			.builder()
			.name("hw.tape_drive.mounts_total")
			.type(MetricType.COUNTER)
			.description("Number of mount operations that occurred during the last collect interval.")
			.build());
		map.put(TapeDrive.NEEDS_CLEANING.getName(), MetricInfo
			.builder()
			.name("hw.tape_drive.needs_cleaning")
			.unit(TapeDrive.NEEDS_CLEANING.getUnit())
			.description("Whether the tape drive needs cleaning.")
			.build());
		map.put(TapeDrive.UNMOUNT_COUNT.getName(), MetricInfo
			.builder()
			.name("hw.tape_drive.unmounts_total")
			.unit("unmounts")
			.type(MetricType.COUNTER)
			.description("Number of unmount operations that occurred during the last collect interval.")
			.build());
		map.put(IMetaMonitor.ENERGY.getName(), MetricInfo
			.builder()
			.name("hw.tape_drive.energy_joules_total")
			.unit(JOULES)
			.type(MetricType.COUNTER)
			.description("Energy consumed by the tape drive since the start of the Hardware Sentry Agent.")
			.build());
		map.put(IMetaMonitor.POWER_CONSUMPTION.getName(), MetricInfo
			.builder()
			.name("hw.tape_drive.power_watts")
			.unit(WATTS)
			.type(MetricType.GAUGE)
			.description("Energy consumed by the tape drive.")
			.build());

		return map;
	}

	/**
	 * Build robotics metrics map
	 *
	 * @return  {@link Map} where the metrics are indexed by the matrix parameter name
	 */
	private static Map<String, MetricInfo> buildRoboticsMetricsMapping() {
		final Map<String, MetricInfo> map = new TreeMap<>(String.CASE_INSENSITIVE_ORDER);

		map.put(IMetaMonitor.STATUS.getName(), MetricInfo
			.builder()
			.name("hw.robotics.status")
			.unit(IMetaMonitor.STATUS.getUnit())
			.description("Robotic device status.")
			.build());
		map.put(IMetaMonitor.PRESENT.getName(), MetricInfo
			.builder()
			.name("hw.robotics.present")
			.unit(IMetaMonitor.PRESENT.getUnit())
			.description("Whether the robotic device is found or not.")
			.build());
		map.put(IMetaMonitor.ERROR_COUNT.getName(), MetricInfo
			.builder()
			.name("hw.robotics.errors_total")
			.type(MetricType.COUNTER)
			.description("Number of errors encountered by the robotic device since the start of the Hardware Sentry Agent.")
			.build());
		map.put(Robotics.MOVE_COUNT.getName(), MetricInfo
			.builder()
			.name("hw.robotics.moves_total")
			.unit("moves")
			.type(MetricType.COUNTER)
			.description("Number of moves operations that occurred during the last collect interval.")
			.build());
		map.put(IMetaMonitor.ENERGY.getName(), MetricInfo
			.builder()
			.name("hw.robotics.energy_joules_total")
			.unit(JOULES)
			.type(MetricType.COUNTER)
			.description("Energy consumed by the robotic device since the start of the Hardware Sentry Agent.")
			.build());
		map.put(IMetaMonitor.POWER_CONSUMPTION.getName(), MetricInfo
			.builder()
			.name("hw.robotics.power_watts")
			.unit(WATTS)
			.type(MetricType.GAUGE)
			.description("Energy consumed by the robotic device.")
			.build());

		return map;
	}

	/**
	 * Build power supply metrics map
	 *
	 * @return  {@link Map} where the metrics are indexed by the matrix parameter name
	 */
	private static Map<String, MetricInfo> buildPowerSupplyMetricsMapping() {
		final Map<String, MetricInfo> map = new TreeMap<>(String.CASE_INSENSITIVE_ORDER);

		map.put(IMetaMonitor.STATUS.getName(), MetricInfo
			.builder()
			.name("hw.power_supply.status")
			.unit(IMetaMonitor.STATUS.getUnit())
			.description("Power supply status.")
			.build());
		map.put(IMetaMonitor.PRESENT.getName(), MetricInfo
			.builder()
			.name("hw.power_supply.present")
			.unit(IMetaMonitor.PRESENT.getUnit())
			.description("Whether the power supply is found or not.")
			.build());
		map.put(PowerSupply.USED_CAPACITY.getName(), MetricInfo
			.builder()
			.name("hw.power_supply.used_capacity_ratio")
			.factor(0.01)
			.description("Ratio of the power supply power currently in use.")
			.build());

		return map;
	}

	/**
	 * Build physical disk metrics map
	 *
	 * @return  {@link Map} where the metrics are indexed by the matrix parameter name
	 */
	private static Map<String, MetricInfo> buildPhysicalDiskMetricsMapping() {
		final Map<String, MetricInfo> map = new TreeMap<>(String.CASE_INSENSITIVE_ORDER);

		map.put(IMetaMonitor.STATUS.getName(), MetricInfo
			.builder()
			.name("hw.physical_disk.status")
			.unit(IMetaMonitor.STATUS.getUnit())
			.description("Physical disk status.")
			.build());
		map.put(IMetaMonitor.PRESENT.getName(), MetricInfo
			.builder()
			.name("hw.physical_disk.present")
			.unit(IMetaMonitor.PRESENT.getUnit())
			.description("Whether the physical disk is found or not.")
			.build());
		map.put(PhysicalDisk.ENDURANCE_REMAINING.getName(), MetricInfo
			.builder()
			.name("hw.physical_disk.endurance_remaining_ratio")
			.factor(0.01)
			.description("Physical disk remaining endurance ratio.")
			.build());
		map.put(IMetaMonitor.ERROR_COUNT.getName(), MetricInfo
			.builder()
			.name("hw.physical_disk.errors_total")
			.type(MetricType.COUNTER)
			.description("Number of errors encountered by the physical disk since the start of the Hardware Sentry Agent.")
			.build());
		map.put(IMetaMonitor.PREDICTED_FAILURE.getName(), MetricInfo
			.builder()
			.name("hw.physical_disk.predicted_failure")
			.unit(IMetaMonitor.PREDICTED_FAILURE.getUnit())
			.description("Informs if a failure is predicted.")
			.build());
		map.put(IMetaMonitor.ENERGY.getName(), MetricInfo
			.builder()
			.name("hw.physical_disk.energy_joules_total")
			.unit(JOULES)
			.type(MetricType.COUNTER)
			.description("Energy consumed by the physical disk since the start of the Hardware Sentry Agent.")
			.build());
		map.put(IMetaMonitor.POWER_CONSUMPTION.getName(), MetricInfo
			.builder()
			.name("hw.physical_disk.power_watts")
			.unit(WATTS)
			.type(MetricType.GAUGE)
			.description("Energy consumed by the physical disk.")
			.build());

		return map;
	}

	/**
	 * Build other device metrics map
	 *
	 * @return  {@link Map} where the metrics are indexed by the matrix parameter name
	 */
	private static Map<String, MetricInfo> buildOtherDeviceMetricsMapping() {
		final Map<String, MetricInfo> map = new TreeMap<>(String.CASE_INSENSITIVE_ORDER);

		map.put(IMetaMonitor.STATUS.getName(), MetricInfo
			.builder()
			.name("hw.other_device.status")
			.unit(IMetaMonitor.STATUS.getUnit())
			.description("Other device status.")
			.build());
		map.put(IMetaMonitor.PRESENT.getName(), MetricInfo
			.builder()
			.name("hw.other_device.present")
			.unit(IMetaMonitor.PRESENT.getUnit())
			.description("Whether the device is found or not.")
			.build());
		map.put(OtherDevice.USAGE_COUNT.getName(), MetricInfo
			.builder()
			.name("hw.other_device.usage_times_total")
			.type(MetricType.COUNTER)
			.description("Number of times the device has been used.")
			.build());
		map.put(OtherDevice.VALUE.getName(), MetricInfo
			.builder()
			.name("hw.other_device.value")
			.description("Currently reported value of the device.")
			.build());

		return map;
	}

	/**
	 * Build network card metrics map
	 *
	 * @return  {@link Map} where the metrics are indexed by the matrix parameter name
	 */
	private static Map<String, MetricInfo> buildNetworkCardMetricsMapping() {
		final Map<String, MetricInfo> map = new TreeMap<>(String.CASE_INSENSITIVE_ORDER);

		map.put(IMetaMonitor.STATUS.getName(), MetricInfo.builder()
			.name("hw.network_card.status")
			.unit(IMetaMonitor.STATUS.getUnit())
			.description("Network interface status.")
			.build());
		map.put(IMetaMonitor.PRESENT.getName(), MetricInfo.builder()
			.name("hw.network_card.present")
			.unit(IMetaMonitor.PRESENT.getUnit())
			.description("Whether the network interface is found or not.")
			.build());
		map.put(NetworkCard.BANDWIDTH_UTILIZATION.getName(), MetricInfo.builder()
			.name("hw.network_card.bandwidth_utilization_ratio")
			.factor(0.01)
			.description("Ratio of the available bandwidth utilization.")
			.build());
		map.put(NetworkCard.DUPLEX_MODE.getName(), MetricInfo.builder()
			.name("hw.network_card.duplex_mode")
			.unit(NetworkCard.DUPLEX_MODE.getUnit())
			.description("Whether the port is configured to operate in half-duplex or full-duplex mode.")
			.build());
		map.put(IMetaMonitor.ERROR_COUNT.getName(), MetricInfo.builder()
			.name("hw.network_card.errors_total")
			.type(MetricType.COUNTER)
			.description("Number of sent and received packets that were in error.")
			.build());
		map.put(NetworkCard.LINK_SPEED.getName(), MetricInfo.builder()
			.name("hw.network_card.link_speed_bytes_per_second")
			.unit(BYTES_PER_SECOND)
			.factor(125000.0)
			.description("Speed that the network adapter and its remote counterpart currently use to communicate with each other.")
			.build());
		map.put(NetworkCard.LINK_STATUS.getName(), MetricInfo.builder()
			.name("hw.network_card.link_status")
			.unit(NetworkCard.LINK_STATUS.getUnit())
			.description("Whether the network interface is plugged-in to the network or not.")
			.build());
		map.put(NetworkCard.RECEIVED_BYTES.getName(), MetricInfo.builder()
			.name("hw.network_card.received_bytes_total")
			.unit(BYTES)
			.type(MetricType.COUNTER)
			.description("Total number of bytes received through the network interface.")
			.build());
		map.put(NetworkCard.RECEIVED_PACKETS.getName(), MetricInfo.builder()
			.name("hw.network_card.received_packets_total")
			.unit(PACKETS)
			.type(MetricType.COUNTER)
			.description("Total number of packets received through the network interface.")
			.build());
		map.put(NetworkCard.TRANSMITTED_BYTES.getName(), MetricInfo.builder()
			.name("hw.network_card.transmitted_bytes_total")
			.unit(BYTES)
			.type(MetricType.COUNTER)
			.description("Total number of bytes transmitted through the network interface.")
			.build());
		map.put(NetworkCard.TRANSMITTED_PACKETS.getName(), MetricInfo.builder()
			.name("hw.network_card.transmitted_packets_total")
			.unit(PACKETS)
			.type(MetricType.COUNTER)
			.description("Total number of packets transmitted through the network interface.")
			.build());
		map.put(NetworkCard.ZERO_BUFFER_CREDIT_COUNT.getName(), MetricInfo.builder()
			.name("hw.network_card.zero_buffer_credits_total")
			.type(MetricType.COUNTER)
			.description("Total number of zero buffer credits that occurred.")
			.build());
		map.put(IMetaMonitor.ENERGY.getName(), MetricInfo.builder()
			.name("hw.network_card.energy_joules_total")
			.unit(JOULES)
			.type(MetricType.COUNTER)
			.description("Energy consumed by the network interface since the start of the Hardware Sentry Agent.")
			.build());
		map.put(IMetaMonitor.POWER_CONSUMPTION.getName(), MetricInfo.builder()
			.name("hw.network_card.power_watts")
			.unit(WATTS)
			.type(MetricType.GAUGE)
			.description("Energy consumed by the network interface.")
			.build());
		map.put(NetworkCard.ERROR_PERCENT.getName(), MetricInfo.builder()
			.name("hw.network_card.error_ratio")
			.factor(0.01)
			.description("Ratio of sent and received packets that were in error.")
			.build());
		return map;
	}

	/**
	 * Build memory metrics map
	 *
	 * @return  {@link Map} where the metrics are indexed by the matrix parameter name
	 */
	private static Map<String, MetricInfo> buildMemoryMetricsMapping() {
		final Map<String, MetricInfo> map = new TreeMap<>(String.CASE_INSENSITIVE_ORDER);

		map.put(IMetaMonitor.STATUS.getName(), MetricInfo.builder()
			.name("hw.memory.status")
			.unit(IMetaMonitor.STATUS.getUnit())
			.description("Memory module status.")
			.build());
		map.put(IMetaMonitor.PRESENT.getName(), MetricInfo.builder()
			.name("hw.memory.present")
			.unit(IMetaMonitor.PRESENT.getUnit())
			.description("Whether the memory module is found or not.")
			.build());
		map.put(IMetaMonitor.ERROR_COUNT.getName(), MetricInfo.builder()
			.name("hw.memory.errors_total")
			.type(MetricType.COUNTER)
			.description("Number of errors encountered by the memory module since the start of the Hardware Sentry Agent.")
			.build());
		map.put(Memory.ERROR_STATUS.getName(),  MetricInfo.builder()
			.name("hw.memory.error_status")
			.unit(Memory.ERROR_STATUS.getUnit())
			.description("Error status of the memory module.")
			.build());
		map.put(IMetaMonitor.PREDICTED_FAILURE.getName(), MetricInfo.builder()
			.name("hw.memory.predicted_failure")
			.unit(IMetaMonitor.PREDICTED_FAILURE.getUnit())
			.description("Predicted failure set by analyzing the trend of the number of detected/corrected errors with the ECC technology.")
			.build());
		map.put(IMetaMonitor.ENERGY.getName(), MetricInfo.builder()
			.name("hw.memory.energy_joules_total")
			.unit(JOULES)
			.type(MetricType.COUNTER)
			.description("Energy consumed by the memory module since the start of the Hardware Sentry Agent.")
			.build());
		map.put(IMetaMonitor.POWER_CONSUMPTION.getName(), MetricInfo.builder()
			.name("hw.memory.power_watts")
			.unit(WATTS)
			.type(MetricType.GAUGE)
			.description("Energy consumed by the memory module.")
			.build());

		return map;
	}

	/**
	 * Build lun metrics map
	 *
	 * @return  {@link Map} where the metrics are indexed by the matrix parameter name
	 */
	private static Map<String, MetricInfo> buildLunMetricsMapping() {
		final Map<String, MetricInfo> map = new TreeMap<>(String.CASE_INSENSITIVE_ORDER);

		map.put(IMetaMonitor.STATUS.getName(), MetricInfo
			.builder()
			.name("hw.lun.status")
			.unit(IMetaMonitor.STATUS.getUnit())
			.description("LUN status.")
			.build());
		map.put(Lun.AVAILABLE_PATH_COUNT.getName(), MetricInfo
			.builder()
			.name("hw.lun.available_paths")
			.description("Number of distinct paths available to the remote volume.")
			.build());
		map.put(IMetaMonitor.PRESENT.getName(), MetricInfo
			.builder()
			.name("hw.lun.present")
			.unit(IMetaMonitor.PRESENT.getUnit())
			.description("Whether the LUN is found or not.")
			.build());

		return map;
	}

	/**
	 * Build logical disk metrics map
	 *
	 * @return  {@link Map} where the metrics are indexed by the matrix parameter name
	 */
	private static Map<String, MetricInfo> buildLogicalDiskMetricsMapping() {
		final Map<String, MetricInfo> map = new TreeMap<>(String.CASE_INSENSITIVE_ORDER);

		map.put(IMetaMonitor.STATUS.getName(), MetricInfo.builder()
			.name("hw.logical_disk.status")
			.unit(IMetaMonitor.STATUS.getUnit())
			.description("Logical disk status.")
			.build());
		map.put(IMetaMonitor.ERROR_COUNT.getName(), MetricInfo.builder()
			.name("hw.logical_disk.errors_total")
			.type(MetricType.COUNTER)
			.description("Number of errors encountered by the logical disk since the start of the Hardware Sentry Agent.")
			.build());
		map.put(LogicalDisk.UNALLOCATED_SPACE.getName(), MetricInfo.builder()
			.name("hw.logical_disk.unallocated_space_bytes")
			.unit(BYTES)
			.factor(1073741824.0)
			.description("Amount of unused disk space in the logical disk.")
			.build());
		map.put(IMetaMonitor.PRESENT.getName(), MetricInfo
			.builder()
			.name("hw.logical_disk.present")
			.unit(IMetaMonitor.PRESENT.getUnit())
			.description("Whether the logical disk is found or not.")
			.build());

		return map;
	}

	/**
	 * Build led metrics map
	 *
	 * @return  {@link Map} where the metrics are indexed by the matrix parameter name
	 */
	private static Map<String, MetricInfo> buildLedMetricsMapping() {
		final Map<String, MetricInfo> map = new TreeMap<>(String.CASE_INSENSITIVE_ORDER);

		map.put(IMetaMonitor.STATUS.getName(), MetricInfo.builder()
			.name("hw.led.status")
			.unit(IMetaMonitor.STATUS.getUnit())
			.description("LED status")
			.build());
		map.put(Led.COLOR.getName(), MetricInfo.builder()
			.name("hw.led.color_status")
			.unit(Led.COLOR.getUnit())
			.description("Color status of the LED as per the LED color definition.")
			.build());
		map.put(Led.LED_INDICATOR.getName(), MetricInfo.builder()
			.name("hw.led.indicator_status")
			.unit(Led.LED_INDICATOR.getUnit())
			.description("LED indicator status.")
			.build());
		map.put(IMetaMonitor.PRESENT.getName(), MetricInfo
			.builder()
			.name("hw.led.present")
			.unit(IMetaMonitor.PRESENT.getUnit())
			.description("Whether the LED is found or not.")
			.build());
		return map;
	}

	/**
	 * Build fan metrics map
	 *
	 * @return  {@link Map} where the metrics are indexed by the matrix parameter name
	 */
	private static Map<String, MetricInfo> buildFanMetricsMapping() {
		final Map<String, MetricInfo> map = new TreeMap<>(String.CASE_INSENSITIVE_ORDER);

		map.put(IMetaMonitor.STATUS.getName(), MetricInfo.builder()
			.name("hw.fan.status")
			.unit(IMetaMonitor.STATUS.getUnit())
			.description("Fan status.")
			.build());
		map.put(IMetaMonitor.PRESENT.getName(), MetricInfo.builder()
			.name("hw.fan.present")
			.unit(IMetaMonitor.PRESENT.getUnit())
			.description("Whether the fan is found or not.")
			.build());
		map.put(Fan.SPEED.getName(), MetricInfo.builder()
			.name("hw.fan.speed_rpm")
			.unit(RPM)
			.description("Fan speed.")
			.build());
		map.put(Fan.SPEED_PERCENT.getName(), MetricInfo.builder()
			.name("hw.fan.speed_ratio")
			.factor(0.01)
			.description("Fan speed ratio.")
			.build());
		map.put(IMetaMonitor.ENERGY.getName(), MetricInfo.builder()
			.name("hw.fan.energy_joules_total")
			.unit(JOULES)
			.type(MetricType.COUNTER)
			.description("Energy consumed by the fan since the start of the Hardware Sentry Agent.")
			.build());
		map.put(IMetaMonitor.POWER_CONSUMPTION.getName(), MetricInfo.builder()
			.name("hw.fan.power_watts")
			.unit(WATTS)
			.type(MetricType.GAUGE)
			.description("Energy consumed by the fan.")
			.build());

		return map;
	}

	/**
	 * Build enclosure metrics map
	 *
	 * @return {@link Map} where the metrics are indexed by the matrix parameter name
	 */
	private static Map<String, MetricInfo> buildEnclosureMetricsMapping() {
		final Map<String, MetricInfo> map = new TreeMap<>(String.CASE_INSENSITIVE_ORDER);

		map.put(IMetaMonitor.STATUS.getName(), MetricInfo.builder()
			.name("hw.enclosure.status")
			.unit(IMetaMonitor.STATUS.getUnit())
			.description("Enclosure status.")
			.build());
		map.put(IMetaMonitor.PRESENT.getName(), MetricInfo.builder()
			.name("hw.enclosure.present")
			.unit(IMetaMonitor.PRESENT.getUnit())
			.description("Whether the enclosure is found or not.")
			.build());
		map.put(Enclosure.INTRUSION_STATUS.getName(), MetricInfo.builder()
			.name("hw.enclosure.intrusion_status")
			.unit(Enclosure.INTRUSION_STATUS.getUnit())
			.description("Enclosure intrusion status. If the enclosure is open or not properly closed, it is set to 1.")
			.build());
		map.put(IMetaMonitor.ENERGY.getName(), MetricInfo.builder()
			.name("hw.enclosure.energy_joules_total")
			.unit(JOULES)
			.type(MetricType.COUNTER)
			.description("Energy consumed by the enclosure since the start of the Hardware Sentry Agent.")
			.build());
		map.put(IMetaMonitor.POWER_CONSUMPTION.getName(), MetricInfo.builder()
			.name("hw.enclosure.power_watts")
			.unit(WATTS)
			.type(MetricType.GAUGE)
			.description("Energy consumed by the enclosure.")
			.build());

		return map;
	}

	/**
	 * Build disk controller metrics map
	 *
	 * @return {@link Map} where the metrics are indexed by the matrix parameter name
	 */
	private static Map<String, MetricInfo> buildDiskControllerMetricsMapping() {
		final Map<String, MetricInfo> map = new TreeMap<>(String.CASE_INSENSITIVE_ORDER);

		map.put(IMetaMonitor.STATUS.getName(), MetricInfo.builder()
			.name("hw.disk_controller.status")
			.unit(IMetaMonitor.STATUS.getUnit())
			.description("Disk controller overall status.")
			.build());
		map.put(IMetaMonitor.PRESENT.getName(), MetricInfo.builder()
			.name("hw.disk_controller.present")
			.unit(IMetaMonitor.PRESENT.getUnit())
			.description("Whether the disk controller is found or not.")
			.build());
		map.put(DiskController.BATTERY_STATUS.getName(), MetricInfo.builder()
			.name("hw.disk_controller.battery_status")
			.unit(DiskController.BATTERY_STATUS.getUnit())
			.description("Disk controller battery status.")
			.build());
		map.put(DiskController.CONTROLLER_STATUS.getName(), MetricInfo.builder()
			.name("hw.disk_controller.controller_status")
			.unit(DiskController.CONTROLLER_STATUS.getUnit())
			.description("Disk controller status.")
			.build());
		map.put(IMetaMonitor.ENERGY.getName(), MetricInfo.builder()
			.name("hw.disk_controller.energy_joules_total")
			.unit(JOULES)
			.type(MetricType.COUNTER)
			.description("Energy consumed by the disk controller since the start of the Hardware Sentry Agent.")
			.build());
		map.put(IMetaMonitor.POWER_CONSUMPTION.getName(), MetricInfo.builder()
			.name("hw.disk_controller.power_watts")
			.unit(WATTS)
			.type(MetricType.GAUGE)
			.description("Energy consumed by the disk controller.")
			.build());

		return map;
	}

	/**
	 * Build CPU core metrics map
	 *
	 * @return {@link Map} where the metrics are indexed by the matrix parameter name
	 */
	private static Map<String, MetricInfo> buildCpuCoreMetricsMapping() {
		final Map<String, MetricInfo> map = new TreeMap<>(String.CASE_INSENSITIVE_ORDER);

		map.put(IMetaMonitor.STATUS.getName(), MetricInfo.builder()
			.name("hw.cpu_core.status")
			.unit(IMetaMonitor.STATUS.getUnit())
			.description("CPU core status.")
			.build());
		map.put(IMetaMonitor.PRESENT.getName(), MetricInfo.builder()
			.name("hw.cpu_core.present")
			.unit(IMetaMonitor.PRESENT.getUnit())
			.description("Whether the CPU core is found or not.")
			.build());
		map.put(CpuCore.CURRENT_SPEED.getName(), MetricInfo.builder()
			.name("hw.cpu_core.current_speed_hertz")
			.unit(HERTZ)
			.factor(1000000.0)
			.description("Current speed of the CPU core.")
			.build());
		map.put(CpuCore.USED_TIME_PERCENT.getName(), MetricInfo.builder()
			.name("hw.cpu_core.used_time_ratio")
			.factor(0.01)
			.description("Ratio of the CPU core usage.")
			.build());

		return map;
	}

	/**
	 * Build CPU metrics map
	 *
	 * @return {@link Map} where the metrics are indexed by the matrix parameter name
	 */
	private static Map<String, MetricInfo> buildCpuMetricsMapping() {
		final Map<String, MetricInfo> map = new TreeMap<>(String.CASE_INSENSITIVE_ORDER);

		map.put(IMetaMonitor.STATUS.getName(), MetricInfo.builder()
			.name("hw.cpu.status")
			.unit(IMetaMonitor.STATUS.getUnit())
			.description("CPU status.")
			.build());
		map.put(IMetaMonitor.PRESENT.getName(), MetricInfo.builder()
			.name("hw.cpu.present")
			.unit(IMetaMonitor.PRESENT.getUnit())
			.description("Whether the CPU is found or not.")
			.build());
		map.put(Cpu.CORRECTED_ERROR_COUNT.getName(), MetricInfo.builder()
			.name("hw.cpu.corrected_errors_total")
			.type(MetricType.COUNTER)
			.description("Number of detected and corrected errors.")
			.build());
		map.put(Cpu.CURRENT_SPEED.getName(), MetricInfo.builder()
			.name("hw.cpu.current_speed_hertz")
			.unit(HERTZ)
			.factor(1000000.0)
			.description("CPU current speed.")
			.build());
		map.put(IMetaMonitor.PREDICTED_FAILURE.getName(), MetricInfo.builder()
			.name("hw.cpu.predicted_failure")
			.unit(IMetaMonitor.PREDICTED_FAILURE.getUnit())
			.description("Predicted failure analysis performed by the CPU itself.")
			.build());
		map.put(IMetaMonitor.ENERGY.getName(), MetricInfo.builder()
			.name("hw.cpu.energy_joules_total")
			.unit(JOULES)
			.type(MetricType.COUNTER)
			.description("Energy consumed by the CPU since the start of the Hardware Sentry Agent.")
			.build());
		map.put(IMetaMonitor.POWER_CONSUMPTION.getName(), MetricInfo.builder()
			.name("hw.cpu.power_watts")
			.unit(WATTS)
			.type(MetricType.GAUGE)
			.description("Energy consumed by the CPU.")
			.build());

		return map;
	}

	/**
	 * Build connector metrics map
	 *
	 * @return {@link Map} where the metrics are indexed by the matrix parameter name
	 */
	private static Map<String, MetricInfo> buildConnectorMetricsMapping() {
		final Map<String, MetricInfo> map = new TreeMap<>(String.CASE_INSENSITIVE_ORDER);

		map.put(IMetaMonitor.STATUS.getName(), MetricInfo.builder()
			.name("hw.connector.status")
			.unit(IMetaMonitor.STATUS.getUnit())
			.description("Connector status.")
			.build());

		return map;
	}

	/**
	 * Build blade metrics map
	 *
	 * @return {@link Map} where the metrics are indexed by the matrix parameter name
	 */
	private static Map<String, MetricInfo> buildBladeMetricsMapping() {
		final Map<String, MetricInfo> map = new TreeMap<>(String.CASE_INSENSITIVE_ORDER);

		map.put(IMetaMonitor.STATUS.getName(), MetricInfo.builder()
			.name("hw.blade.status")
			.unit(IMetaMonitor.STATUS.getUnit())
			.description("Blade status.")
			.build());
		map.put(IMetaMonitor.PRESENT.getName(), MetricInfo.builder()
			.name("hw.blade.present")
			.unit(IMetaMonitor.PRESENT.getUnit())
			.description("Whether the blade is found or not.")
			.build());
		map.put(Blade.POWER_STATE.getName(), MetricInfo.builder()
			.name("hw.blade.power_state")
			.unit(Blade.POWER_STATE.getUnit())
			.description("Whether the blade is currently on or off.")
			.build());

		return map;
	}

	/**
	 * Build battery metrics map
	 *
	 * @return {@link Map} where the metrics are indexed by the matrix parameter name
	 */
	private static Map<String, MetricInfo> buildBatteryMetricsMapping() {
		final Map<String, MetricInfo> map = new TreeMap<>(String.CASE_INSENSITIVE_ORDER);

		map.put(IMetaMonitor.STATUS.getName(), MetricInfo.builder()
			.name("hw.battery.status")
			.unit(IMetaMonitor.STATUS.getUnit())
			.description("Battery status.")
			.build());
		map.put(IMetaMonitor.PRESENT.getName(), MetricInfo.builder()
			.name("hw.battery.present")
			.unit(IMetaMonitor.PRESENT.getUnit())
			.description("Whether the battery is found or not.")
			.build());
		map.put(Battery.CHARGE.getName(), MetricInfo.builder()
			.name("hw.battery.charge_ratio")
			.factor(0.01)
			.description("Battery charge ratio.")
			.build());
		map.put(Battery.TIME_LEFT.getName(), MetricInfo.builder()
			.name("hw.battery.time_left_seconds")
			.unit(SECONDS)
			.description("Number of seconds left before recharging the battery.")
			.build());

		return map;
	}

	/**
	 * Build VM metrics map
	 *
	 * @return {@link Map} where the metrics are indexed by the matrix parameter name
	 */
	private static Map<String, MetricInfo> buildVmMetricsMapping() {

		final Map<String, MetricInfo> map = new TreeMap<>(String.CASE_INSENSITIVE_ORDER);

		map.put(IMetaMonitor.STATUS.getName(), MetricInfo.builder()
			.name("hw.vm.status")
			.unit(IMetaMonitor.STATUS.getUnit())
			.description("Virtual machine status.")
			.build());
		map.put(IMetaMonitor.PRESENT.getName(), MetricInfo.builder()
			.name("hw.vm.present")
			.unit(IMetaMonitor.PRESENT.getUnit())
			.description("Whether the virtual machine is found or not.")
			.build());
		map.put(Vm.POWER_STATE.getName(), MetricInfo.builder()
			.name("hw.vm.power_state")
			.unit(Vm.POWER_STATE.getUnit())
			.description("Whether the state of the virtual machine is currently on, off or standby.")
			.build());
		map.put(IMetaMonitor.ENERGY.getName(), MetricInfo.builder()
			.name("hw.vm.energy_joules_total")
			.unit(JOULES)
			.type(MetricType.COUNTER)
			.description("Energy consumed by the virtual machine since the start of the Hardware Sentry Agent.")
			.build());
		map.put(IMetaMonitor.POWER_CONSUMPTION.getName(), MetricInfo.builder()
			.name("hw.vm.power_watts")
			.unit(WATTS)
			.type(MetricType.GAUGE)
			.description("Energy consumed by the virtual machine.")
			.build());

		return map;
	}

	/**
	 * Build GPU metrics map
	 *
	 * @return {@link Map} where the metrics are indexed by the matrix parameter name
	 */
	private static Map<String, MetricInfo> buildGpuMetricsMapping() {

		final Map<String, MetricInfo> map = new TreeMap<>(String.CASE_INSENSITIVE_ORDER);

		map.put(IMetaMonitor.STATUS.getName(), MetricInfo.builder()
			.name("hw.gpu.status")
			.unit(IMetaMonitor.STATUS.getUnit())
			.description("GPU status.")
			.build());
		map.put(IMetaMonitor.PRESENT.getName(), MetricInfo.builder()
			.name("hw.gpu.present")
			.unit(IMetaMonitor.PRESENT.getUnit())
			.description("Whether the GPU is found or not.")
			.build());
		map.put(Gpu.CORRECTED_ERROR_COUNT.getName(), MetricInfo.builder()
			.name("hw.gpu.corrected_errors_total")
			.type(MetricType.COUNTER)
			.description("Number of detected and corrected errors.")
			.build());
		map.put(IMetaMonitor.PREDICTED_FAILURE.getName(), MetricInfo.builder()
			.name("hw.gpu.predicted_failure")
			.unit(IMetaMonitor.PREDICTED_FAILURE.getUnit())
			.description("Predicted failure analysis performed by the GPU itself.")
			.build());
		map.put(Gpu.USED_TIME_PERCENT.getName(), MetricInfo.builder()
			.name("hw.gpu.used_time_ratio")
			.factor(0.01)
			.description("Ratio of time spent by the GPU doing any work.")
			.build());
		map.put(Gpu.DECODER_USED_TIME_PERCENT.getName(), MetricInfo.builder()
			.name("hw.gpu.decoder_used_time_ratio")
			.factor(0.01)
			.description("Ratio of time spent by the GPU decoding videos.")
			.build());
		map.put(Gpu.ENCODER_USED_TIME_PERCENT.getName(), MetricInfo.builder()
			.name("hw.gpu.encoder_used_time_ratio")
			.factor(0.01)
			.description("Ratio of time spent by the GPU encoding videos.")
			.build());
		map.put(Gpu.MEMORY_UTILIZATION.getName(), MetricInfo.builder()
			.name("hw.gpu.memory_utilization_ratio")
			.factor(0.01)
			.description("GPU memory utilization ratio.")
			.build());
		map.put(Gpu.RECEIVED_BYTES.getName(), MetricInfo.builder()
			.name("hw.gpu.received_bytes_total")
			.unit(BYTES)
			.type(MetricType.COUNTER)
			.description("Number of bytes received through the GPU.")
			.build());
		map.put(Gpu.TRANSMITTED_BYTES.getName(), MetricInfo.builder()
			.name("hw.gpu.transmitted_bytes_total")
			.unit(BYTES)
			.type(MetricType.COUNTER)
			.description("Number of bytes transmitted through the GPU.")
			.build());
		map.put(IMetaMonitor.ENERGY.getName(), MetricInfo.builder()
			.name("hw.gpu.energy_joules_total")
			.unit(JOULES)
			.type(MetricType.COUNTER)
			.description("Energy consumed by the GPU since the start of the Hardware Sentry Agent.")
			.build());
		map.put(IMetaMonitor.POWER_CONSUMPTION.getName(), MetricInfo.builder()
			.name("hw.gpu.power_watts")
			.unit(WATTS)
			.type(MetricType.GAUGE)
			.description("Energy consumed by the GPU.")
			.build());
		map.put(Gpu.ERROR_COUNT.getName(), MetricInfo.builder()
				.name("hw.gpu.errors_total")
				.type(MetricType.COUNTER)
				.description("Number of errors encountered by the GPU since the start of the Hardware Sentry Agent.")
				.build());

		return map;
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
		final Map<String, MetricInfo> metadataToMetricMap = matrixMetadataToMetricMap.get(monitorType);

		return metadataToMetricMap != null && metadataToMetricMap.containsKey(matrixMetadataName);
	}

	/**
	 * Get the corresponding MetricInfo object which gives the correct syntax for the parameter name and its corresponding unit and
	 * conversion factor
	 *
	 * @param monitorType         The type of monitor defined by matrix
	 * @param matrixParameterName The name of the matrix predefined parameter
	 * @return {@link Optional} {@link MetricInfo} since the parameter could be
	 */
	public static Optional<MetricInfo> getMetricInfo(final MonitorType monitorType, final String matrixParameterName) {
		final Map<String, MetricInfo> parametersMap = matrixParamToMetricMap.get(monitorType);
		return parametersMap == null ? Optional.empty() : Optional.ofNullable(parametersMap.get(matrixParameterName));
	}

	/**
	 * Get the corresponding MetricInfo object which gives the correct syntax for the matrix metadata, its corresponding unit and
	 * conversion factor
	 *
	 * @param monitorType     The type of monitor defined by matrix
	 * @param matrixMetadataKey  The name of the matrix predefined metadata
	 * @return {@link Optional} {@link MetricInfo} since the parameter could be
	 */
	public static Optional<MetricInfo> getMetadataAsMetricInfo(final MonitorType monitorType, final String matrixMetadataKey) {
		final Map<String, MetricInfo> parametersMap = matrixMetadataToMetricMap.get(monitorType);
		return (parametersMap == null || matrixMetadataKey == null) ? Optional.empty() : Optional.ofNullable(parametersMap.get(matrixMetadataKey));
	}

}
