package org.sentrysoftware.metricshub.agent.process.config;

import lombok.Builder;
import lombok.Data;
import org.sentrysoftware.metricshub.agent.process.io.ProcessorHelper;
import org.sentrysoftware.metricshub.agent.process.io.StreamProcessor;
import org.sentrysoftware.metricshub.agent.process.runtime.AbstractProcess;
import org.slf4j.Logger;

/**
 * Defines how to process the STDOUT and STDERR.<br> The
 * {@link StreamProcessor} of the STDOUT is mandatory, if you don't define the
 * {@link StreamProcessor} of the STDERR, a redirection will take place and
 * the standard error and standard output will be merged. You can change this behavior by overriding
 * the {@link AbstractProcess#onBeforeProcessStart(ProcessBuilder)}.
 */
@Data
@Builder
public class ProcessOutput {

	final StreamProcessor outputProcessor;
	final StreamProcessor errorProcessor;

	/**
	 * Builds a {@link ProcessOutput} which prints the process standard output only
	 *
	 * @param label The keyword used in the beginning of each printed line
	 * @return a new {@link ProcessOutput}
	 */
	public static ProcessOutput namedConsoleOutput(final String label) {
		return builder().outputProcessor(ProcessorHelper.namedConsole(String.format("[%s output]", label), false)).build();
	}

	/**
	 * Builds a {@link ProcessOutput} which prints the process standard output
	 * and standard error to the console.
	 *
	 * @param label The keyword used in the beginning of each printed line
	 * @return a new {@link ProcessOutput}
	 */
	public static ProcessOutput namedConsole(final String label) {
		return builder()
			.outputProcessor(ProcessorHelper.namedConsole(String.format("[%s output]", label), false))
			.errorProcessor(ProcessorHelper.namedConsole(String.format("[%s error]", label), true))
			.build();
	}

	/**
	 * Builds a {@link ProcessOutput} which ignores the process standard output
	 * and standard error.
	 *
	 * @return a new {@link ProcessOutput}
	 */
	public static ProcessOutput silent() {
		return builder().build();
	}

	/**
	 * Builds a {@link ProcessOutput} which prints the process standard output and
	 * standard error using the Slf4j {@link Logger}.
	 *
	 * @param logger Slf4j logger interface
	 * @return a new {@link ProcessOutput}
	 */
	public static ProcessOutput log(final Logger logger) {
		return builder()
			.outputProcessor(ProcessorHelper.logger(logger, Slf4jLevel.DEBUG))
			.errorProcessor(ProcessorHelper.logger(logger, Slf4jLevel.ERROR))
			.build();
	}

	/**
	 * Builds a {@link ProcessOutput} which prints the process standard output only
	 * using the Slf4j {@link Logger}.
	 *
	 * @param logger Slf4j logger interface
	 * @return a new {@link ProcessOutput}
	 */
	public static ProcessOutput logOutput(final Logger logger) {
		return builder().outputProcessor(ProcessorHelper.logger(logger, Slf4jLevel.DEBUG)).build();
	}
}
