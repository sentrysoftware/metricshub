package com.sentrysoftware.hardware.prometheus.service;

import static com.sentrysoftware.hardware.prometheus.service.HostMonitoringCollectorService.LABELS;

import java.util.Collections;
import java.util.EnumMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.TreeMap;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import com.sentrysoftware.hardware.prometheus.dto.PrometheusParameter;
import com.sentrysoftware.hardware.prometheus.dto.PrometheusParameter.PrometheusMetricType;
import com.sentrysoftware.matrix.common.meta.monitor.Battery;
import com.sentrysoftware.matrix.common.meta.monitor.Blade;
import com.sentrysoftware.matrix.common.meta.monitor.Cpu;
import com.sentrysoftware.matrix.common.meta.monitor.CpuCore;
import com.sentrysoftware.matrix.common.meta.monitor.DiskController;
import com.sentrysoftware.matrix.common.meta.monitor.Enclosure;
import com.sentrysoftware.matrix.common.meta.monitor.Fan;
import com.sentrysoftware.matrix.common.meta.monitor.IMetaMonitor;
import com.sentrysoftware.matrix.common.meta.monitor.Led;
import com.sentrysoftware.matrix.common.meta.monitor.LogicalDisk;
import com.sentrysoftware.matrix.common.meta.monitor.Lun;
import com.sentrysoftware.matrix.common.meta.monitor.Memory;
import com.sentrysoftware.matrix.common.meta.monitor.NetworkCard;
import com.sentrysoftware.matrix.common.meta.monitor.OtherDevice;
import com.sentrysoftware.matrix.common.meta.monitor.PhysicalDisk;
import com.sentrysoftware.matrix.common.meta.monitor.PowerSupply;
import com.sentrysoftware.matrix.common.meta.monitor.Robotic;
import com.sentrysoftware.matrix.common.meta.monitor.TapeDrive;
import com.sentrysoftware.matrix.common.meta.monitor.Temperature;
import com.sentrysoftware.matrix.common.meta.monitor.Voltage;
import com.sentrysoftware.matrix.connector.model.monitor.MonitorType;

import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;

@NoArgsConstructor(access = AccessLevel.PRIVATE)
public class PrometheusSpecificities {

	private static final String PACKETS = "packets";
	private static final String BYTES = "bytes";
	private static final String JOULES = "joules";
	private static final String ERRORS = "errors";
	private static final String BYTES_PER_SECOND = "bytes_per_second";
	private static final String RATIO = "ratio";

	private static Map<MonitorType, List<String>> metricInfoLabels;

	@Getter
	private static Map<MonitorType, Map<String, PrometheusParameter>> prometheusParameters;

	private static Map<MonitorType, String> infoMetricNames;

