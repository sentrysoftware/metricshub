package com.sentrysoftware.hardware.cli.service;

import org.springframework.stereotype.Service;

import com.sentrysoftware.matrix.model.monitoring.IHostMonitoring;

@Service
public class JobResultFormatterService {

	public String format(final IHostMonitoring hostMonitoring) {
		return "{ \"data\" : \"data\"}";
	}
}
