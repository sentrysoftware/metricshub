package com.sentrysoftware.hardware.cli;

import org.fusesource.jansi.AnsiConsole;

import com.sentrysoftware.hardware.cli.component.cli.HardwareSentryCli;
import com.sentrysoftware.hardware.cli.component.cli.printer.PrintExceptionMessageHandler;

import picocli.CommandLine;

public class HardwareSentryCliApplication {

	public static void main(String[] args) {

		System.setProperty("log4j2.configurationFile", "log4j2-cli.xml");

		// Enable colors on Windows terminal
		AnsiConsole.systemInstall();

		final CommandLine cli = new CommandLine(new HardwareSentryCli());

		// Keep the below line commented for future reference
		// Using JAnsi on Windows breaks the output of Unicode (UTF-8) chars
		// It can be fixed using the below line... when running in Windows Terminal
		// and not CMD.EXE.
		// As this is poorly documented, we keep this for future improvement.
		//cli.setOut(new PrintWriter(AnsiConsole.out(), true, StandardCharsets.UTF_8));

		// Set the exception handler
		cli.setExecutionExceptionHandler(new PrintExceptionMessageHandler());

		// Execute the command
		int exitCode = cli.execute(args);

		// Cleanup Windows terminal settings
		AnsiConsole.systemUninstall();

		System.exit(exitCode);

	}
}
