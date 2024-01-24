package org.sentrysoftware.metricshub.agent.context;

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

import com.fasterxml.jackson.databind.ObjectMapper;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.lang.management.ManagementFactory;
import java.time.Duration;
import java.util.HashMap;
import java.util.Map;
import lombok.Data;
import lombok.extern.slf4j.Slf4j;
import org.sentrysoftware.metricshub.agent.config.AgentConfig;
import org.sentrysoftware.metricshub.agent.context.ApplicationProperties.Project;
import org.sentrysoftware.metricshub.agent.deserialization.PostConfigDeserializer;
import org.sentrysoftware.metricshub.agent.helper.ConfigHelper;
import org.sentrysoftware.metricshub.agent.helper.OtelConfigHelper;
import org.sentrysoftware.metricshub.agent.helper.PostConfigDeserializeHelper;
import org.sentrysoftware.metricshub.agent.service.OtelCollectorProcessService;
import org.sentrysoftware.metricshub.agent.service.TaskSchedulingService;
import org.sentrysoftware.metricshub.engine.common.helpers.JsonHelper;
import org.sentrysoftware.metricshub.engine.common.helpers.MetricsHubConstants;
import org.sentrysoftware.metricshub.engine.connector.model.ConnectorStore;
import org.sentrysoftware.metricshub.engine.telemetry.TelemetryManager;

/**
 * AgentContext represents the context of the MetricsHub agent, containing various components such as agent information,
 * configuration, telemetry managers, OpenTelemetry SDK configuration, OtelCollector process service, task scheduling service,
 * and metric definitions. It also includes methods for building the context and logging product information.
 */
@Data
@Slf4j
public class AgentContext {

	/**
	 * ObjectMapper instance for deserializing agent configuration.
	 */
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
	 * @param alternateConfigFile Alternation configuration file passed by the user
	 * @throws IOException Signals that an I/O exception has occurred
	 */
	public AgentContext(final String alternateConfigFile) throws IOException {
		build(alternateConfigFile, true);
	}

	/**
	 * Builds the agent context
	 * @param alternateConfigFile Alternation configuration file passed by the user
	 * @param createConnectorStore Whether we should create a new connector store
	 * @throws IOException Signals that an I/O exception has occurred
	 */
	public void build(final String alternateConfigFile, final boolean createConnectorStore) throws IOException {
		final long startTime = System.nanoTime();

		// Set the current PID
		pid = findPid();

		if (createConnectorStore) {
			// Parse the connector store
			connectorStore = new ConnectorStore(ConfigHelper.getSubDirectory("connectors", false));
		}

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
		ConfigHelper.normalizeAgentConfiguration(agentConfig);

		telemetryManagers = ConfigHelper.buildTelemetryManagers(agentConfig, connectorStore);

		// Build OpenTelemetry SDK configuration
		otelSdkConfiguration = OtelConfigHelper.buildOtelSdkConfiguration(agentConfig);

		// Build the OpenTelemetry Collector Service
		otelCollectorProcessService = new OtelCollectorProcessService(agentConfig);

		// Build the Host Metric Definitions
		hostMetricDefinitions = ConfigHelper.readHostMetricDefinitions();

		// Build the TaskScheduling Service
		taskSchedulingService =
			TaskSchedulingService
				.builder()
				.withAgentConfig(agentConfig)
				.withAgentInfo(agentInfo)
				.withConfigFile(configFile)
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
				"\nCommunity Connector Library version: {}" +
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
				applicationProperties.ccVersion(),
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
