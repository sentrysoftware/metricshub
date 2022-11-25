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
	 * @param agentInfo key-value pair attributes containing the agent information.
	 * @return File instance
	 * 
	 * @throws IOException
	 */
	@Bean
	public File configFile(@Value("${config:}") final String configFilePath) throws IOException {

		return ConfigHelper.findConfigFile(configFilePath);

	}

}
