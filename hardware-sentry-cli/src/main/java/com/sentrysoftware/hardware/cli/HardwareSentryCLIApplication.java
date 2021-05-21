package com.sentrysoftware.hardware.cli;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import org.apache.logging.log4j.ThreadContext;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.util.StringUtils;

@SpringBootApplication
public class HardwareSentryCLIApplication {

	public static void main(String[] args) {
		final List<String> disabledCommands = new ArrayList<>();

		List<String> argList = Arrays.asList(args);
		if (!argList.contains("--help") && !argList.contains("-h")) {
			disabledCommands.add("--spring.main.banner-mode=off");
		}

		// Default values for targetId and debugMode
		ThreadContext.put("targetId", "no-target-yet");
		ThreadContext.put("debugMode", "false");

		final String[] fullArgs = StringUtils.concatenateStringArrays(args, disabledCommands.toArray(String[]::new));

		System.exit(SpringApplication.exit(SpringApplication.run(HardwareSentryCLIApplication.class, fullArgs)));
	}

}
