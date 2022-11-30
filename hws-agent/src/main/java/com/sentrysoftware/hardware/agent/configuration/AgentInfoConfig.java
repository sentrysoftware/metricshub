package com.sentrysoftware.hardware.agent.configuration;

import java.util.Map;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import com.sentrysoftware.hardware.agent.mapping.opentelemetry.MappingConstants;

@Configuration
public class AgentInfoConfig {

	public static final String AGENT_INFO_NAME_ATTRIBUTE_KEY = MappingConstants.NAME;
	public static final String AGENT_INFO_VERSION_ATTRIBUTE_KEY = "version";
	public static final String AGENT_INFO_BUILD_DATE_ATTRIBUTE_KEY = "build_date";
	public static final String AGENT_INFO_BUILD_NUMBER_ATTRIBUTE_KEY = "build_number";
	public static final String AGENT_INFO_HC_VERSION_ATTRIBUTE_KEY = "hc_version";
	public static final String AGENT_INFO_OTEL_VERSION_ATTRIBUTE_KEY = "otel_version";

	// These properties come from src/main/resources/application.yml or application-ssl.yml
	// which themselves are "filtered" by Maven's Resources Plugin to expose
	// pom.xml's values
	@Value("${project.name}")
	private String projectName;

	@Value("${project.version}")
	private String projectVersion;

	@Value("${buildNumber}")
	private String buildNumber;

	@Value("${buildDate}")
	private String buildDate;

	@Value("${hcVersion}")
	private String hcVersion;

	@Value("${otelVersion}")
	private String otelVersion;

	/**
	 * Creates the agent information bean <code>agentInfo</code>.
	 * 
	 * @return new {@link Map} of key-value pair attributes
	 */
	@Bean
	public Map<String, String> agentInfo() {

		return Map.of(
			AGENT_INFO_NAME_ATTRIBUTE_KEY, projectName,
			AGENT_INFO_VERSION_ATTRIBUTE_KEY, projectVersion,
			AGENT_INFO_BUILD_NUMBER_ATTRIBUTE_KEY, buildNumber,
			AGENT_INFO_BUILD_DATE_ATTRIBUTE_KEY, buildDate,
			AGENT_INFO_HC_VERSION_ATTRIBUTE_KEY, hcVersion,
			AGENT_INFO_OTEL_VERSION_ATTRIBUTE_KEY, otelVersion
		);
	}
}
