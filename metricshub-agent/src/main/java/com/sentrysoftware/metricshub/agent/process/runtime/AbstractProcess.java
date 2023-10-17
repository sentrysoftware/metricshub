package com.sentrysoftware.metricshub.agent.process.runtime;

import com.sentrysoftware.metricshub.agent.process.config.ProcessConfig;
import com.sentrysoftware.metricshub.agent.process.config.ProcessOutput;
import java.io.IOException;
import java.io.Reader;
import lombok.Getter;
import lombok.extern.slf4j.Slf4j;

/**
 * Extends this abstract class to create your own process implementation.<br>
 * Create a new instance using the {@link ProcessConfig} then invoke the <code>start()</code> method to start the process.
 * You can override the default behavior of this abstract class by implementing the following methods:
 * <ol>
 *   <li><code>onBeforeProcess()</code>: override this method to execute additional operations before the <code>start</code> procedure begins its executions.</li>
 *   <li><code>onBeforeProcessStart()</code>: override this method to update the process builder or to add additional actions before starting the process.</li>
 *   <li><code>onAfterProcessStart()</code>: override this method to execute additional operations after the process is started.</li>
 *   <li><code>onBeforeProcessStop()</code>: override this method to execute additional operations before terminating the process.</li>
 *   <li><code>onAfterProcessStop()</code>: override this method to execute additional operations after terminating the process.</li>
 *   <li><code>stopInternal()</code>: You MUST implement this method in order to provide your own stop strategy.
 *   					 You can call the <code>stopProcess()</code> method which destroys the process.
 *   					 But, for instance, you might want to terminate your process with a SIGTERM or SIGKILL.</li>
 * </ol>
 */
@Slf4j
public abstract class AbstractProcess implements IStoppable {

	@Getter
	protected final ProcessConfig processConfig;

	@Getter
	protected ProcessControl processControl;

	@Getter
	protected boolean stopped;

	protected AbstractProcess(final ProcessConfig processConfig) {
		this.processConfig = processConfig;
	}

	/**
	 * Starts the process
	 *
	 * @throws IOException
	 */
	public void start() throws IOException {
		try {
			onBeforeProcess();

			// Get the process output to check if we should redirect STDERR
			final ProcessOutput output = processConfig.getOutput();

			// Create a new process builder
			final ProcessBuilder processBuilder = ProcessControl.newProcessBuilder(
				processConfig.getCommandLine(),
				processConfig.getEnvironment(),
				processConfig.getWorkingDir(),
				output != null && output.getErrorProcessor() == null
			);

			onBeforeProcessStart(processBuilder);

			// Start the process
			processControl = ProcessControl.start(processBuilder);

			// Register a new virtual-machine shutdown hook to stop the process
			ProcessControl.addShutdownHook(this::stop);

			onAfterProcessStart();

			stopped = false;

			log.info("Started process with command line: {}", processConfig.getCommandLine());
		} catch (Exception e) {
			log.error("Cannot start the process due to an exception: {}", e.getMessage());
			log.debug("Exception: ", e);
			stop();
			throw e;
		}
	}

	@Override
	public void stop() {
		if (!stopped) {
			onBeforeProcessStop();

			stopInternal();

			stopped = true;

			onAfterProcessStop();

			log.info("Stopped process previously launched with command line: {}", processConfig.getCommandLine());
		}
	}

	/**
	 * Stop the process
	 */
	protected final void stopProcess() {
		if (processControl != null) {
			processControl.stop();
		}
	}

	/**
	 * Executes additional operations before starting the process
	 */
	protected abstract void onBeforeProcess();

	/**
	 * Updates the process builder before starting the process
	 *
	 * @param processBuilder This object is used to create operating system processes.
	 */
	protected abstract void onBeforeProcessStart(ProcessBuilder processBuilder);

	/**
	 * Executes additional operations after the process is started
	 */
	protected abstract void onAfterProcessStart();

	/**
	 * Executes operations before stopping the process
	 */
	protected abstract void onBeforeProcessStop();

	/**
	 * Execute operations after the process is stopped
	 */
	protected abstract void onAfterProcessStop();

	/**
	 * Stop the process
	 */
	protected abstract void stopInternal();

	/**
	 * Get the STDOUT reader
	 *
	 * @return {@link Reader} instance
	 */
	protected Reader getReader() {
		return processControl.getReader();
	}

	/**
	 * Get the STDERR reader
	 *
	 * @return {@link Reader} instance
	 */
	protected Reader getError() {
		return processControl.getError();
	}
}
