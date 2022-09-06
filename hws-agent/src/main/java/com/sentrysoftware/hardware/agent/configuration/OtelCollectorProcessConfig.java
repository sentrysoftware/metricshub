package com.sentrysoftware.hardware.agent.configuration;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import com.sentrysoftware.hardware.agent.dto.MultiHostsConfigurationDto;
import com.sentrysoftware.hardware.agent.process.config.ProcessConfig;
import com.sentrysoftware.hardware.agent.service.opentelemetry.process.OtelCollectorProcess;

@Configuration
public class OtelCollectorProcessConfig {

	@Bean
	public ProcessConfig processConfig(final MultiHostsConfigurationDto multiHostsConfigurationDto) {
		// getOtelCollector() is configured to return the default configuration when 
		// the user doesn't override the 'otelCollector' section of the hws-config.yaml file.
		// Based on what has been configured, create the bean defining the information required to start the process
		// this bean is created when the application starts and remains alive during the application life cycle
		return multiHostsConfigurationDto
			.getOtelCollector()
			.toProcessConfig();

	}

	@Bean
	public OtelCollectorProcess otelCollectorProcess(final ProcessConfig processConfig) {
		// Create the bean which allows us to start and stop the OpenTelemetry Collector
		return new OtelCollectorProcess(processConfig);
	}
}
