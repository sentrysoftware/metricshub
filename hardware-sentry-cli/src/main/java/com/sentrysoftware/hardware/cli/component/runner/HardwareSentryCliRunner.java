package com.sentrysoftware.hardware.cli.component.runner;

import java.util.Arrays;

import org.fusesource.jansi.AnsiConsole;
import org.springframework.boot.CommandLineRunner;
import org.springframework.boot.ExitCodeGenerator;
import org.springframework.stereotype.Component;

import com.sentrysoftware.hardware.cli.component.cli.HardwareSentryCli;

import picocli.CommandLine;
import picocli.CommandLine.IFactory;

@Component
public class HardwareSentryCliRunner implements CommandLineRunner, ExitCodeGenerator {

	private final HardwareSentryCli monitorHardwareCommand;

	private final IFactory factory; // auto-configured to inject PicocliSpringFactory

	private int exitCode;

	public HardwareSentryCliRunner(HardwareSentryCli monitorHardwareCommand, IFactory factory) {
		this.monitorHardwareCommand = monitorHardwareCommand;
		this.factory = factory;
	}

	@Override
	public void run(String... args) throws Exception {

		final String[] filteredArgs = Arrays.stream(args)
				.filter(arg -> !arg.startsWith("--spring"))
				.toArray(String[]::new);

		// Enable colors on Windows terminal
		AnsiConsole.systemInstall();

		// Run the picocli command
		exitCode = new CommandLine(monitorHardwareCommand, factory).execute(filteredArgs);

		// Cleanup Windows terminal settings
		AnsiConsole.systemUninstall();

	}

	@Override
	public int getExitCode() {
		return exitCode;
	}
}
