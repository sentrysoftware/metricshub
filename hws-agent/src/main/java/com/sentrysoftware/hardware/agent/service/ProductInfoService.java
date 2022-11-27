package com.sentrysoftware.hardware.agent.service;

import javax.annotation.PostConstruct;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import lombok.extern.slf4j.Slf4j;

@Service
@Slf4j
public class ProductInfoService {

	@Value("${project.name}")
	private String projectName;

	@Value("${project.version}")
	private String projectVersion;

	@Value("${buildNumber}")
	private String buildNumber;

	@Value("${buildDate}")
	private String buildDate;

	@Value("${hcVersion}")
	private String hcVersion;

	@Value("${otelVersion}")
	private String otelVersion;

	/**
	 * Logs the product information
	 */
	@PostConstruct
	public void logProductInformation() {

		if (isLogInfoEnabled()) {

			// Log product information
			log.info(
				"Product information:"
					+ "\nProduct name: {}"
					+ "\nProduct version: {}"
					+ "\nProduct build number: {}"
					+ "\nProduct build date: {}"
					+ "\nHardware Connector Library version: {}"
					+ "\nOpenTelemetry Collector Contrib version: {}"
					+ "\nJava version: {}"
					+ "\nJava Runtime Environment directory: {}"
					+ "\nOperating System: {} {}"
					+ "\nUser working directory: {}",
				projectName,
				projectVersion,
				buildNumber,
				buildDate,
				hcVersion,
				otelVersion,
				System.getProperty("java.version"),
				System.getProperty("java.home"),
				System.getProperty("os.name"), System.getProperty("os.arch"),
				System.getProperty("user.dir")
			);

		}
	}

	/**
	 * Whether the log info is enabled or not
	 * 
	 * @return boolean value
	 */
	static boolean isLogInfoEnabled() {
		return log.isInfoEnabled();
	}
}
