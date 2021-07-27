package com.sentrysoftware.hardware.cli;

import org.apache.logging.log4j.ThreadContext;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.util.StringUtils;

import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

@SpringBootApplication
public class HardwareSentryCLIApplication {

	public static void main(String[] args) {

		final List<String> disabledCommands = new ArrayList<>();

		List<String> argList = Arrays.asList(args);
		if (!argList.contains("--help") && !argList.contains("-h")) {
			disabledCommands.add("--spring.main.banner-mode=off");
		}

		// Default values for targetId and debugMode and outputDirectory
		ThreadContext.put("targetId", "no-target-yet");
		ThreadContext.put("debugMode", "false");

		// by default, the logs go in a directory "hardware-logs" in the temporary folder
		ThreadContext.put("outputDirectory",
			Paths.get(System.getProperty("java.io.tmpdir"), "hardware-logs").toString());

		final String[] fullArgs = StringUtils.concatenateStringArrays(args, disabledCommands.toArray(String[]::new));

		System.exit(SpringApplication.exit(SpringApplication.run(HardwareSentryCLIApplication.class, fullArgs)));
	}
}
