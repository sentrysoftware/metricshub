package com.sentrysoftware.hardware.agent;

import static com.sentrysoftware.matrix.common.helpers.HardwareConstants.EMPTY;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.util.List;

import org.apache.logging.log4j.Level;
import org.apache.logging.log4j.ThreadContext;
import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.DefaultApplicationArguments;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.core.io.ClassPathResource;

import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.MapperFeature;
import com.fasterxml.jackson.databind.json.JsonMapper;
import com.fasterxml.jackson.dataformat.yaml.YAMLFactory;
import com.sentrysoftware.hardware.agent.configuration.ConfigHelper;
import com.sentrysoftware.hardware.agent.dto.application.ApplicationConfigDto;

import lombok.extern.slf4j.Slf4j;

@SpringBootApplication
@Slf4j
public class HardwareSentryAgentApp {

	private static final String SSL_ENABLED = "server.ssl.enabled";
	private static final String CONFIG = "config";

	public static void main(String[] args) throws Exception {

		try {

			// Initialize the application
			final SpringApplication application = new SpringApplication(HardwareSentryAgentApp.class);

			// Parse arguments
			final ApplicationArguments applicationArguments = new DefaultApplicationArguments(args);

			// Configure the global logger
			configureGlobalLogger(applicationArguments);

			// Default application.yml file
			String applicationConfigFileName = "application.yml";

			// SSL management
			final List<String> sslOptions = applicationArguments.getOptionValues(SSL_ENABLED);
			if (sslOptions != null && sslOptions.contains("true")) {
				application.setAdditionalProfiles("ssl");
				applicationConfigFileName = "application-ssl.yml";
			}

			// Log the product information
			logProductInformation(applicationConfigFileName);

			// Start the application
			application.run(args);

		} catch (Exception e) {
			configureGlobalErrorLogger();
			log.error("Failed to start Hardware Sentry Agent application.", e);
			throw e;
		}

	}

	/**
	 * Reads the internal application YAML configuration then logs the product information
	 * 
	 * @param applicationConfigFileName
	 * @throws IOException
	 */
	static void logProductInformation(String applicationConfigFileName)
			throws IOException {

		if (isLogInfoEnabled()) {

			final InputStream appConfig = new ClassPathResource(applicationConfigFileName).getInputStream();

			// Parse the internal application configuration file (application.yml)
			final ApplicationConfigDto applicationConfig = JsonMapper
				.builder(new YAMLFactory())
				.configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false)
				.configure(DeserializationFeature.FAIL_ON_INVALID_SUBTYPE, false)
				.enable(MapperFeature.ACCEPT_CASE_INSENSITIVE_ENUMS)
				.enable(MapperFeature.ACCEPT_CASE_INSENSITIVE_PROPERTIES)
				.build()
				.readValue(appConfig, ApplicationConfigDto.class);

			// Log product information

			log.info("Product name: {}", applicationConfig.getProject().getName());
			log.info("Product version: {}", applicationConfig.getProject().getVersion());
			log.info("Product build number: {}", applicationConfig.getBuildNumber());
			log.info("Product build date: {}", applicationConfig.getBuildDate());
			log.info("Hardware Connector Library version: {}", applicationConfig.getHcVersion());
			log.info("OpenTelemetry Collector Contrib version: {}", applicationConfig.getOtelVersion());
			log.info("Java version: {}", System.getProperty("java.version"));
			log.info("Java Runtime Environment directory: {}", System.getProperty("java.home"));
			log.info("Operating System: {} {}", System.getProperty("os.name"), System.getProperty("os.arch"));
			log.info("User working directory: {}", System.getProperty("user.dir"));

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
	 * Finds the configuration file then configures global logger
	 * 
	 * @param applicationArguments Provides access to the arguments that were used to run a the SpringApplication
	 * @throws IOException
	 */
	static void configureGlobalLogger(final ApplicationArguments applicationArguments) throws IOException {

		// Get the 'config' option
		final List<String> userConfigOptions = applicationArguments.getOptionValues(CONFIG);

		// Get the configured file path, the user must configure only one configuration path
		final String userConfigFilePath = userConfigOptions != null ? userConfigOptions.stream().findFirst().orElse(EMPTY) : EMPTY;

		// Find the configuration file
		final File configFile = ConfigHelper.findConfigFile(userConfigFilePath);

		// Configure global logger for all the application
		ConfigHelper.configureGlobalLogger(ConfigHelper.readConfigurationSafe(configFile));

	}

	/**
	 * Configure the global error logger to be able to log startup fatal errors
	 * preventing the application from starting
	 */
	static void configureGlobalErrorLogger() {

		ThreadContext.put("logId", "hws-agent-global-error");
		ThreadContext.put("loggerLevel", Level.ERROR.toString());
		ThreadContext.put("outputDirectory", ConfigHelper.DEFAULT_OUTPUT_DIRECTORY.toString());

	}
}
