package com.sentrysoftware.metricshub.agent.process.io;

import java.io.PrintStream;

/**
 * Implementation of {@link StreamProcessor} that prints the received block to either
 * standard output or standard error.
 */
public class ConsoleStreamProcessor implements StreamProcessor {

	private PrintStream printStream;

	/**
	 * Constructs a {@code ConsoleStreamProcessor} instance.
	 *
	 * @param isError {@code true} if the output is an error, {@code false} for standard output.
	 */
	public ConsoleStreamProcessor(final boolean isError) {
		// Check if we should print to STDOUT or STDERR
		if (isError) {
			printStream = System.err; // NOSONAR
		} else {
			printStream = System.out; // NOSONAR
		}
	}

	@Override
	public void process(final String block) {
		// Print the block
		printStream.println(block);
		printStream.flush();
	}
}