	static {


		final Map<MonitorType, List<String>> labelsMap = new EnumMap<>(MonitorType.class);

		labelsMap.put(MonitorType.BATTERY, concatLabelsWithMetadata(MonitorType.BATTERY));
		labelsMap.put(MonitorType.BLADE, concatLabelsWithMetadata(MonitorType.BLADE));
		labelsMap.put(MonitorType.CPU_CORE, concatLabelsWithMetadata(MonitorType.CPU_CORE));
		labelsMap.put(MonitorType.CPU, concatLabelsWithMetadata(MonitorType.CPU));
		labelsMap.put(MonitorType.DISK_CONTROLLER, concatLabelsWithMetadata(MonitorType.DISK_CONTROLLER));
		labelsMap.put(MonitorType.ENCLOSURE, concatLabelsWithMetadata(MonitorType.ENCLOSURE));
		labelsMap.put(MonitorType.FAN, concatLabelsWithMetadata(MonitorType.FAN));
		labelsMap.put(MonitorType.LED, concatLabelsWithMetadata(MonitorType.LED));
		labelsMap.put(MonitorType.LOGICAL_DISK, concatLabelsWithMetadata(MonitorType.LOGICAL_DISK));
		labelsMap.put(MonitorType.LUN, concatLabelsWithMetadata(MonitorType.LUN));
		labelsMap.put(MonitorType.TARGET, concatLabelsWithMetadata(MonitorType.TARGET));
		labelsMap.put(MonitorType.MEMORY, concatLabelsWithMetadata(MonitorType.BATTERY));
		labelsMap.put(MonitorType.NETWORK_CARD, concatLabelsWithMetadata(MonitorType.NETWORK_CARD));
		labelsMap.put(MonitorType.OTHER_DEVICE, concatLabelsWithMetadata(MonitorType.OTHER_DEVICE));
		labelsMap.put(MonitorType.PHYSICAL_DISK, concatLabelsWithMetadata(MonitorType.PHYSICAL_DISK));
		labelsMap.put(MonitorType.POWER_SUPPLY, concatLabelsWithMetadata(MonitorType.POWER_SUPPLY));
		labelsMap.put(MonitorType.ROBOTIC, concatLabelsWithMetadata(MonitorType.ROBOTIC));
		labelsMap.put(MonitorType.TAPE_DRIVE, concatLabelsWithMetadata(MonitorType.TAPE_DRIVE));
		labelsMap.put(MonitorType.TEMPERATURE, concatLabelsWithMetadata(MonitorType.TEMPERATURE));
		labelsMap.put(MonitorType.VOLTAGE, concatLabelsWithMetadata(MonitorType.VOLTAGE));
		labelsMap.put(MonitorType.CONNECTOR, concatLabelsWithMetadata(MonitorType.CONNECTOR));

		metricInfoLabels = Collections.unmodifiableMap(labelsMap);

		final Map<MonitorType, Map<String, PrometheusParameter>> prometheusParametersMap = new EnumMap<>(MonitorType.class);

		prometheusParametersMap.put(MonitorType.BATTERY, buildBatteryPrometheusParameters());
		prometheusParametersMap.put(MonitorType.BLADE, buildBladePrometheusParameters());
		prometheusParametersMap.put(MonitorType.CONNECTOR, buildConnectorPrometheusParameters());
		prometheusParametersMap.put(MonitorType.CPU, buildCpuPrometheusParameters());
		prometheusParametersMap.put(MonitorType.CPU_CORE, buildCpuCorePrometheusParameters());
		prometheusParametersMap.put(MonitorType.DISK_CONTROLLER, buildDiskControllerPrometheusParameters());
		prometheusParametersMap.put(MonitorType.ENCLOSURE, buildEnclosurePrometheusParameters());
		prometheusParametersMap.put(MonitorType.FAN, buildFanPrometheusParameters());
		prometheusParametersMap.put(MonitorType.LED, buildLedPrometheusParameters());
		prometheusParametersMap.put(MonitorType.LOGICAL_DISK, buildLogicalDiskPrometheusParameters());
		prometheusParametersMap.put(MonitorType.LUN, buildLunPrometheusParameters());
		prometheusParametersMap.put(MonitorType.MEMORY, buildMemoryPrometheusParameters());
		prometheusParametersMap.put(MonitorType.NETWORK_CARD, buildNetworkCardPrometheusParameters());
		prometheusParametersMap.put(MonitorType.OTHER_DEVICE, buildOtherDevicePrometheusParameters());
		prometheusParametersMap.put(MonitorType.PHYSICAL_DISK, buildPhysicalDiskPrometheusParameters());
		prometheusParametersMap.put(MonitorType.POWER_SUPPLY, buildPowerSupplyPrometheusParameters());
		prometheusParametersMap.put(MonitorType.ROBOTIC, buildRoboticPrometheusParameters());
		prometheusParametersMap.put(MonitorType.TAPE_DRIVE, buildTapeDrivePrometheusParameters());
		prometheusParametersMap.put(MonitorType.TEMPERATURE, buildTemperaturePrometheusParameters());
		prometheusParametersMap.put(MonitorType.VOLTAGE, buildVoltagePrometheusParameters());
		prometheusParametersMap.put(MonitorType.TARGET, buildTargetPrometheusParameters());

		prometheusParameters = Collections.unmodifiableMap(prometheusParametersMap);

		final Map<MonitorType, String> infoMetricsMap = new EnumMap<>(MonitorType.class);

		infoMetricsMap.put(MonitorType.BATTERY, "hw_battery_info");
		infoMetricsMap.put(MonitorType.BLADE, "hw_blade_info");
		infoMetricsMap.put(MonitorType.CONNECTOR, "hw_connector_info");
		infoMetricsMap.put(MonitorType.CPU, "hw_cpu_info");
		infoMetricsMap.put(MonitorType.CPU_CORE, "hw_cpu_core_info");
		infoMetricsMap.put(MonitorType.DISK_CONTROLLER, "hw_disk_controller_info");
		infoMetricsMap.put(MonitorType.ENCLOSURE, "hw_enclosure_info");
		infoMetricsMap.put(MonitorType.FAN, "hw_fan_info");
		infoMetricsMap.put(MonitorType.LED, "hw_led_info");
		infoMetricsMap.put(MonitorType.LOGICAL_DISK, "hw_logical_disk_info");
		infoMetricsMap.put(MonitorType.LUN, "hw_lun_info");
		infoMetricsMap.put(MonitorType.MEMORY, "hw_memory_info");
		infoMetricsMap.put(MonitorType.NETWORK_CARD, "hw_network_card_info");
		infoMetricsMap.put(MonitorType.OTHER_DEVICE, "hw_other_device_info");
		infoMetricsMap.put(MonitorType.PHYSICAL_DISK, "hw_physical_disk_info");
		infoMetricsMap.put(MonitorType.POWER_SUPPLY, "hw_power_supply_info");
		infoMetricsMap.put(MonitorType.ROBOTIC, "hw_robotic_info");
		infoMetricsMap.put(MonitorType.TAPE_DRIVE, "hw_tape_drive_info");
		infoMetricsMap.put(MonitorType.TEMPERATURE, "hw_temperature_info");
		infoMetricsMap.put(MonitorType.VOLTAGE, "hw_voltage_info");
		infoMetricsMap.put(MonitorType.TARGET, "hw_target_info");

		infoMetricNames = Collections.unmodifiableMap(infoMetricsMap);
	}

