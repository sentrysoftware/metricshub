package com.sentrysoftware.hardware.prometheus;

import java.util.Arrays;

import org.apache.logging.log4j.ThreadContext;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

@SpringBootApplication
public class HardwareSentryPrometheusApp {

	private static final String SSL_ENABLED = "--server.ssl.enabled=true";

	public static void main(String[] args) {
		// Default values for targetId and debugMode
		ThreadContext.put("port", "");
		ThreadContext.put("targetId", "");
		ThreadContext.put("debugMode", "false");

		SpringApplication application = new SpringApplication(HardwareSentryPrometheusApp.class);

		if (Arrays.asList(args).contains(SSL_ENABLED)) {
			application.setAdditionalProfiles("ssl");
		}

		application.run(args);
	}
}
