package com.sentrysoftware.hardware.prometheus.service;

import java.util.AbstractMap;
import java.util.HashMap;
import java.util.Map;

import com.sentrysoftware.hardware.prometheus.dto.PrometheusParameters;


public class PrometheusSpecificities {
	
	private static final String CURRENTSPEED = "currentspeed";
	private static final String BYTES_PER_SECOND = "bytes_per_second";
	private static final String VOLTAGE = "voltage";
	private static final String FRACTION = "fraction";
	public static final String PIPE = "\\|";
	public static final String AMPERSAND = "&";
	
	public static Map<String, Map<String, PrometheusParameters>> prometheusParameterSpecificities;
	static {
		// if a monitor has multiple pramater to redefine, please use Map.ofEntries
		prometheusParameterSpecificities = new HashMap<>();
		prometheusParameterSpecificities.put("battery",
				Map.of("charge", new PrometheusParameters("charge", FRACTION, 0.01)));
		prometheusParameterSpecificities.put("cpu",
				Map.of(CURRENTSPEED, new PrometheusParameters(CURRENTSPEED, "hertz", 1000000.0)));

		prometheusParameterSpecificities.put("cpucore",
				Map.ofEntries(
						new AbstractMap.SimpleEntry<String, PrometheusParameters>(CURRENTSPEED,
								new PrometheusParameters("usedtime", FRACTION, 0.01)),
						new AbstractMap.SimpleEntry<String, PrometheusParameters>("usedtimepercent",
								new PrometheusParameters("usedtime", FRACTION, 0.01))));

		prometheusParameterSpecificities.put("fan",
				Map.of("speedpercent", new PrometheusParameters("speed", FRACTION, 0.01)));

		prometheusParameterSpecificities.put("logicaldisk",
				Map.of("unallocatedspace", new PrometheusParameters("unallocatedspace", "bytes", 1073741824.0)));

		prometheusParameterSpecificities.put("networkCard", Map.ofEntries(
				new AbstractMap.SimpleEntry<String, PrometheusParameters>("bandwidthutilization",
						new PrometheusParameters("bandwidthutilization", FRACTION, 0.01)),
				new AbstractMap.SimpleEntry<String, PrometheusParameters>("errorpercent",
						new PrometheusParameters("error", FRACTION, 0.01)),
				new AbstractMap.SimpleEntry<String, PrometheusParameters>("linkspeed",
						new PrometheusParameters("linkspeed", BYTES_PER_SECOND, 125000.0)),
				new AbstractMap.SimpleEntry<String, PrometheusParameters>("receivedbytesrate",
						new PrometheusParameters("receivedrate", BYTES_PER_SECOND, 1048576.0)),
				new AbstractMap.SimpleEntry<String, PrometheusParameters>("transmittedbytesrate",
						new PrometheusParameters("transmittedrate", BYTES_PER_SECOND, 1048576.0)),
				new AbstractMap.SimpleEntry<String, PrometheusParameters>("zerobuffercreditpercent",
						new PrometheusParameters("zerobuffercredit", FRACTION, 0.01))));

		prometheusParameterSpecificities.put("powersupply",
				Map.of("usedcapacity", new PrometheusParameters("usedcapacity", FRACTION, 0.01)));

		prometheusParameterSpecificities.put(VOLTAGE,
				Map.of(VOLTAGE, new PrometheusParameters(VOLTAGE, "volts", 0.001)));
	}

	public static String getPrometheusParameterName(String monitorType, String matrixParameter) {

		if (monitorType != null && matrixParameter != null) {
			if (prometheusParameterSpecificities.containsKey(monitorType)) {
				Map<String, PrometheusParameters> map = prometheusParameterSpecificities.get(monitorType);
				if (map.containsKey(matrixParameter)) {
					PrometheusParameters prometheusParameters = map.get(matrixParameter);
					return prometheusParameters.getPrometheusParameterName();
				}
			}
		}
		return null;
	}

	public static String getPrometheusParameterUnit(String monitorType, String matrixParameter) {

		if (monitorType != null && matrixParameter != null) {
			if (prometheusParameterSpecificities.containsKey(monitorType)) {
				Map<String, PrometheusParameters> map = prometheusParameterSpecificities.get(monitorType);
				if (map.containsKey(matrixParameter)) {
					PrometheusParameters prometheusParameters = map.get(matrixParameter);
					return prometheusParameters.getPrometheusParameterUnit();
				}
			}
		}

		return null;
	}

	public static Double getPrometheusParameterFactor(String monitorType, String matrixParameter) {

		if (monitorType != null && matrixParameter != null) {
			if (prometheusParameterSpecificities.containsKey(monitorType)) {
				Map<String, PrometheusParameters> map = prometheusParameterSpecificities.get(monitorType);
				if (map.containsKey(matrixParameter)) {
					PrometheusParameters prometheusParameters = map.get(matrixParameter);
					return prometheusParameters.getPrometheusParameterFactor();
				}
			}
		}
		return 1.0;
	}


}