	/**
	 * Build target prometheus parameters map
	 * 
	 * @return  {@link Map} where the prometheus parameters are indexed by the matrix parameter name
	 */
	private static Map<String, PrometheusParameter> buildTargetPrometheusParameters() {
		final Map<String, PrometheusParameter> map = new TreeMap<>(String.CASE_INSENSITIVE_ORDER);

		map.put(IMetaMonitor.STATUS.getName(), PrometheusParameter.builder()
				.name("hw_target_status")
				.unit(IMetaMonitor.STATUS.getUnit())
				.build());
		map.put(IMetaMonitor.ENERGY.getName(), PrometheusParameter.builder()
				.name("hw_target_energy_joules")
				.unit(JOULES)
				.type(PrometheusMetricType.COUNTER)
				.build());
		map.put(IMetaMonitor.HEATING_MARGIN.getName(), PrometheusParameter.builder()
				.name("hw_target_heating_margin_celsius")
				.unit("celsius")
				.build());

		return map;
	}

	/**
	 * Build voltage prometheus parameters map
	 * 
	 * @return  {@link Map} where the prometheus parameters are indexed by the matrix parameter name
	 */
	private static Map<String, PrometheusParameter> buildVoltagePrometheusParameters() {
		final Map<String, PrometheusParameter> map = new TreeMap<>(String.CASE_INSENSITIVE_ORDER);

		map.put(IMetaMonitor.STATUS.getName(), PrometheusParameter.builder()
				.name("hw_voltage_status")
				.unit(IMetaMonitor.STATUS.getUnit())
				.build());
		map.put(Voltage._VOLTAGE.getName(), PrometheusParameter.builder()
				.name("hw_voltage_volts")
				.unit("volts")
				.factor(0.001)
				.build());

		return map;
	}

	/**
	 * Build temperature prometheus parameters map
	 * 
	 * @return  {@link Map} where the prometheus parameters are indexed by the matrix parameter name
	 */
	private static Map<String, PrometheusParameter> buildTemperaturePrometheusParameters() {
		final Map<String, PrometheusParameter> map = new TreeMap<>(String.CASE_INSENSITIVE_ORDER);

		map.put(IMetaMonitor.STATUS.getName(), PrometheusParameter.builder()
				.name("hw_temperature_status")
				.unit(IMetaMonitor.STATUS.getUnit())
				.build());
		map.put(Temperature._TEMPERATURE.getName(), PrometheusParameter.builder()
				.name("hw_temperature_celsius")
				.unit(IMetaMonitor.STATUS.getUnit())
				.build());

		return map;
	}

	/**
	 * Build tape drive prometheus parameters map
	 * 
	 * @return  {@link Map} where the prometheus parameters are indexed by the matrix parameter name
	 */
	private static Map<String, PrometheusParameter> buildTapeDrivePrometheusParameters() {
		final Map<String, PrometheusParameter> map = new TreeMap<>(String.CASE_INSENSITIVE_ORDER);

		map.put(IMetaMonitor.STATUS.getName(), PrometheusParameter.builder()
				.name("hw_tape_drive_status")
				.unit(IMetaMonitor.STATUS.getUnit())
				.build());
		map.put(IMetaMonitor.PRESENT.getName(), PrometheusParameter.builder()
				.name("hw_tape_drive_present")
				.unit(IMetaMonitor.PRESENT.getUnit())
				.build());
		map.put(IMetaMonitor.ERROR_COUNT.getName(), PrometheusParameter.builder()
				.name("hw_tape_drive_errors")
				.unit(ERRORS)
				.type(PrometheusMetricType.COUNTER)
				.build());
		map.put(TapeDrive.MOUNT_COUNT.getName(), PrometheusParameter.builder()
				.name("hw_tape_drive_mounts")
				.unit(ERRORS)
				.type(PrometheusMetricType.COUNTER)
				.build());
		map.put(TapeDrive.NEEDS_CLEANING.getName(), PrometheusParameter.builder()
				.name("hw_tape_drive_needs_cleaning")
				.unit(TapeDrive.NEEDS_CLEANING.getUnit())
				.build());
		map.put(TapeDrive.UNMOUNT_COUNT.getName(), PrometheusParameter.builder()
				.name("hw_tape_drive_unmounts")
				.unit("unmounts")
				.type(PrometheusMetricType.COUNTER)
				.build());

		return map;
	}

