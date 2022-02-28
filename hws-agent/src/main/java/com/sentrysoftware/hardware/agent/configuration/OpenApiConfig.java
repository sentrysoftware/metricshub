package com.sentrysoftware.hardware.agent.configuration;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import org.springdoc.core.GroupedOpenApi;

/**
 * OpenAPI configuration so that the API doc is automatically generated
 * on the specified package.
 */
@Configuration
public class OpenApiConfig {

	@Bean
	public GroupedOpenApi api() {
		return GroupedOpenApi
				.builder()
				.group("hws-agent-api")
				.pathsToMatch("/**").build();
	}
}