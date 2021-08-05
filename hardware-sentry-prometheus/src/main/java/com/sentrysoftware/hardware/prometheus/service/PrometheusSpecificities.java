package com.sentrysoftware.hardware.prometheus.service;

import static com.sentrysoftware.hardware.prometheus.service.HostMonitoringCollectorService.LABELS;

import java.util.EnumMap;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import com.sentrysoftware.hardware.prometheus.dto.PrometheusParameter;
import com.sentrysoftware.matrix.connector.model.monitor.MonitorType;

public class PrometheusSpecificities {

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

		metricInfoLabels.put(MonitorType.BATTERY, concatLabelsWithMetadata(MonitorType.BATTERY));
		metricInfoLabels.put(MonitorType.BLADE, concatLabelsWithMetadata(MonitorType.BLADE));
		metricInfoLabels.put(MonitorType.CPU_CORE, concatLabelsWithMetadata(MonitorType.CPU_CORE));
		metricInfoLabels.put(MonitorType.CPU, concatLabelsWithMetadata(MonitorType.CPU));
		metricInfoLabels.put(MonitorType.DISK_CONTROLLER, concatLabelsWithMetadata(MonitorType.DISK_CONTROLLER));
		metricInfoLabels.put(MonitorType.ENCLOSURE, concatLabelsWithMetadata(MonitorType.ENCLOSURE));
		metricInfoLabels.put(MonitorType.FAN, concatLabelsWithMetadata(MonitorType.FAN));
		metricInfoLabels.put(MonitorType.LED, concatLabelsWithMetadata(MonitorType.LED));
		metricInfoLabels.put(MonitorType.LOGICAL_DISK, concatLabelsWithMetadata(MonitorType.LOGICAL_DISK));
		metricInfoLabels.put(MonitorType.LUN, concatLabelsWithMetadata(MonitorType.LUN));
		metricInfoLabels.put(MonitorType.TARGET, concatLabelsWithMetadata(MonitorType.TARGET));
		metricInfoLabels.put(MonitorType.MEMORY, concatLabelsWithMetadata(MonitorType.BATTERY));
		metricInfoLabels.put(MonitorType.NETWORK_CARD, concatLabelsWithMetadata(MonitorType.NETWORK_CARD));
		metricInfoLabels.put(MonitorType.OTHER_DEVICE, concatLabelsWithMetadata(MonitorType.OTHER_DEVICE));
		metricInfoLabels.put(MonitorType.PHYSICAL_DISK, concatLabelsWithMetadata(MonitorType.PHYSICAL_DISK));
		metricInfoLabels.put(MonitorType.POWER_SUPPLY, concatLabelsWithMetadata(MonitorType.POWER_SUPPLY));
		metricInfoLabels.put(MonitorType.ROBOTIC, concatLabelsWithMetadata(MonitorType.ROBOTIC));
		metricInfoLabels.put(MonitorType.TAPE_DRIVE, concatLabelsWithMetadata(MonitorType.TAPE_DRIVE));
		metricInfoLabels.put(MonitorType.TEMPERATURE, concatLabelsWithMetadata(MonitorType.TEMPERATURE));
		metricInfoLabels.put(MonitorType.VOLTAGE, concatLabelsWithMetadata(MonitorType.VOLTAGE));
		metricInfoLabels.put(MonitorType.CONNECTOR, concatLabelsWithMetadata(MonitorType.CONNECTOR));
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
	 * Get the monitor type predefined labels
	 * 
	 * @param monitorType The type of monitor
	 * @return List of string values
	 */
	public static List<String> getLabels(MonitorType monitorType) {
		return metricInfoLabels.get(monitorType);
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
}