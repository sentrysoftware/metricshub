package com.sentrysoftware.metricshub.agent.process.config;

import java.io.File;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import lombok.Builder;
import lombok.Builder.Default;
import lombok.Data;
import lombok.NonNull;

/**
 * Defines the configuration of a specific process.<br>
 * You need to configure the command line used to run the process.<br>
 * If you wish to process the STDOUT and STDERR, provide the
 * {@link ProcessOutput} configuration. A set of predefined configurations are
 * already defined by {@link ProcessOutput} class such as
 * {@link ProcessOutput#log(String)}, {@link ProcessOutput#silent()} and
 * {@link ProcessOutput#namedConsole(String)} but you can provide your own
 * implementation to process the output.<br> The process environment map can also be
 * defined, subsequently the started subprocesses will use this map as their
 * environment.
 */
@Data
@Builder
public class ProcessConfig {

	@NonNull
	private List<String> commandLine;

	private ProcessOutput output;

	@Default
	@NonNull
	private Map<String, String> environment = new HashMap<>();

	private File workingDir;

	/**
	 * Update the current ProcessConfig
	 *
	 * @param processConfig the new process configuration
	 */
	public void update(@NonNull final ProcessConfig processConfig) {
		setCommandLine(processConfig.getCommandLine());
		setEnvironment(processConfig.getEnvironment());
		setOutput(processConfig.getOutput());
		setWorkingDir(processConfig.getWorkingDir());
	}
}
