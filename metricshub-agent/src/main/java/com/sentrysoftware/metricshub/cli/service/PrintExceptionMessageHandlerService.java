package com.sentrysoftware.metricshub.cli.service;

import picocli.CommandLine;
import picocli.CommandLine.IExecutionExceptionHandler;
import picocli.CommandLine.ParseResult;

/**
 * Class in charge of output a nice-to-read error message in case of an exception
 * occurs during the CLI execution (and not related to invalid arguments)
 */
public class PrintExceptionMessageHandlerService implements IExecutionExceptionHandler {

	private static final int MAXIMUM_CAUSE_DEPTH = 10;

	@Override
	public int handleExecutionException(Exception exception, CommandLine commandLine, ParseResult parseResult) {
		int depth = 0;
		Throwable cause = exception;

		// We will loop through a maximum of 10 causes
		while (depth < MAXIMUM_CAUSE_DEPTH && cause != null) {
			String message = " ".repeat(depth * 2) + cause.getClass().getSimpleName();
			if (cause.getMessage() != null) {
				message = message + ": " + cause.getMessage();
			}
			commandLine.getErr().println(commandLine.getColorScheme().errorText(message));

			// Do we have a cause?
			cause = cause.getCause();
			depth++;
		}

		return CommandLine.ExitCode.SOFTWARE;
	}
}
