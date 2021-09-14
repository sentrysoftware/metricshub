package com.sentrysoftware.hardware.cli;

import org.apache.logging.log4j.ThreadContext;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

import java.nio.file.Paths;

@SpringBootApplication
public class HardwareSentryCliApplication {

	public static void main(String[] args) {

		// Default values for targetId and debugMode and outputDirectory
		ThreadContext.put("targetId", "no-target-yet");
		ThreadContext.put("debugMode", "false");

		// by default, the logs go in a directory "hardware-logs" in the temporary folder
		ThreadContext.put(
				"outputDirectory",
				Paths.get(System.getProperty("java.io.tmpdir"), "hardware-logs").toString()
		);

		System.exit(SpringApplication.exit(SpringApplication.run(HardwareSentryCliApplication.class, args)));
	}
}