	/**
	 * Build robotic prometheus parameters map
	 * 
	 * @return  {@link Map} where the prometheus parameters are indexed by the matrix parameter name
	 */
	private static Map<String, PrometheusParameter> buildRoboticPrometheusParameters() {
		final Map<String, PrometheusParameter> map = new TreeMap<>(String.CASE_INSENSITIVE_ORDER);

		map.put(IMetaMonitor.STATUS.getName(), PrometheusParameter.builder()
				.name("hw_robotic_status")
				.unit(IMetaMonitor.STATUS.getUnit())
				.build());
		map.put(IMetaMonitor.PRESENT.getName(), PrometheusParameter.builder()
				.name("hw_robotic_present")
				.unit(IMetaMonitor.PRESENT.getUnit())
				.build());
		map.put(IMetaMonitor.ERROR_COUNT.getName(), PrometheusParameter.builder()
				.name("hw_robotic_errors")
				.unit(ERRORS)
				.type(PrometheusMetricType.COUNTER)
				.build());
		map.put(Robotic.MOVE_COUNT.getName(), PrometheusParameter.builder()
				.name("hw_robotic_moves")
				.unit("moves")
				.type(PrometheusMetricType.COUNTER)
				.build());

		return map;
	}

	/**
	 * Build power supply prometheus parameters map
	 * 
	 * @return  {@link Map} where the prometheus parameters are indexed by the matrix parameter name
	 */
	private static Map<String, PrometheusParameter> buildPowerSupplyPrometheusParameters() {
		final Map<String, PrometheusParameter> map = new TreeMap<>(String.CASE_INSENSITIVE_ORDER);

		map.put(IMetaMonitor.STATUS.getName(), PrometheusParameter.builder()
				.name("hw_power_supply_status")
				.unit(IMetaMonitor.STATUS.getUnit())
				.build());
		map.put(IMetaMonitor.PRESENT.getName(), PrometheusParameter.builder()
				.name("hw_power_supply_present")
				.unit(IMetaMonitor.PRESENT.getUnit())
				.build());
		map.put(PowerSupply.USED_CAPACITY.getName(), PrometheusParameter.builder()
				.name("hw_power_supply_used_capacity_ratio")
				.unit(RATIO)
				.factor(0.01)
				.build());

		return map;
	}

	/**
	 * Build physical disk prometheus parameters map
	 * 
	 * @return  {@link Map} where the prometheus parameters are indexed by the matrix parameter name
	 */
	private static Map<String, PrometheusParameter> buildPhysicalDiskPrometheusParameters() {
		final Map<String, PrometheusParameter> map = new TreeMap<>(String.CASE_INSENSITIVE_ORDER);

		map.put(IMetaMonitor.STATUS.getName(), PrometheusParameter.builder()
				.name("hw_physical_disk_status")
				.unit(IMetaMonitor.STATUS.getUnit())
				.build());
		map.put(IMetaMonitor.PRESENT.getName(), PrometheusParameter.builder()
				.name("hw_physical_disk_present")
				.unit(IMetaMonitor.PRESENT.getUnit())
				.build());
		map.put(PhysicalDisk.INTRUSION_STATUS.getName(), PrometheusParameter.builder()
				.name("hw_physical_disk_intrusion_status")
				.unit(PhysicalDisk.INTRUSION_STATUS.getUnit())
				.build());
		map.put(PhysicalDisk.ENDURANCE_REMAINING.getName(), PrometheusParameter.builder()
				.name("hw_physical_disk_endurance_remaining_ratio")
				.unit(RATIO)
				.factor(0.01)
				.build());
		map.put(IMetaMonitor.ERROR_COUNT.getName(), PrometheusParameter.builder()
				.name("hw_physical_disk_errors")
				.unit(ERRORS)
				.type(PrometheusMetricType.COUNTER)
				.build());
		map.put(IMetaMonitor.PREDICTED_FAILURE.getName(), PrometheusParameter.builder()
				.name("hw_physical_disk_predicted_failure")
				.unit(IMetaMonitor.PREDICTED_FAILURE.getUnit())
				.build());

		return map;
	}

