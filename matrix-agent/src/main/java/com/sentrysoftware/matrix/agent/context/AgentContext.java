package com.sentrysoftware.matrix.agent.context;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.sentrysoftware.matrix.agent.config.AgentConfig;
import com.sentrysoftware.matrix.agent.context.ApplicationProperties.Project;
import com.sentrysoftware.matrix.agent.deserialization.PostConfigDeserializer;
import com.sentrysoftware.matrix.agent.helper.ConfigHelper;
import com.sentrysoftware.matrix.agent.helper.PostConfigDeserializeHelper;
import com.sentrysoftware.matrix.common.helpers.JsonHelper;
import com.sentrysoftware.matrix.common.helpers.MatrixConstants;
import com.sentrysoftware.matrix.connector.model.ConnectorStore;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.lang.management.ManagementFactory;
import java.time.Duration;
import lombok.Data;
import lombok.Getter;
import lombok.extern.slf4j.Slf4j;

@Data
@Slf4j
public class AgentContext {

	public static final ObjectMapper AGENT_CONFIG_OBJECT_MAPPER = newAgentConfigObjectMapper();

	@Getter
	private static AgentContext instance = new AgentContext();

	private AgentInfo agentInfo;
	private File configFile;
	private AgentConfig agentConfig;
	private ConnectorStore connectorStore;
	private String pid;

	/**
	 * Read the agent configuration file
	 *
	 * @param alternateConfigFile Alternation configuration file passed by the user.
	 * @throws IOException
	 */
	public static void initialize(final String alternateConfigFile) throws IOException {
		long startTime = System.nanoTime();

		// Set the current PID
		instance.pid = findPid();

		// Parse the connector store
		instance.connectorStore = new ConnectorStore(ConfigHelper.getSubDirectory("connectors", false));

		// Initialize agent information
		instance.agentInfo = new AgentInfo();

		// Find the configuration file
		instance.configFile = ConfigHelper.findConfigFile(alternateConfigFile);

		// Read the agent configuration file (Default: metricshub.yaml)
		instance.agentConfig =
			JsonHelper.deserialize(AGENT_CONFIG_OBJECT_MAPPER, new FileInputStream(instance.configFile), AgentConfig.class);

		// Configure the global logger
		ConfigHelper.configureGlobalLogger(
			instance.agentConfig.getLoggerLevel(),
			instance.agentConfig.getOutputDirectory()
		);

		log.info("Starting MetricsHub Agent...");

		logProductInformation();

		// Normalizes the agent configuration, configurations from parent will be set in children configuration
		// to ease data retrieval in the scheduler
		ConfigHelper.normalizeAgentConfiguration(instance.agentConfig);

		final Duration startupDuration = Duration.ofNanos(System.nanoTime() - startTime);

		log.info("Started MetricsHub Agent in {} seconds.", startupDuration.toMillis() / 1000.0);
	}

	/**
	 * Create a new {@link ObjectMapper} instance then add to it the
	 * {@link PostConfigDeserializer}
	 *
	 * @return new {@link ObjectMapper} instance
	 */
	private static ObjectMapper newAgentConfigObjectMapper() {
		final ObjectMapper objectMapper = ConfigHelper.newObjectMapper();

		PostConfigDeserializeHelper.addPostDeserializeSupport(objectMapper);

		return objectMapper;
	}

	/**
	 * Log information about MetricsHub application
	 */
	public static void logProductInformation() {
		if (isLogInfoEnabled()) {
			// Log product information
			final ApplicationProperties applicationProperties = instance.agentInfo.getApplicationProperties();

			final Project project = applicationProperties.project();

			log.info(
				"Product information:" + // NOSONAR
				"\nName: {}" +
				"\nVersion: {}" +
				"\nBuild number: {}" +
				"\nBuild date: {}" +
				"\nConnector Library version: {}" +
				"\nOpenTelemetry Collector Contrib version: {}" +
				"\nJava version: {}" +
				"\nJava Runtime Environment directory: {}" +
				"\nOperating System: {} {}" +
				"\nUser working directory: {}" +
				"\nPID: {}",
				project.name(),
				project.version(),
				applicationProperties.buildNumber(),
				applicationProperties.buildDate(),
				applicationProperties.hcVersion(),
				applicationProperties.otelVersion(),
				System.getProperty("java.version"),
				System.getProperty("java.home"),
				System.getProperty("os.name"),
				System.getProperty("os.arch"),
				System.getProperty("user.dir"),
				instance.pid
			);
		}
	}

	/**
	 * Whether the log info is enabled or not
	 *
	 * @return boolean value
	 */
	static boolean isLogInfoEnabled() {
		return log.isInfoEnabled();
	}

	/**
	 * Get the application PID.
	 *
	 * @return PID as {@link String} value
	 */
	private static String findPid() {
		try {
			final String jvmName = ManagementFactory.getRuntimeMXBean().getName();
			return jvmName.split("@")[0];
		} catch (Throwable ex) { // NOSONAR
			return MatrixConstants.EMPTY;
		}
	}
}
