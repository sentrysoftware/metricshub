package com.sentrysoftware.metricshub.cli;

import com.sentrysoftware.metricshub.cli.service.MetricsHubCliService;
import com.sentrysoftware.metricshub.cli.service.PrintExceptionMessageHandlerService;
import java.util.Locale;
import org.fusesource.jansi.AnsiConsole;
import picocli.CommandLine;

/**
 * MetricsHub CLI application entry point. This class contains the main method
 * {@link #main(String[]) main} to launch the MetricsHub CLI.
 */
public class MetricsHubCliApplication {
	static {
		Locale.setDefault(Locale.US);
	}

	/**
	 * Entry point for the MetricsHub CLI application. Initializes necessary configurations,
	 * processes command line arguments, and executes the CLI.
	 *
	 * @param args The command line arguments passed to the application.
	 */
	public static void main(String[] args) {
		System.setProperty("log4j2.configurationFile", "log4j2-cli.xml");

		// Enable colors on Windows terminal
		AnsiConsole.systemInstall();

		final CommandLine cli = new CommandLine(new MetricsHubCliService());

		// Keep the below line commented for future reference
		// Using JAnsi on Windows breaks the output of Unicode (UTF-8) chars
		// It can be fixed using the below line... when running in Windows Terminal
		// and not CMD.EXE.
		// As this is poorly documented, we keep this for future improvement.
		// cli.setOut(new PrintWriter(AnsiConsole.out(), true, StandardCharsets.UTF_8)); // NOSONAR on commented code

		// Set the exception handler
		cli.setExecutionExceptionHandler(new PrintExceptionMessageHandlerService());

		// Allow case insensitive enum values
		cli.setCaseInsensitiveEnumValuesAllowed(true);

		// Execute the command
		final int exitCode = cli.execute(args);

		// Cleanup Windows terminal settings
		AnsiConsole.systemUninstall();

		System.exit(exitCode);
	}
}
