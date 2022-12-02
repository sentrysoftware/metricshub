package com.sentrysoftware.hardware.agent;

import static com.sentrysoftware.matrix.common.helpers.HardwareConstants.EMPTY;

import java.io.File;
import java.io.IOException;
import java.util.List;

import org.apache.logging.log4j.Level;
import org.apache.logging.log4j.ThreadContext;
import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.DefaultApplicationArguments;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

import com.sentrysoftware.hardware.agent.configuration.ConfigHelper;

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

			// SSL management
			final List<String> sslOptions = applicationArguments.getOptionValues(SSL_ENABLED);
			if (sslOptions != null && sslOptions.contains("true")) {
				application.setAdditionalProfiles("ssl");
			}

			// Start the application
			application.run(args);

		} catch (Exception e) {
			configureGlobalErrorLogger();
			log.error("Failed to start Hardware Sentry Agent application.", e);
			throw e;
		}

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
