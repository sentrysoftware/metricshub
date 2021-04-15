package com.sentrysoftware.hardware.cli.service;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.sentrysoftware.hardware.cli.component.cli.HardwareSentryCLI;

@Service
public class EngineService {

	@Autowired
	private JobResultFormatterService jobResultFormatterService;

	public String call(final HardwareSentryCLI data) {

		System.out.println("EngineService called with data " + data.toString());

		// Call the formatter with the HostMonitoring object
		return jobResultFormatterService.format(null);
	}
}
