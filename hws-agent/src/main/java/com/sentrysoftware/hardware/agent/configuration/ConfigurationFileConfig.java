package com.sentrysoftware.hardware.agent.configuration;

import java.io.File;
import java.io.IOException;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class ConfigurationFileConfig {


	/**
	 * Create the configuration file bean.
	 * 
	 * @param configFilePath The path of the configuration file (hws-config.yaml) 
	 * @return File instance
	 * 
	 * @throws IOException
	 */
	@Bean
	public File configFile(@Value("${config:}") final String configFilePath) throws IOException {

		return ConfigHelper.findConfigFile(configFilePath);

	}

}
