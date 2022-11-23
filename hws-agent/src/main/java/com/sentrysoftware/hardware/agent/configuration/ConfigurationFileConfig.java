package com.sentrysoftware.hardware.agent.configuration;

import static com.sentrysoftware.hardware.agent.configuration.AgentInfoConfig.AGENT_INFO_BUILD_DATE_ATTRIBUTE_KEY;
import static com.sentrysoftware.hardware.agent.configuration.AgentInfoConfig.AGENT_INFO_BUILD_NUMBER_ATTRIBUTE_KEY;
import static com.sentrysoftware.hardware.agent.configuration.AgentInfoConfig.AGENT_INFO_HC_VERSION_ATTRIBUTE_KEY;
import static com.sentrysoftware.hardware.agent.configuration.AgentInfoConfig.AGENT_INFO_NAME_ATTRIBUTE_KEY;
import static com.sentrysoftware.hardware.agent.configuration.AgentInfoConfig.AGENT_INFO_OTEL_VERSION_ATTRIBUTE_KEY;
import static com.sentrysoftware.hardware.agent.configuration.AgentInfoConfig.AGENT_INFO_VERSION_ATTRIBUTE_KEY;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardCopyOption;
import java.util.Map;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import com.sentrysoftware.hardware.agent.service.TaskSchedulingService;

import lombok.extern.slf4j.Slf4j;

@Configuration
@Slf4j
public class ConfigurationFileConfig {

	@Value("${config:}")
	private String configFilePath;

	@Value("${server.port:8080}")
	private int serverPort;

	/**
	 * Create the configuration file bean.
	 * 
	 * @return File instance
	 * 
	 * @throws IOException
	 */
	@Bean
	public File configFile(final Map<String, String> agentInfo) throws IOException {

		final File configFile = determineConfigFile();

		// Configure global logger
		TaskSchedulingService.configureGlobalLogger(ConfigHelper.readConfigurationSafe(configFile), serverPort);

		logProductInformation(agentInfo);

		return configFile;

	}

	/**
	 * Log product information.
	 * 
	 * @param agentInfo key-value pair attributes containing the agent information.
	 */
	private void logProductInformation(final Map<String, String> agentInfo) {

		if (log.isInfoEnabled()) {

			log.info("Product name: {}", agentInfo.get(AGENT_INFO_NAME_ATTRIBUTE_KEY));
			log.info("Product version: {}", agentInfo.get(AGENT_INFO_VERSION_ATTRIBUTE_KEY));
			log.info("Product build number: {}", agentInfo.get(AGENT_INFO_BUILD_NUMBER_ATTRIBUTE_KEY));
			log.info("Product build date: {}", agentInfo.get(AGENT_INFO_BUILD_DATE_ATTRIBUTE_KEY));
			log.info("Hardware Connector Library version: {}", agentInfo.get(AGENT_INFO_HC_VERSION_ATTRIBUTE_KEY));
			log.info("OpenTelemetry Collector Contrib version: {}", agentInfo.get(AGENT_INFO_OTEL_VERSION_ATTRIBUTE_KEY));
			log.info("Java version: {}", System.getProperty("java.version"));
			log.info("Operating System: {} {}", System.getProperty("os.name"), System.getProperty("os.arch"));
		}

	}

	/**
	 * Determine the application's configuration file (hws-config.yaml).<br>
	 * <ol>
	 *   <li>If the user has configured the configFilePath via <em>--config=$filePath</em> then it is the chosen file</li>
	 *   <li>Else if <em>config/hws-config.yaml</em> path exists, the resulting File is the one representing this path</li>
	 *   <li>Else we copy <em>config/hws-config-example.yaml</em> to the host file <em>config/hws-config.yaml</em> then we return the resulting host file</li>
	 * </ol>
	 * 
	 * The program fails if
	 * <ul>
	 *   <li>The configured file path doesn't exist</li>
	 *   <li>config/hws-config-example.yaml is not present</li>
	 *   <li>If an I/O error occurs</li>
	 * </ul>
	 * 
	 * @return {@link File} instance
	 * @throws IOException
	 */
	private File determineConfigFile() throws IOException {
		// The user has configured a configuration file path
		if (!configFilePath.isBlank()) {
			final File configFile = new File(configFilePath);
			if (configFile.exists()) {
				return configFile;
			}
			throw new IllegalStateException("Cannot find " + configFilePath
					+ ". Please make sure the file exists on your system");
		}

		// Get the configuration file path from ../config/hws-config.yaml
		final Path configPath = ConfigHelper.getSubPath("config/hws-config.yaml");

		// If it exists then we are good we can just return the resulting File
		if (Files.exists(configPath)) {
			return configPath.toFile();
		}

		// Now we will proceed with a copy of hws-config-example.yaml to config/hws-config.yaml
		final Path exampleConfigPath = ConfigHelper.getSubPath("config/hws-config-example.yaml");

		// Bad configuration
		if (!Files.exists(exampleConfigPath)) {
			throw new IllegalStateException("Cannot find hws-config-example.yaml. Please create the configuration file "
					+ configPath.toAbsolutePath() + " before starting the Hardware Sentry Agent");
		}

		return Files.copy(exampleConfigPath, configPath, StandardCopyOption.REPLACE_EXISTING).toFile();

	}

}
