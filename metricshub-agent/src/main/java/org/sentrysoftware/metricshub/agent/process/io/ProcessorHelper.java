package org.sentrysoftware.metricshub.agent.process.io;

import java.io.Reader;
import java.util.Optional;
import java.util.function.BiFunction;
import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.NonNull;
import org.sentrysoftware.metricshub.agent.process.config.Slf4jLevel;
import org.slf4j.Logger;

/**
 * Use this helper class to create console, log and silent {@link StreamProcessor}
 */
@AllArgsConstructor(access = AccessLevel.PRIVATE)
public class ProcessorHelper {

	/**
	 * Create a new {@link ConsoleStreamProcessor} wrapped by the {@link NamedStreamProcessor}
	 *
	 * @param name    The name which each line starts with
	 * @param isError Whether the console processor handles output errors or not
	 * @return new {@link StreamProcessor}
	 */
	public static StreamProcessor namedConsole(final String name, final boolean isError) {
		return named(name, console(isError));
	}

	/**
	 * Create a new {@link NamedStreamProcessor} instance
	 *
	 * @param name        The name which each line starts with
	 * @param destination The next {@link StreamProcessor} to be called after adding the name to the beginning of the line
	 * @return new {@link StreamProcessor}
	 */
	public static StreamProcessor named(final String name, final StreamProcessor destination) {
		return new NamedStreamProcessor(name, destination);
	}

	/**
	 * Create a new {@link ConsoleStreamProcessor} which prints the block to the
	 * console
	 *
	 * @param isError Whether the console processor handles output errors or not
	 * @return new {@link StreamProcessor}
	 */
	public static StreamProcessor console(final boolean isError) {
		return new ConsoleStreamProcessor(isError);
	}

	/**
	 * Create a new {@link Slf4jStreamProcessor} which uses the given logger to log
	 * output messages
	 *
	 * @param logger Slf4j {@link Logger}
	 * @param level  Level used to log messages
	 * @return new {@link StreamProcessor}
	 */
	public static StreamProcessor logger(final Logger logger, final Slf4jLevel level) {
		return new Slf4jStreamProcessor(logger, level);
	}

	/**
	 * Create a new {@link Slf4jStreamProcessor} wrapped by the {@link NamedStreamProcessor}
	 *
	 * @param name    The name which each line starts with
	 * @param logger  Slf4j {@link Logger}
	 * @param level   Log level such as {@link Slf4jLevel#DEBUG}, {@link Slf4jLevel#INFO}, {@link Slf4jLevel#ERROR}, ...etc.
	 * @return new {@link StreamProcessor}
	 */
	public static StreamProcessor namedLogger(final String name, final Logger logger, final Slf4jLevel level) {
		return named(name, logger(logger, level));
	}

	/**
	 * Connect the given {@link Reader} to the {@link StreamProcessor} in order to
	 * start absorbing the process' output
	 *
	 * @param reader         Output reader
	 * @param processor      Output processor
	 * @param linkerFunction linker function creator witch builds runnable instance
	 *                       of {@link AbstractReaderProcessor}
	 * @return new {@link Thread} instance
	 */
	public static Optional<Thread> connect(
		final Reader reader,
		final StreamProcessor processor,
		@NonNull final BiFunction<Reader, StreamProcessor, AbstractReaderProcessor> linkerFunction
	) {
		if (reader == null || processor == null) {
			return Optional.empty();
		}

		final Thread thread = new Thread(linkerFunction.apply(reader, processor));

		thread.setDaemon(true);
		thread.start();

		return Optional.of(thread);
	}
}
