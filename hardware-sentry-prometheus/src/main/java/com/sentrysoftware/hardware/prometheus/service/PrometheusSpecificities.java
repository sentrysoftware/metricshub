package com.sentrysoftware.hardware.prometheus.service;

import static com.sentrysoftware.hardware.prometheus.service.HostMonitoringCollectorService.ID;
import static com.sentrysoftware.hardware.prometheus.service.HostMonitoringCollectorService.LABEL;
import static com.sentrysoftware.hardware.prometheus.service.HostMonitoringCollectorService.PARENT;
import static com.sentrysoftware.matrix.common.helpers.HardwareConstants.DESCRIPTION;
import static com.sentrysoftware.matrix.common.helpers.HardwareConstants.DISPLAY_NAME;
import static com.sentrysoftware.matrix.common.helpers.HardwareConstants.FILE_NAME;
import static com.sentrysoftware.matrix.common.helpers.HardwareConstants.FQDN;

import java.util.EnumMap;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;

import com.sentrysoftware.hardware.prometheus.dto.PrometheusParameter;
import com.sentrysoftware.matrix.connector.model.monitor.MonitorType;

public class PrometheusSpecificities {

	public static final String FAN_TYPE = "fanType";
	public static final String VOLTAGE_TYPE = "voltageType";
	public static final String TEMPERATURE_TYPE = "temperatureType";
	public static final String ROBOTIC_TYPE = "roboticType";
	public static final String POWER_SUPPLY_TYPE = "powerSupplyType";
	public static final String LOCATION = "location";
	public static final String BIOS_VERSION = "biosVersion";
	public static final String DEVICE_TYPE = "deviceType";
	public static final String LOGICAL_ADDRESS = "logicalAddress";
	public static final String PHYSICAL_ADDRESS = "physicalAddress";
	public static final String BANDWIDTH = "bandwidth";
	public static final String REMOTE_PHYSICAL_ADDRESS = "remotePhysicalAddress";
	public static final String WWN = "wwn";
	public static final String ARRAY_NAME = "arrayName";
	public static final String REMOTE_DEVICE_NAME = "remoteDeviceName";
	public static final String SIZE = "size";
	public static final String RAID_LEVEL = "raidLevel";
	public static final String LOCAL_DEVICE_NAME = "localDeviceName";
	public static final String EXPECTED_PATH_COUNT = "expectedPathCount";
	public static final String NAME = "Name";
	public static final String DRIVER_VERSION = "driverVersion";
	public static final String FIRMWARE_VERSION = "firmwareVersion";
	public static final String MAXIMUM_SPEED = "maximumSpeed";
	public static final String BLADE_NAME = "bladeName";
	public static final String SERIAL_NUMBER = "serialNumber";
	public static final String ADDITIONAL_INFORMATION3 = "additionalInformation3";
	public static final String ADDITIONAL_INFORMATION2 = "additionalInformation2";
	public static final String ADDITIONAL_INFORMATION1 = "additionalInformation1";
	public static final String CHEMISTRY = "chemistry";
	public static final String TYPE = "type";
	public static final String MODEL = "model";
	public static final String VENDOR = "vendor";
	public static final String DEVICE_ID = "deviceId";
	private static final String CURRENTSPEED = "currentspeed";
	private static final String CURRENT_SPEED = "current_speed";
	private static final String BYTES_PER_SECOND = "bytes_per_second";
	private static final String VOLTAGE = "voltage";
	private static final String FRACTION = "fraction";

	private PrometheusSpecificities() {
	}

	private static Map<String, Map<String, PrometheusParameter>> prometheusParameterSpecificities;
	private static Map<MonitorType, List<String>> metricInfoLabels;