	/**
	 * Build other device prometheus parameters map
	 * 
	 * @return  {@link Map} where the prometheus parameters are indexed by the matrix parameter name
	 */
	private static Map<String, PrometheusParameter> buildOtherDevicePrometheusParameters() {
		final Map<String, PrometheusParameter> map = new TreeMap<>(String.CASE_INSENSITIVE_ORDER);

		map.put(IMetaMonitor.STATUS.getName(), PrometheusParameter.builder()
				.name("hw_other_device_status")
				.unit(IMetaMonitor.STATUS.getUnit())
				.build());
		map.put(IMetaMonitor.PRESENT.getName(), PrometheusParameter.builder()
				.name("hw_other_device_present")
				.unit(IMetaMonitor.PRESENT.getUnit())
				.build());
		map.put(OtherDevice.USAGE_COUNT.getName(), PrometheusParameter.builder()
				.name("hw_other_device_usage_times")
				.unit("times")
				.type(PrometheusMetricType.COUNTER)
				.build());
		map.put(OtherDevice.VALUE.getName(), PrometheusParameter.builder()
				.name("hw_other_device_value")
				.build());

		return map;
	}

	/**
	 * Build network card prometheus parameters map
	 * 
	 * @return  {@link Map} where the prometheus parameters are indexed by the matrix parameter name
	 */
	private static Map<String, PrometheusParameter> buildNetworkCardPrometheusParameters() {
		final Map<String, PrometheusParameter> map = new TreeMap<>(String.CASE_INSENSITIVE_ORDER);

		map.put(IMetaMonitor.STATUS.getName(), PrometheusParameter.builder()
				.name("hw_network_card_status")
				.unit(IMetaMonitor.STATUS.getUnit())
				.build());
		map.put(IMetaMonitor.PRESENT.getName(), PrometheusParameter.builder()
				.name("hw_network_card_present")
				.unit(IMetaMonitor.PRESENT.getUnit())
				.build());
		map.put(NetworkCard.BANDWIDTH_UTILIZATION.getName(), PrometheusParameter.builder()
				.name("hw_network_card_bandwidth_utilization_ratio")
				.unit(RATIO)
				.factor(0.01)
				.build());
		map.put(NetworkCard.DUPLEX_MODE.getName(), PrometheusParameter.builder()
				.name("hw_network_card_duplex_mode")
				.unit(NetworkCard.DUPLEX_MODE.getUnit())
				.build());
		map.put(IMetaMonitor.ERROR_COUNT.getName(), PrometheusParameter.builder()
				.name("hw_network_card_errors")
				.unit(ERRORS)
				.type(PrometheusMetricType.COUNTER)
				.build());
		map.put(NetworkCard.LINK_SPEED.getName(), PrometheusParameter.builder()
				.name("hw_network_card_link_speed_bytes_per_second")
				.unit(BYTES_PER_SECOND)
				.factor(125000.0)
				.build());
		map.put(NetworkCard.LINK_STATUS.getName(), PrometheusParameter.builder()
				.name("hw_network_card_link_status")
				.unit(NetworkCard.LINK_STATUS.getUnit())
				.build());
		map.put(NetworkCard.RECEIVED_BYTES.getName(), PrometheusParameter.builder()
				.name("hw_network_card_received_bytes")
				.unit(BYTES)
				.type(PrometheusMetricType.COUNTER)
				.build());
		map.put(NetworkCard.RECEIVED_PACKETS.getName(), PrometheusParameter.builder()
				.name("hw_network_card_received_packets")
				.unit(PACKETS)
				.type(PrometheusMetricType.COUNTER)
				.build());
		map.put(NetworkCard.TRANSMITTED_BYTES.getName(), PrometheusParameter.builder()
				.name("hw_network_card_transmitted_bytes")
				.unit(BYTES)
				.type(PrometheusMetricType.COUNTER)
				.build());
		map.put(NetworkCard.TRANSMITTED_PACKETS.getName(), PrometheusParameter.builder()
				.name("hw_network_card_transmitted_packets")
				.unit(PACKETS)
				.type(PrometheusMetricType.COUNTER)
				.build());
		map.put(NetworkCard.ZERO_BUFFER_CREDIT_COUNT.getName(), PrometheusParameter.builder()
				.name("hw_network_card_zero_buffer_credits")
				.unit("buffer_credits")
				.type(PrometheusMetricType.COUNTER)
				.build());

		return map;
	}

	/**
	 * Build memory prometheus parameters map
	 * 
	 * @return  {@link Map} where the prometheus parameters are indexed by the matrix parameter name
	 */
	private static Map<String, PrometheusParameter> buildMemoryPrometheusParameters() {
		final Map<String, PrometheusParameter> map = new TreeMap<>(String.CASE_INSENSITIVE_ORDER);

		map.put(IMetaMonitor.STATUS.getName(), PrometheusParameter.builder()
				.name("hw_memory_status")
				.unit(IMetaMonitor.STATUS.getUnit())
				.build());
		map.put(IMetaMonitor.PRESENT.getName(), PrometheusParameter.builder()
				.name("hw_memory_present")
				.unit(IMetaMonitor.PRESENT.getUnit())
				.build());
		map.put(IMetaMonitor.ERROR_COUNT.getName(), PrometheusParameter.builder()
				.name("hw_memory_errors")
				.unit(ERRORS)
				.type(PrometheusMetricType.COUNTER)
				.build());
		map.put(Memory.ERROR_STATUS.getName(),  PrometheusParameter.builder()
				.name("hw_memory_error_status")
				.unit(Memory.ERROR_STATUS.getUnit())
				.build());
		map.put(IMetaMonitor.PREDICTED_FAILURE.getName(), PrometheusParameter.builder()
				.name("hw_memory_predicted_failure")
				.unit(IMetaMonitor.PREDICTED_FAILURE.getUnit())
				.build());

		return map;
	}

