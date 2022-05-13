package com.sentrysoftware.hardware.agent.configuration;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardCopyOption;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class ConfigurationFileConfig {

	@Value("${config:}")
	private String configFilePath;

	/**
	 * Create the configuration file bean.
	 * <ol>
	 *   <li>If the user has configured the configFilePath via <em>--config=$filePath</em> then it is the chosen file</li>
	 *   <li>Else if <em>config/hws-config.yaml</em> path exists, the resulting File is the one representing this path</li>
	 *   <li>Else we copy <em>config/hws-config-example.yaml</em> to the target file <em>config/hws-config.yaml</em> then we return the resulting target file</li>
	 * </ol>
	 * 
	 * The program fails if
	 * <ul>
	 *   <li>The configured file path doesn't exist</li>
	 *   <li>config/hws-config-example.yaml is not present</li>
	 *   <li>If an I/O error occurs</li>
	 * </ul>
	 * 
	 * @return File instance
	 * 
	 * @throws IOException
	 */
	@Bean
	public File configFile() throws IOException {

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
