package com.sentrysoftware.hardware.cli;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import org.apache.logging.log4j.ThreadContext;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.util.StringUtils;

import com.sentrysoftware.hardware.cli.helpers.StringHelper;

@SpringBootApplication
public class HardwareSentryCLIApplication {

	public static void main(String[] args) {
		final List<String> disabledCommands = new ArrayList<>();

		List<String> argList = Arrays.asList(args);
		if (!argList.contains("--help") && !argList.contains("-h")) {
			disabledCommands.add("--spring.main.banner-mode=off");
		}

		// Find the hostname in the arguments to add it in the context.
		boolean isHostArg = false;
		boolean debugMode = false;
		for(String arg : argList) {
			if (isHostArg) {
				ThreadContext.put("targetId", arg);
				isHostArg = false;
			}

			if (arg.toLowerCase().contains(StringHelper.HOST_ARG)) {
				isHostArg = true;
			}

			if (arg.toLowerCase().equals(StringHelper.DEBUG_ARG) || arg.toLowerCase().equals(StringHelper.D_ARG)) {
				debugMode = true;
			}
		}

		ThreadContext.put("debugMode", String.valueOf(debugMode));

		final String[] fullArgs = StringUtils.concatenateStringArrays(args, disabledCommands.toArray(String[]::new));
		
		System.exit(SpringApplication.exit(SpringApplication.run(HardwareSentryCLIApplication.class, fullArgs)));
	}

}