	/**
	 * Build lun prometheus parameters map
	 * 
	 * @return  {@link Map} where the prometheus parameters are indexed by the matrix parameter name
	 */
	private static Map<String, PrometheusParameter> buildLunPrometheusParameters() {
		final Map<String, PrometheusParameter> map = new TreeMap<>(String.CASE_INSENSITIVE_ORDER);

		map.put(IMetaMonitor.STATUS.getName(), PrometheusParameter.builder()
				.name("hw_lun_status")
				.unit(IMetaMonitor.STATUS.getUnit())
				.build());
		map.put(Lun.AVAILABLE_PATH_COUNT.getName(), PrometheusParameter.builder()
				.name("hw_lun_available_paths")
				.unit(Lun.AVAILABLE_PATH_COUNT.getUnit())
				.build());

		return map;
	}

	/**
	 * Build logical disk prometheus parameters map
	 * 
	 * @return  {@link Map} where the prometheus parameters are indexed by the matrix parameter name
	 */
	private static Map<String, PrometheusParameter> buildLogicalDiskPrometheusParameters() {
		final Map<String, PrometheusParameter> map = new TreeMap<>(String.CASE_INSENSITIVE_ORDER);

		map.put(IMetaMonitor.STATUS.getName(), PrometheusParameter.builder()
				.name("hw_logical_disk_status")
				.unit(IMetaMonitor.STATUS.getUnit())
				.build());
		map.put(IMetaMonitor.ERROR_COUNT.getName(), PrometheusParameter.builder()
				.name("hw_logical_disk_errors")
				.unit(ERRORS)
				.type(PrometheusMetricType.COUNTER)
				.build());
		map.put(LogicalDisk.UNALLOCATED_SPACE.getName(), PrometheusParameter.builder()
				.name("hw_logical_disk_unallocated_space_bytes")
				.unit(BYTES)
				.factor(1073741824.0)
				.build());

		return map;
	}

	/**
	 * Build led prometheus parameters map
	 * 
	 * @return  {@link Map} where the prometheus parameters are indexed by the matrix parameter name
	 */
	private static Map<String, PrometheusParameter> buildLedPrometheusParameters() {
		final Map<String, PrometheusParameter> map = new TreeMap<>(String.CASE_INSENSITIVE_ORDER);

		map.put(IMetaMonitor.STATUS.getName(), PrometheusParameter.builder()
				.name("hw_led_status")
				.unit(IMetaMonitor.STATUS.getUnit())
				.build());
		map.put(Led.COLOR.getName(), PrometheusParameter.builder()
				.name("hw_led_color_status")
				.unit(Led.COLOR.getUnit())
				.build());
		map.put(Led.LED_INDICATOR.getName(), PrometheusParameter.builder()
				.name("hw_led_indicator_status")
				.unit(Led.LED_INDICATOR.getUnit())
				.build());

		return map;
	}

	/**
	 * Build fan prometheus parameters map
	 * 
	 * @return  {@link Map} where the prometheus parameters are indexed by the matrix parameter name
	 */
	private static Map<String, PrometheusParameter> buildFanPrometheusParameters() {
		final Map<String, PrometheusParameter> map = new TreeMap<>(String.CASE_INSENSITIVE_ORDER);

		map.put(IMetaMonitor.STATUS.getName(), PrometheusParameter.builder()
				.name("hw_fan_status")
				.unit(IMetaMonitor.STATUS.getUnit())
				.build());
		map.put(IMetaMonitor.PRESENT.getName(), PrometheusParameter.builder()
				.name("hw_fan_present")
				.unit(IMetaMonitor.PRESENT.getUnit())
				.build());
		map.put(Fan.SPEED.getName(), PrometheusParameter.builder()
				.name("hw_fan_speed_rpm")
				.unit("rpm")
				.build());
		map.put(Fan.SPEED_PERCENT.getName(), PrometheusParameter.builder()
				.name("hw_fan_speed_ratio")
				.unit(RATIO)
				.factor(0.01)
				.build());

		return map;
	}

