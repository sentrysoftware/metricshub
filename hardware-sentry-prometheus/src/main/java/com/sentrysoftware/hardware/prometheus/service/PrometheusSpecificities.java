package com.sentrysoftware.hardware.prometheus.service;

import java.util.Map;
import java.util.TreeMap;

import com.sentrysoftware.hardware.prometheus.dto.PrometheusParameter;

public class PrometheusSpecificities {

	private static final String CURRENTSPEED = "currentspeed";
	private static final String CURRENT_SPEED = "current_speed";
	private static final String BYTES_PER_SECOND = "bytes_per_second";
	private static final String VOLTAGE = "voltage";
	private static final String FRACTION = "fraction";

	private PrometheusSpecificities() {
	}

	private static Map<String, Map<String, PrometheusParameter>> prometheusParameterSpecificities;
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
		parameterMapLogicalDisk.put("unallocatedspace",
				new PrometheusParameter("unallocated_space", "bytes", 1073741824.0));
		prometheusParameterSpecificities.put("logicaldisk", parameterMapLogicalDisk);

		TreeMap<String, PrometheusParameter> parameterMapNetCard = new TreeMap<>(String.CASE_INSENSITIVE_ORDER);
		parameterMapNetCard.put("bandwidthutilization",
				new PrometheusParameter("bandwidth_utilization", FRACTION, 0.01));
		parameterMapNetCard.put("errorpercent", new PrometheusParameter("error", FRACTION, 0.01));
		parameterMapNetCard.put("linkspeed", new PrometheusParameter("link_speed", BYTES_PER_SECOND, 125000.0));
		parameterMapNetCard.put("receivedbytesrate",
				new PrometheusParameter("received_rate", BYTES_PER_SECOND, 1048576.0));
		parameterMapNetCard.put("transmittedbytesrate",
				new PrometheusParameter("transmitted_rate", BYTES_PER_SECOND, 1048576.0));
		parameterMapNetCard.put("zerobuffercreditpercent",
				new PrometheusParameter("zero_buffer_credit", FRACTION, 0.01));
		prometheusParameterSpecificities.put("networkCard", parameterMapNetCard);

		TreeMap<String, PrometheusParameter> parameterMapPowerSupp = new TreeMap<>(String.CASE_INSENSITIVE_ORDER);
		prometheusParameterSpecificities.put("powersupply", parameterMapPowerSupp);
		parameterMapPowerSupp.put("usedcapacity", new PrometheusParameter("used_capacity", FRACTION, 0.01));

		TreeMap<String, PrometheusParameter> parameterMapVoltage = new TreeMap<>(String.CASE_INSENSITIVE_ORDER);
		parameterMapVoltage.put(VOLTAGE, new PrometheusParameter(VOLTAGE, "volts", 0.001));
		prometheusParameterSpecificities.put(VOLTAGE, parameterMapVoltage);
	}

	/**
	 * get the corresponding PrometheusParameter object that will gives the correct
	 * syntax for the parameter name and it corresponding unit and conversion factor
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

}