package org.sentrysoftware.metricshub.agent.process.runtime;

/*-
 * ╱╲╱╲╱╲╱╲╱╲╱╲╱╲╱╲╱╲╱╲╱╲╱╲╱╲╱╲╱╲╱╲╱╲╱╲╱╲╱╲
 * MetricsHub Agent
 * ჻჻჻჻჻჻
 * Copyright 2023 - 2024 Sentry Software
 * ჻჻჻჻჻჻
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Affero General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU Affero General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 * ╲╱╲╱╲╱╲╱╲╱╲╱╲╱╲╱╲╱╲╱╲╱╲╱╲╱╲╱╲╱╲╱╲╱╲╱╲╱╲╱
 */

import java.io.Closeable;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.List;
import java.util.Map;
import lombok.Getter;
import lombok.NonNull;
import lombok.extern.slf4j.Slf4j;
import org.springframework.util.Assert;

/**
 * This class controls the process through the start, stop and addShutdownHook
 * methods
 */
@Slf4j
public class ProcessControl {

	@Getter
	private final Process process;

	@Getter
	private InputStreamReader reader;

	@Getter
	private InputStreamReader error;

	ProcessControl(final Process process) {
		this.process = process;
		final InputStream inputStream = process.getInputStream();
		if (inputStream != null) {
			this.reader = new InputStreamReader(inputStream);
		}
		final InputStream errorStream = process.getErrorStream();
		if (errorStream != null) {
			this.error = new InputStreamReader(errorStream);
		}
	}

	/**
	 * Create a new instance of the {@link ProcessBuilder} used to start the process
	 *
	 * @param commandLine         The process command line
	 * @param environment         The process environment map
	 * @param workingDir          The process working directory
	 * @param redirectErrorStream If set to true then any error output generated by subprocesses will be merged with the standard output
	 * @return {@link ProcessBuilder} instance
	 */
	public static ProcessBuilder newProcessBuilder(
		@NonNull final List<String> commandLine,
		@NonNull final Map<String, String> environment,
		final File workingDir,
		final boolean redirectErrorStream
	) {
		Assert.isTrue(!commandLine.isEmpty(), "Command line cannot be empty.");

		final ProcessBuilder processBuilder = new ProcessBuilder(commandLine);

		processBuilder.environment().putAll(environment);

		if (workingDir != null) {
			processBuilder.directory(workingDir);
		}

		// Sets the process builder's redirectErrorStream property.
		// This makes it easier to correlate error messages with the corresponding output.
		// The ProcessBuilder's initial value is false
		processBuilder.redirectErrorStream(redirectErrorStream);

		return processBuilder;
	}

	/**
	 * Starts a process and builds the {@link ProcessControl} instance.
	 *
	 * @param processBuilder The {@code ProcessBuilder} used to start the process.
	 * @return {@link ProcessControl} instance.
	 * @throws IOException If an I/O error occurs.
	 */
	public static ProcessControl start(final ProcessBuilder processBuilder) throws IOException {
		return new ProcessControl(processBuilder.start());
	}

	/**
	 * Close the InputStream, OutputStream and ErrorStream then destroy the process
	 */
	public void stop() {
		try {
			// Close streams
			close(process.getErrorStream());
			close(process.getInputStream());
			close(process.getOutputStream());

			// Destroy the process
			process.destroy();
		} catch (Exception e) {
			log.error("Error detected when terminating the process. Message {}.", e.getMessage());
			log.debug("Exception: ", e);
		}
		reader = null;
		error = null;
	}

	/**
	 * Close the closable and avoid any null pointer exception if the argument is <code>null</code>
	 *
	 * @param closeable
	 * @throws IOException
	 */
	private void close(Closeable closeable) throws IOException {
		if (closeable != null) {
			closeable.close();
		}
	}

	/**
	 * Registers a new virtual-machine shutdown hook.
	 *
	 * @param runnable The {@code Runnable} to be executed during the shutdown.
	 */
	public static void addShutdownHook(Runnable runnable) {
		Runtime.getRuntime().addShutdownHook(new Thread(runnable));
	}
}