	/**
	 * Build enclosure prometheus parameters map
	 * 
	 * @return {@link Map} where the prometheus parameters are indexed by the matrix parameter name
	 */
	private static Map<String, PrometheusParameter> buildEnclosurePrometheusParameters() {
		final Map<String, PrometheusParameter> map = new TreeMap<>(String.CASE_INSENSITIVE_ORDER);

		map.put(IMetaMonitor.STATUS.getName(), PrometheusParameter.builder()
				.name("hw_enclosure_status")
				.unit(IMetaMonitor.STATUS.getUnit())
				.build());
		map.put(Enclosure.INTRUSION_STATUS.getName(), PrometheusParameter.builder()
				.name("hw_enclosure_intrusion_status")
				.unit(Enclosure.INTRUSION_STATUS.getUnit())
				.build());
		map.put(IMetaMonitor.ENERGY.getName(), PrometheusParameter.builder()
				.name("hw_enclosure_energy_joules")
				.unit(JOULES)
				.type(PrometheusMetricType.COUNTER)
				.build());

		return map;
	}

	/**
	 * Build disk controller prometheus parameters map
	 * 
	 * @return {@link Map} where the prometheus parameters are indexed by the matrix parameter name
	 */
	private static Map<String, PrometheusParameter> buildDiskControllerPrometheusParameters() {
		final Map<String, PrometheusParameter> map = new TreeMap<>(String.CASE_INSENSITIVE_ORDER);

		map.put(IMetaMonitor.STATUS.getName(), PrometheusParameter.builder()
				.name("hw_disk_controller_status")
				.unit(IMetaMonitor.STATUS.getUnit())
				.build());
		map.put(IMetaMonitor.PRESENT.getName(), PrometheusParameter.builder()
				.name("hw_disk_controller_present")
				.unit(IMetaMonitor.PRESENT.getUnit())
				.build());
		map.put(DiskController.BATTERY_STATUS.getName(), PrometheusParameter.builder()
				.name("hw_disk_controller_battery_status")
				.unit(DiskController.BATTERY_STATUS.getUnit())
				.build());
		map.put(DiskController.CONTROLLER_STATUS.getName(), PrometheusParameter.builder()
				.name("hw_disk_controller_controller_status")
				.unit(DiskController.CONTROLLER_STATUS.getUnit())
				.build());

		return map;
	}

	/**
	 * Build cpu core prometheus parameters map
	 * 
	 * @return {@link Map} where the prometheus parameters are indexed by the matrix parameter name
	 */
	private static Map<String, PrometheusParameter> buildCpuCorePrometheusParameters() {
		final Map<String, PrometheusParameter> map = new TreeMap<>(String.CASE_INSENSITIVE_ORDER);

		map.put(IMetaMonitor.STATUS.getName(), PrometheusParameter.builder()
				.name("hw_cpu_core_status")
				.unit(IMetaMonitor.STATUS.getUnit())
				.build());
		map.put(IMetaMonitor.PRESENT.getName(), PrometheusParameter.builder()
				.name("hw_cpu_core_present")
				.unit(IMetaMonitor.PRESENT.getUnit())
				.build());
		map.put(CpuCore.CURRENT_SPEED.getName(), PrometheusParameter.builder()
				.name("hw_cpu_core_current_speed_hertz")
				.unit("hertz")
				.factor(1000000.0)
				.build());
		map.put(CpuCore.USED_TIME_PERCENT.getName(), PrometheusParameter.builder()
				.name("hw_cpu_core_used_time_ratio")
				.unit(RATIO)
				.factor(0.01)
				.build());

		return map;
	}

	/**
	 * Build cpu prometheus parameters map
	 * 
	 * @return {@link Map} where the prometheus parameters are indexed by the matrix parameter name
	 */
	private static Map<String, PrometheusParameter> buildCpuPrometheusParameters() {
		final Map<String, PrometheusParameter> map = new TreeMap<>(String.CASE_INSENSITIVE_ORDER);

		map.put(IMetaMonitor.STATUS.getName(), PrometheusParameter.builder()
				.name("hw_cpu_status")
				.unit(IMetaMonitor.STATUS.getUnit())
				.build());
		map.put(IMetaMonitor.PRESENT.getName(), PrometheusParameter.builder()
				.name("hw_cpu_present")
				.unit(IMetaMonitor.PRESENT.getUnit())
				.build());
		map.put(Cpu.CORRECTED_ERROR_COUNT.getName(), PrometheusParameter.builder()
				.name("hw_cpu_corrected_errors")
				.unit(Cpu.CORRECTED_ERROR_COUNT.getUnit())
				.type(PrometheusMetricType.COUNTER)
				.build());
		map.put(Cpu.CURRENT_SPEED.getName(), PrometheusParameter.builder()
				.name("hw_cpu_current_speed_hertz")
				.unit("hertz")
				.factor(1000000.0)
				.build());
		map.put(IMetaMonitor.PREDICTED_FAILURE.getName(), PrometheusParameter.builder()
				.name("hw_cpu_predicted_failure")
				.unit(IMetaMonitor.PREDICTED_FAILURE.getUnit())
				.build());


		return map;
	}

