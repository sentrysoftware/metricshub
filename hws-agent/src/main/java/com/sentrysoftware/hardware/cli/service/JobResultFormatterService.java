package com.sentrysoftware.hardware.cli.service;

import com.sentrysoftware.matrix.model.monitoring.IHostMonitoring;

public class JobResultFormatterService {

	public static final JobResultFormatterService INSTANCE = new JobResultFormatterService();

	/**
	 * Parse and write the monitors of a host monitoring into a JSON format.
	 * @param hostMonitoring The hostMonitoring to parse.
	 * @return The data from the monitors parsed into a JSON format.
	 */
	public String format(final IHostMonitoring hostMonitoring) {

		if (hostMonitoring == null || hostMonitoring.getMonitors() == null || hostMonitoring.getMonitors().isEmpty()) {
			return "{}";
		}

		return hostMonitoring.toJson();
	}

}
