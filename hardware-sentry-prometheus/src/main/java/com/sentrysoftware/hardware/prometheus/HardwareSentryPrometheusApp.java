package com.sentrysoftware.hardware.prometheus;

import java.util.Arrays;

import org.apache.logging.log4j.ThreadContext;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

@SpringBootApplication
public class HardwareSentryPrometheusApp {

	private static final String SSL_ENABLED = "--server.ssl.enabled=true";

	public static void main(String[] args) {
		initializeLoggerContext();

		SpringApplication application = new SpringApplication(HardwareSentryPrometheusApp.class);

		if (Arrays.asList(args).contains(SSL_ENABLED)) {
			application.setAdditionalProfiles("ssl");
		}

		application.run(args);
	}

	/**
	 * Initialize the log4j thread context values
	 */
	static void initializeLoggerContext() {
		// Default values for targetId, debugMode and port
		ThreadContext.put("port", "");
		ThreadContext.put("targetId", "");
		ThreadContext.put("debugMode", "false");

		// by default, the logs go in a directory "hardware-logs" in the temporary folder
		String outputDirectory = System.getProperty("java.io.tmpdir") + "hardware-logs";

		// Set the default property in system props so that it is always available and spring will be able to auto-wire the value
		System.setProperty("outputDirectory", outputDirectory);

		ThreadContext.put("outputDirectory", outputDirectory);
	}
}
