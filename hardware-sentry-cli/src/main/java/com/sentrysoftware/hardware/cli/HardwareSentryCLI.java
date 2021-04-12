package com.sentrysoftware.hardware.cli;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.util.StringUtils;

@SpringBootApplication
public class HardwareSentryCLI {

	public static void main(String[] args) {

		final List<String> disabledCommands = new ArrayList<>();

		if (Arrays.asList(args).contains("monitor-hardware")) {
			disabledCommands.add("--spring.main.banner-mode=off");
		}

		final String[] fullArgs = StringUtils.concatenateStringArrays(args, disabledCommands.toArray(String[]::new));

		SpringApplication.run(HardwareSentryCLI.class, fullArgs);
	}

}
