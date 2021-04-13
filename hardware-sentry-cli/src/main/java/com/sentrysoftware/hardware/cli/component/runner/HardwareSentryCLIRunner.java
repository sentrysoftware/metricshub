package com.sentrysoftware.hardware.cli.component.runner;

import java.util.Arrays;

import org.springframework.boot.CommandLineRunner;
import org.springframework.boot.ExitCodeGenerator;
import org.springframework.stereotype.Component;

import com.sentrysoftware.hardware.cli.component.cli.HardwareSentryCLI;

import picocli.CommandLine;
import picocli.CommandLine.IFactory;

@Component
public class HardwareSentryCLIRunner implements CommandLineRunner, ExitCodeGenerator {

	private static final String SPRING_CONFIG_PREFIX = "--spring";
	
	private final HardwareSentryCLI monitorHardwareCommand;

	private final IFactory factory; // auto-configured to inject PicocliSpringFactory

	private int exitCode;

	public HardwareSentryCLIRunner(HardwareSentryCLI monitorHardwareCommand, IFactory factory) {
		this.monitorHardwareCommand = monitorHardwareCommand;
		this.factory = factory;
	}

	@Override
	public void run(String... args) throws Exception {
		final String[] normalizedArgs = Arrays.stream(args).filter(arg -> !arg.startsWith(SPRING_CONFIG_PREFIX)).toArray(String[]::new);
		exitCode = new CommandLine(monitorHardwareCommand, factory).execute(normalizedArgs);
	}

	@Override
	public int getExitCode() {
		return exitCode;
	}
}