	static {
		prometheusParameterSpecificities = new TreeMap<>(String.CASE_INSENSITIVE_ORDER);

		TreeMap<String, PrometheusParameter> parameterMapCharge = new TreeMap<>(String.CASE_INSENSITIVE_ORDER);
		parameterMapCharge.put("charge", new PrometheusParameter("charge", FRACTION, 0.01));
		prometheusParameterSpecificities.put("battery", parameterMapCharge);

		TreeMap<String, PrometheusParameter> parameterMapCpu = new TreeMap<>(String.CASE_INSENSITIVE_ORDER);
		parameterMapCpu.put(CURRENTSPEED, new PrometheusParameter(CURRENT_SPEED, "hertz", 1000000.0));
		prometheusParameterSpecificities.put("cpu", parameterMapCpu);

		TreeMap<String, PrometheusParameter> parameterMapCpuCore = new TreeMap<>(String.CASE_INSENSITIVE_ORDER);
		parameterMapCpuCore.put(CURRENTSPEED, new PrometheusParameter(CURRENT_SPEED, FRACTION, 0.01));
		parameterMapCpuCore.put("usedtimepercent", new PrometheusParameter("used_time", FRACTION, 0.01));
		prometheusParameterSpecificities.put("cpucore", parameterMapCpuCore);

		TreeMap<String, PrometheusParameter> parameterMapFan = new TreeMap<>(String.CASE_INSENSITIVE_ORDER);
		parameterMapFan.put("speedpercent", new PrometheusParameter("speed_fraction", FRACTION, 0.01));
		prometheusParameterSpecificities.put("fan", parameterMapFan);

		TreeMap<String, PrometheusParameter> parameterMapLogicalDisk = new TreeMap<>(String.CASE_INSENSITIVE_ORDER);
		parameterMapLogicalDisk.put("unallocatedspace", new PrometheusParameter("unallocated_space", "bytes", 1073741824.0));
		prometheusParameterSpecificities.put("logicaldisk", parameterMapLogicalDisk);

		TreeMap<String, PrometheusParameter> parameterMapNetCard = new TreeMap<>(String.CASE_INSENSITIVE_ORDER);
		parameterMapNetCard.put("bandwidthutilization", new PrometheusParameter("bandwidth_utilization", FRACTION, 0.01));
		parameterMapNetCard.put("errorpercent", new PrometheusParameter("error", FRACTION, 0.01));
		parameterMapNetCard.put("linkspeed", new PrometheusParameter("link_speed", BYTES_PER_SECOND, 125000.0));
		parameterMapNetCard.put("receivedbytesrate", new PrometheusParameter("received_rate", BYTES_PER_SECOND, 1048576.0));
		parameterMapNetCard.put("transmittedbytesrate", new PrometheusParameter("transmitted_rate", BYTES_PER_SECOND, 1048576.0));
		parameterMapNetCard.put("zerobuffercreditpercent", new PrometheusParameter("zero_buffer_credit", FRACTION, 0.01));
		prometheusParameterSpecificities.put("networkCard", parameterMapNetCard);

		TreeMap<String, PrometheusParameter> parameterMapPowerSupp = new TreeMap<>(String.CASE_INSENSITIVE_ORDER);
		prometheusParameterSpecificities.put("powersupply", parameterMapPowerSupp);
		parameterMapPowerSupp.put("usedcapacity", new PrometheusParameter("used_capacity", FRACTION, 0.01));

		TreeMap<String, PrometheusParameter> parameterMapVoltage = new TreeMap<>(String.CASE_INSENSITIVE_ORDER);
		parameterMapVoltage.put(VOLTAGE, new PrometheusParameter(VOLTAGE, "volts", 0.001));
		prometheusParameterSpecificities.put(VOLTAGE, parameterMapVoltage);

		metricInfoLabels = new EnumMap<>(MonitorType.class);

		metricInfoLabels.put(MonitorType.BATTERY, List.of(ID, PARENT, LABEL, FQDN, DEVICE_ID, VENDOR, MODEL, TYPE, CHEMISTRY, ADDITIONAL_INFORMATION1,
				ADDITIONAL_INFORMATION2, ADDITIONAL_INFORMATION3));
		metricInfoLabels.put(MonitorType.BLADE, List.of(ID, PARENT, LABEL, FQDN, DEVICE_ID, SERIAL_NUMBER, MODEL, BLADE_NAME, ADDITIONAL_INFORMATION1,
				ADDITIONAL_INFORMATION2, ADDITIONAL_INFORMATION3));
		metricInfoLabels.put(MonitorType.CPU_CORE,
				List.of(ID, PARENT, LABEL, FQDN, DEVICE_ID, ADDITIONAL_INFORMATION1, ADDITIONAL_INFORMATION2, ADDITIONAL_INFORMATION3));
		metricInfoLabels.put(MonitorType.CPU, List.of(ID, PARENT, LABEL, FQDN, DEVICE_ID, VENDOR, MODEL, MAXIMUM_SPEED, ADDITIONAL_INFORMATION1,
				ADDITIONAL_INFORMATION2, ADDITIONAL_INFORMATION3));
		metricInfoLabels.put(MonitorType.DISK_CONTROLLER, List.of(ID, PARENT, LABEL, FQDN, DEVICE_ID, SERIAL_NUMBER, VENDOR, MODEL, BIOS_VERSION,
				FIRMWARE_VERSION, DRIVER_VERSION, ADDITIONAL_INFORMATION1, ADDITIONAL_INFORMATION2, ADDITIONAL_INFORMATION3));
		metricInfoLabels.put(MonitorType.ENCLOSURE, List.of(ID, PARENT, LABEL, FQDN, DEVICE_ID, SERIAL_NUMBER, VENDOR, MODEL, BIOS_VERSION, TYPE,
				ADDITIONAL_INFORMATION1, ADDITIONAL_INFORMATION2, ADDITIONAL_INFORMATION3));
		metricInfoLabels.put(MonitorType.FAN,
				List.of(ID, PARENT, LABEL, FQDN, DEVICE_ID, FAN_TYPE, ADDITIONAL_INFORMATION1, ADDITIONAL_INFORMATION2, ADDITIONAL_INFORMATION3));
		metricInfoLabels.put(MonitorType.LED,
				List.of(ID, PARENT, LABEL, FQDN, DEVICE_ID, NAME, ADDITIONAL_INFORMATION1, ADDITIONAL_INFORMATION2, ADDITIONAL_INFORMATION3));
		metricInfoLabels.put(MonitorType.LOGICAL_DISK, List.of(ID, PARENT, LABEL, FQDN, DEVICE_ID, RAID_LEVEL, SIZE, ADDITIONAL_INFORMATION1,
				ADDITIONAL_INFORMATION2, ADDITIONAL_INFORMATION3));
		metricInfoLabels.put(MonitorType.LUN, List.of(ID, PARENT, LABEL, FQDN, DEVICE_ID, LOCAL_DEVICE_NAME, REMOTE_DEVICE_NAME, ARRAY_NAME, WWN,
				EXPECTED_PATH_COUNT, ADDITIONAL_INFORMATION1, ADDITIONAL_INFORMATION2, ADDITIONAL_INFORMATION3));
		metricInfoLabels.put(MonitorType.TARGET, List.of(ID, PARENT, LABEL, FQDN, LOCATION));
		metricInfoLabels.put(MonitorType.MEMORY, List.of(ID, PARENT, LABEL, FQDN, DEVICE_ID, SERIAL_NUMBER, VENDOR, MODEL, TYPE, SIZE, ADDITIONAL_INFORMATION1,
				ADDITIONAL_INFORMATION2, ADDITIONAL_INFORMATION3));
		metricInfoLabels.put(MonitorType.NETWORK_CARD, List.of(ID, PARENT, LABEL, FQDN, DEVICE_ID, SERIAL_NUMBER, VENDOR, MODEL, BANDWIDTH, PHYSICAL_ADDRESS,
				LOGICAL_ADDRESS, REMOTE_PHYSICAL_ADDRESS, ADDITIONAL_INFORMATION1, ADDITIONAL_INFORMATION2, ADDITIONAL_INFORMATION3));
		metricInfoLabels.put(MonitorType.OTHER_DEVICE,
				List.of(ID, PARENT, LABEL, FQDN, DEVICE_ID, DEVICE_TYPE, ADDITIONAL_INFORMATION1, ADDITIONAL_INFORMATION2, ADDITIONAL_INFORMATION3));
		metricInfoLabels.put(MonitorType.PHYSICAL_DISK, List.of(ID, PARENT, LABEL, FQDN, DEVICE_ID, SERIAL_NUMBER, VENDOR, MODEL, FIRMWARE_VERSION, SIZE,
				ADDITIONAL_INFORMATION1, ADDITIONAL_INFORMATION2, ADDITIONAL_INFORMATION3));
		metricInfoLabels.put(MonitorType.POWER_SUPPLY, List.of(ID, PARENT, LABEL, FQDN, DEVICE_ID, POWER_SUPPLY_TYPE, ADDITIONAL_INFORMATION1,
				ADDITIONAL_INFORMATION2, ADDITIONAL_INFORMATION3));
		metricInfoLabels.put(MonitorType.ROBOTIC, List.of(ID, PARENT, LABEL, FQDN, DEVICE_ID, SERIAL_NUMBER, VENDOR, MODEL, ROBOTIC_TYPE, ADDITIONAL_INFORMATION1,
				ADDITIONAL_INFORMATION2, ADDITIONAL_INFORMATION3));
		metricInfoLabels.put(MonitorType.TAPE_DRIVE, List.of(ID, PARENT, LABEL, FQDN, DEVICE_ID, SERIAL_NUMBER, VENDOR, MODEL, ADDITIONAL_INFORMATION1,
				ADDITIONAL_INFORMATION2, ADDITIONAL_INFORMATION3));
		metricInfoLabels.put(MonitorType.TEMPERATURE, List.of(ID, PARENT, LABEL, FQDN, DEVICE_ID, TEMPERATURE_TYPE, ADDITIONAL_INFORMATION1,
				ADDITIONAL_INFORMATION2, ADDITIONAL_INFORMATION3));
		metricInfoLabels.put(MonitorType.VOLTAGE,
				List.of(ID, PARENT, LABEL, FQDN, DEVICE_ID, VOLTAGE_TYPE, ADDITIONAL_INFORMATION1, ADDITIONAL_INFORMATION2, ADDITIONAL_INFORMATION3));
		metricInfoLabels.put(MonitorType.CONNECTOR,
				List.of(ID, PARENT, LABEL, FQDN, DISPLAY_NAME, FILE_NAME, DESCRIPTION));
	}

	/**
	 * get the corresponding PrometheusParameter object that will gives the correct syntax for the parameter name and it corresponding unit and
	 * conversion factor
	 * 
	 * @param monitorType
	 * @param matrixParameter
	 * @return
	 */
	public static PrometheusParameter getPrometheusParameter(String monitorType, String matrixParameter) {

		if (prometheusParameterSpecificities.containsKey(monitorType)) {
			Map<String, PrometheusParameter> map = prometheusParameterSpecificities.get(monitorType);
			if (map.containsKey(matrixParameter)) {
				return map.get(matrixParameter);
			}
		}
		return null;
	}

	/**
	 * Get the specific labels used for the given monitor type
	 * 
	 * @param monitorType The type of monitor
	 * @return List of string values
	 */
	public static List<String> getSpecificLabels(MonitorType monitorType) {
		return metricInfoLabels.get(monitorType);
	}
}