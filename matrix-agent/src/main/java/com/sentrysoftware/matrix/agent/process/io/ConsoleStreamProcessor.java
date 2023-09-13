package com.sentrysoftware.matrix.agent.process.io;

import java.io.PrintStream;

/**
 * {@link StreamProcessor} implementation which prints the received block in the
 * standard out or standard error.
 */
public class ConsoleStreamProcessor implements StreamProcessor {

	private PrintStream printStream;

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