	/**
	 * Build connector prometheus parameters map
	 * 
	 * @return {@link Map} where the prometheus parameters are indexed by the matrix parameter name
	 */
	private static Map<String, PrometheusParameter> buildConnectorPrometheusParameters() {
		final Map<String, PrometheusParameter> map = new TreeMap<>(String.CASE_INSENSITIVE_ORDER);

		map.put(IMetaMonitor.STATUS.getName(), PrometheusParameter.builder()
				.name("hw_connector_status")
				.unit(IMetaMonitor.STATUS.getUnit())
				.build());

		return map;
	}

	/**
	 * Build blade prometheus parameters map
	 * 
	 * @return {@link Map} where the prometheus parameters are indexed by the matrix parameter name
	 */
	private static Map<String, PrometheusParameter> buildBladePrometheusParameters() {
		final Map<String, PrometheusParameter> map = new TreeMap<>(String.CASE_INSENSITIVE_ORDER);

		map.put(IMetaMonitor.STATUS.getName(), PrometheusParameter.builder()
				.name("hw_blade_status")
				.unit(IMetaMonitor.STATUS.getUnit())
				.build());
		map.put(IMetaMonitor.PRESENT.getName(), PrometheusParameter.builder()
				.name("hw_blade_present")
				.unit(IMetaMonitor.PRESENT.getUnit())
				.build());
		map.put(Blade.POWER_STATE.getName(), PrometheusParameter.builder()
				.name("hw_blade_power_state")
				.unit(Blade.POWER_STATE.getUnit())
				.build());

		return map;
	}

	/**
	 * Build battery prometheus parameters map
	 * 
	 * @return {@link Map} where the prometheus parameters are indexed by the matrix parameter name
	 */
	private static Map<String, PrometheusParameter> buildBatteryPrometheusParameters() {
		final Map<String, PrometheusParameter> map = new TreeMap<>(String.CASE_INSENSITIVE_ORDER);

		map.put(IMetaMonitor.STATUS.getName(), PrometheusParameter.builder()
				.name("hw_battery_status")
				.unit(IMetaMonitor.STATUS.getUnit())
				.build());
		map.put(IMetaMonitor.PRESENT.getName(), PrometheusParameter.builder()
				.name("hw_battery_present")
				.unit(IMetaMonitor.PRESENT.getUnit())
				.build());
		map.put(Battery.CHARGE.getName(), PrometheusParameter.builder()
				.name("hw_battery_charge_ratio")
				.unit(Battery.CHARGE.getUnit())
				.factor(0.01)
				.build());
		map.put(Battery.TIME_LEFT.getName(), PrometheusParameter.builder()
			.name("hw_battery_time_left")
			.unit(Battery.TIME_LEFT.getUnit())
			.build());

		return map;
	}

	/**
	 * Get the monitor type predefined labels
	 * 
	 * @param monitorType The type of monitor
	 * @return List of string values
	 */
	public static List<String> getLabels(MonitorType monitorType) {
		return metricInfoLabels.get(monitorType);
	}

	/**
	 * Get the monitor type predefined info metric name
	 * 
	 * @param monitorType The type of monitor
	 * @return String value. E.g fan_info
	 */
	public static String getInfoMetricName(MonitorType monitorType) {
		return infoMetricNames.get(monitorType);
	}

	/**
	 * Concatenate the Prometheus predefined labels with the specific monitor metadata
	 * 
	 * @param monitorType The monitor type we want to get its metadata
	 * @return List of String values
	 */
	private static List<String> concatLabelsWithMetadata(MonitorType monitorType) {
		return Stream.concat(LABELS.stream(), monitorType.getMetaMonitor().getMetadata().stream()).collect(Collectors.toList());
	}

	/**
	 * Get the corresponding PrometheusParameter object which gives the correct syntax for the parameter name and it corresponding unit and
	 * conversion factor
	 * 
	 * @param monitorType     The type of monitor defined by matrix
	 * @param matrixParameter The name of the matrix predefined parameter
	 * @return {@link Optional} {@link PrometheusParameter} since the parameter could be
	 */
	public static Optional<PrometheusParameter> getPrometheusParameter(final MonitorType monitorType, final String matrixParameter) {
		final Map<String, PrometheusParameter> parametersMap = prometheusParameters.get(monitorType);
		return parametersMap == null ? Optional.empty() : Optional.ofNullable(parametersMap.get(matrixParameter));
	}
}