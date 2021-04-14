package com.sentrysoftware.hardware.cli.service;

import java.util.HashSet;
import java.util.Map;
import java.util.Set;

import org.springframework.stereotype.Service;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.sentrysoftware.hardware.cli.helpers.StringHelper;
import com.sentrysoftware.matrix.connector.model.monitor.MonitorType;
import com.sentrysoftware.matrix.model.monitor.Monitor;
import com.sentrysoftware.matrix.model.monitoring.IHostMonitoring;

import lombok.extern.slf4j.Slf4j;

@Service
@Slf4j
public class JobResultFormatterService {

	/**
	 * Parse and write the monitors of a hostmonitoring into a JSON format.
	 * @param hostMonitoring The hostMonitoring to parse.
	 * @return The data from the monitors parsed into a JSON format.
	 */
	public String format(final IHostMonitoring hostMonitoring) {

		if (hostMonitoring == null || hostMonitoring.getMonitors() == null || hostMonitoring.getMonitors().isEmpty()) {
			return null;
		}

		boolean needsComa = false;
		String result = StringHelper.OPENING_CURLY_BRACKET;

		for (MonitorType monitorType : MonitorType.values()) {
			String monitorsStr = "";

			try {
				monitorsStr = parseMonitorMap(hostMonitoring, monitorType);
			} catch (JsonProcessingException e) {
				final String message = String.format("Matrix - Cannot parse %s", monitorType.name());
				log.error(message);
			}

			if (monitorsStr != null && !monitorsStr.isEmpty()) {
				if (needsComa) {
					result = result.concat(StringHelper.COMA);
				}
				result = result.concat(monitorsStr);
				needsComa = true;
			}
		}

		result = result.concat(StringHelper.NEW_LINE)
				.concat(StringHelper.CLOSING_CURLY_BRACKET);

		return result;
	}

	/**
	 * Parse and write the monitors of a specific type from a hostmonitoring into a JSON format.
	 * @param hostMonitoring The hostMonitoring to parse.
	 * @param monitorType The type of monitors to parse.
	 * @return The data from the monitors parsed into a JSON format.
	 * @throws JsonProcessingException When encountering an error during the JSON formating.
	 */
	private String parseMonitorMap (IHostMonitoring hostMonitoring, MonitorType monitorType)  throws JsonProcessingException {

		if (hostMonitoring == null || hostMonitoring.getMonitors() == null || hostMonitoring.getMonitors().isEmpty()) {
			return null;
		}

		Map<String, Monitor> monitorsMap = hostMonitoring.getMonitors().get(monitorType);
		Set<Monitor> monitors = new HashSet<>();

		if (monitorsMap != null && !monitorsMap.isEmpty() && monitorType != null) {
			for(String key : monitorsMap.keySet()) {
				Monitor monitor = monitorsMap.get(key);
				monitor.setDeviceId(key);
				monitor.setMonitorType(monitorType);
				monitors.add(monitor);
			}
		}

		return formatMonitorMap(monitors, monitorType);
	}

	/**
	 * Format a set of monitors into an array preceeded by the  type of monitors, in the JSON format.
	 * @param monitors The monitors to parse.
	 * @param monitorType The type of monitors.
	 * @return An array containing the monitors data in the JSON format.
	 * @throws JsonProcessingException When encountering an error during the JSON formating.
	 */
	private String formatMonitorMap(Set<Monitor> monitors, MonitorType monitorType) throws JsonProcessingException {
		String result = "";
		if (monitors != null && !monitors.isEmpty() && monitorType != null) {
			result = StringHelper.NEW_LINE
					.concat(StringHelper.DOUBLE_SPACE)
					.concat(StringHelper.DOUBLE_QUOTE)
					.concat(monitorType.name().toLowerCase())
					.concat(StringHelper.DOUBLE_QUOTE)
					.concat(StringHelper.SPACE)
					.concat(StringHelper.COLON)
					.concat(StringHelper.SPACE)
					.concat(new ObjectMapper().writerWithDefaultPrettyPrinter().writeValueAsString(monitors).replaceAll(StringHelper.NEW_LINE, StringHelper.NEW_LINE.concat(StringHelper.DOUBLE_SPACE)));
		}

		return result;
	}
}
