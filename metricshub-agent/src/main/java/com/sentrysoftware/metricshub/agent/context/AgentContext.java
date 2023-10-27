package com.sentrysoftware.metricshub.agent.context;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.sentrysoftware.metricshub.agent.config.AgentConfig;
import com.sentrysoftware.metricshub.agent.context.ApplicationProperties.Project;
import com.sentrysoftware.metricshub.agent.deserialization.PostConfigDeserializer;
import com.sentrysoftware.metricshub.agent.helper.ConfigHelper;
import com.sentrysoftware.metricshub.agent.helper.OtelConfigHelper;
import com.sentrysoftware.metricshub.agent.helper.PostConfigDeserializeHelper;
import com.sentrysoftware.metricshub.agent.service.OtelCollectorProcessService;
import com.sentrysoftware.metricshub.agent.service.TaskSchedulingService;
import com.sentrysoftware.metricshub.engine.common.helpers.JsonHelper;
import com.sentrysoftware.metricshub.engine.common.helpers.MetricsHubConstants;
import com.sentrysoftware.metricshub.engine.connector.model.ConnectorStore;
import com.sentrysoftware.metricshub.engine.telemetry.TelemetryManager;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.lang.management.ManagementFactory;
import java.time.Duration;
import java.util.HashMap;
import java.util.Map;
import lombok.Data;
import lombok.extern.slf4j.Slf4j;
import org.springframework.core.io.ClassPathResource;

@Data
@Slf4j
public class AgentContext {

	public static final ObjectMapper AGENT_CONFIG_OBJECT_MAPPER = newAgentConfigObjectMapper();

	private AgentInfo agentInfo;
	private File configFile;
	private AgentConfig agentConfig;
	private ConnectorStore connectorStore;
	private String pid;
	private Map<String, String> otelSdkConfiguration;
	private Map<String, Map<String, TelemetryManager>> telemetryManagers;
	private OtelCollectorProcessService otelCollectorProcessService;
	private TaskSchedulingService taskSchedulingService;
	private MetricDefinitions hostMetricDefinitions;

	/**
	 * Instantiate the global context
	 *
	 * @param alternateConfigFile Alternation configuration file passed by the user.
	 * @throws IOException
	 */
	public AgentContext(final String alternateConfigFile) throws IOException {
		final long startTime = System.nanoTime();

		// Set the current PID
		pid = findPid();

		// Parse the connector store
		connectorStore = new ConnectorStore(ConfigHelper.getSubDirectory("connectors", false));

		// Initialize agent information
		agentInfo = new AgentInfo();

		// Find the configuration file
		configFile = ConfigHelper.findConfigFile(alternateConfigFile);

		// Read the agent configuration file (Default: metricshub.yaml)
		agentConfig =
			JsonHelper.deserialize(AGENT_CONFIG_OBJECT_MAPPER, new FileInputStream(configFile), AgentConfig.class);

		// Configure the global logger
		ConfigHelper.configureGlobalLogger(agentConfig.getLoggerLevel(), agentConfig.getOutputDirectory());

		log.info("Starting MetricsHub Agent...");

		logProductInformation();

		// Normalizes the agent configuration, configurations from parent will be set in children configuration
		// to ease data retrieval in the scheduler
		ConfigHelper.normalizeAgentConfiguration(agentConfig, connectorStore);

		telemetryManagers = ConfigHelper.buildTelemetryManagers(agentConfig, connectorStore);

		// Build OpenTelemetry SDK configuration
		otelSdkConfiguration = OtelConfigHelper.buildOtelSdkConfiguration(agentConfig);

		// Build the OpenTelemetry Collector Service
		otelCollectorProcessService = new OtelCollectorProcessService(agentConfig);

		// Build the Host Metric Definitions
		hostMetricDefinitions = readHostMetricDefinitions();

		// Build the TaskScheduling Service
		taskSchedulingService =
			TaskSchedulingService
				.builder()
				.withAgentConfig(agentConfig)
				.withAgentInfo(agentInfo)
				.withConfigFile(configFile)
				.withConnectorStore(connectorStore)
				.withOtelCollectorProcessService(otelCollectorProcessService)
				.withTaskScheduler(TaskSchedulingService.newScheduler(agentConfig.getJobPoolSize()))
				.withTelemetryManagers(telemetryManagers)
				.withSchedules(new HashMap<>())
				.withOtelSdkConfiguration(otelSdkConfiguration)
				.withHostMetricDefinitions(hostMetricDefinitions)
				.build();

		final Duration startupDuration = Duration.ofNanos(System.nanoTime() - startTime);

		log.info("Started MetricsHub Agent in {} seconds.", startupDuration.toMillis() / 1000.0);
	}

	/**
	 * Read {@link MetricDefinitions} for the root monitor instance (Endpoint)
	 * which is automatically created by the MetricsHub engine
	 *
	 * @return new {@link MetricDefinitions} instance
	 * @throws IOException
	 */
	public static MetricDefinitions readHostMetricDefinitions() throws IOException {
		return JsonHelper.deserialize(
			ConfigHelper.newObjectMapper(),
			new ClassPathResource("metricshub-host-metrics.yaml").getInputStream(),
			MetricDefinitions.class
		);
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
	public void logProductInformation() {
		if (isLogInfoEnabled()) {
			// Log product information
			final ApplicationProperties applicationProperties = agentInfo.getApplicationProperties();

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
				pid
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
			return MetricsHubConstants.EMPTY;
		}
	}
}