package com.sentrysoftware.hardware.agent;

import org.apache.logging.log4j.ThreadContext;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

import static com.sentrysoftware.hardware.agent.configuration.ConfigHelper.DEFAULT_OUTPUT_DIRECTORY;

import java.util.Arrays;

@SpringBootApplication
public class HardwareSentryAgentApp {

	private static final String SSL_ENABLED = "--server.ssl.enabled=true";

	public static void main(String[] args) {
		initializeLoggerContext();

		SpringApplication application = new SpringApplication(HardwareSentryAgentApp.class);

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
		ThreadContext.put("outputDirectory", DEFAULT_OUTPUT_DIRECTORY.toString());
	}
}
