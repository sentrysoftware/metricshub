package com.sentrysoftware.hardware.prometheus;

import static com.sentrysoftware.hardware.prometheus.configuration.ConfigHelper.DEFAULT_OUTPUT_DIRECTORY;
import org.apache.logging.log4j.ThreadContext;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

import java.util.Arrays;

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
		// Default values for targetId, loggerLevel and port
		ThreadContext.put("port", "");
		ThreadContext.put("targetId", "");
		ThreadContext.put("loggerLevel", "OFF");
		ThreadContext.put("outputDirectory", DEFAULT_OUTPUT_DIRECTORY);
	}
}
