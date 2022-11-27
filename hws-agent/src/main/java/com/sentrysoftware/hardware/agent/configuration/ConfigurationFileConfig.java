package com.sentrysoftware.hardware.agent.configuration;

import java.io.File;
import java.io.IOException;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.DependsOn;
import org.springframework.core.Ordered;
import org.springframework.core.annotation.Order;

import com.sentrysoftware.hardware.agent.service.ProductInfoService;

/**
 * This configuration is annotated with a higher priority to make
 * hws-config.yaml available very soon in the application. For esthetic reasons
 * we make the beans created by this configuration depending on the
 * {@link ProductInfoService} so that we get the product information printed in
 * the beginning of the logs.
 */
@Configuration
@Order(Ordered.HIGHEST_PRECEDENCE)
@DependsOn("productInfoService")
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
