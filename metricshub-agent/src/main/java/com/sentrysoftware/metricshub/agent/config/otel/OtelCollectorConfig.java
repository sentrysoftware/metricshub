package com.sentrysoftware.metricshub.agent.config.otel;

import static com.fasterxml.jackson.annotation.Nulls.SKIP;
import static com.sentrysoftware.metricshub.agent.config.otel.OtelCollectorOutput.LOG;
import static com.sentrysoftware.metricshub.agent.helper.AgentConstants.DEFAULT_OTEL_CONFIG_FILENAME;
import static com.sentrysoftware.metricshub.agent.helper.AgentConstants.OTEL_DIRECTORY_NAME;

import com.fasterxml.jackson.annotation.JsonSetter;
import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import com.sentrysoftware.metricshub.agent.deserialization.TimeDeserializer;
import com.sentrysoftware.metricshub.agent.helper.ConfigHelper;
import com.sentrysoftware.metricshub.agent.process.config.ProcessConfig;
import com.sentrysoftware.metricshub.engine.common.helpers.LocalOsHandler;
import com.sentrysoftware.metricshub.engine.common.helpers.MapHelper;
import java.io.File;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Builder.Default;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * Configuration class for the OpenTelemetry Collector.
 */
@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class OtelCollectorConfig {

	protected static final List<String> DEFAULT_COMMAND_LINE = buildDefaultCommandLine();
	protected static final String DEFAULT_WORKING_DIR = ConfigHelper.getSubPath(OTEL_DIRECTORY_NAME).toString();
	protected static final Map<String, String> DEFAULT_ENVIRONMENT;

	static {
		DEFAULT_ENVIRONMENT = new HashMap<>();
		if (LocalOsHandler.isWindows()) {
			// When the application starts as Windows Service, the process may fail with this error:
			// 'The service process could not connect to the service controller'.
			// In this case the NO_WINDOWS_SERVICE environment variable needs be set to `1` to force the collector
			// to be started as if it were running in an interactive terminal, without attempting to run as a Windows service.
			DEFAULT_ENVIRONMENT.put("NO_WINDOWS_SERVICE", "1");
		}
	}

	/**
	 * Default executable name for the OpenTelemetry Collector.
	 */
	public static final String EXECUTABLE_NAME = "otelcol-contrib";
	/**
	 * Default executable output ID for the OpenTelemetry Collector.
	 */
	public static final String EXECUTABLE_OUTPUT_ID = "otelcol";

	@Default
	@JsonSetter(nulls = SKIP)
	private List<String> commandLine = DEFAULT_COMMAND_LINE;

	@Default
	@JsonSetter(nulls = SKIP)
	private Map<String, String> environment = DEFAULT_ENVIRONMENT;

	@Default
	@JsonSetter(nulls = SKIP)
	private OtelCollectorOutput output = LOG;

	@Default
	@JsonSetter(nulls = SKIP)
	private String workingDir = DEFAULT_WORKING_DIR;

	@Default
	private boolean disabled = false;

	@Default
	@JsonDeserialize(using = TimeDeserializer.class)
	private long startupDelay = 10;

	/**
	 * Build the default command line of the OpenTelemetry Collector
	 *
	 * @return List of elements constructing the command line
	 */
	private static List<String> buildDefaultCommandLine() {
		final List<String> commandLine = new ArrayList<>();

		// Executable path
		commandLine.add(ConfigHelper.getSubPath(OTEL_DIRECTORY_NAME + "/" + EXECUTABLE_NAME).toString());

		// Configuration path
		commandLine.add("--config");

		// Get the default configuration file path located under the otel directory
		final String defaultConfigFilePath = ConfigHelper
			.getDefaultConfigFilePath(OTEL_DIRECTORY_NAME, DEFAULT_OTEL_CONFIG_FILENAME)
			.toString();

		commandLine.add(
			LocalOsHandler.isWindows() ? String.format("\"%s\"", defaultConfigFilePath) : defaultConfigFilePath
		);

		// Default feature gate is enabled to normalize Prometheus metrics
		commandLine.add("--feature-gates=pkg.translator.prometheus.NormalizeName");

		return commandLine;
	}

	@Override
	public boolean equals(Object obj) {
		if (this == obj) {
			return true;
		}
		if (obj == null) {
			return false;
		}
		if (getClass() != obj.getClass()) {
			return false;
		}
		OtelCollectorConfig other = (OtelCollectorConfig) obj;
		// CHECKSTYLE:OFF
		return (
			Objects.equals(commandLine, other.commandLine) &&
			disabled == other.disabled &&
			MapHelper.areEqual(environment, other.environment) &&
			output == other.output &&
			Objects.equals(workingDir, other.workingDir)
		);
		// CHECKSTYLE:ON
	}

	@Override
	public int hashCode() {
		return Objects.hash(commandLine, disabled, environment, output, workingDir);
	}

	/**
	 * Build a new {@link ProcessConfig} from actual information
	 *
	 * @return new {@link ProcessConfig} instance
	 */
	public ProcessConfig toProcessConfig() {
		return ProcessConfig
			.builder()
			.commandLine(getCommandLine())
			.environment(getEnvironment())
			.output(getOutput().getProcessOutputSupplier().get())
			.workingDir(new File(getWorkingDir()))
			.build();
	}
}
