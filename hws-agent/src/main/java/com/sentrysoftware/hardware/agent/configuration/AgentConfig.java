package com.sentrysoftware.hardware.agent.configuration;

import static com.sentrysoftware.hardware.agent.configuration.ConfigHelper.buildHostMonitoringMap;
import static com.sentrysoftware.hardware.agent.configuration.ConfigHelper.readConfigurationSafe;

import java.io.File;
import java.util.Map;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import com.sentrysoftware.hardware.agent.dto.MultiHostsConfigurationDto;
import com.sentrysoftware.hardware.agent.mapping.opentelemetry.MappingConstants;
import com.sentrysoftware.hardware.agent.service.ConnectorsLoaderService;
import com.sentrysoftware.matrix.connector.ConnectorStore;
import com.sentrysoftware.matrix.connector.parser.ConnectorParser;
import com.sentrysoftware.matrix.model.monitoring.IHostMonitoring;

@Configuration
public class AgentConfig {

	public static final String AGENT_INFO_NAME_ATTRIBUTE_KEY = MappingConstants.NAME;
	public static final String AGENT_INFO_VERSION_ATTRIBUTE_KEY = "version";
	public static final String AGENT_INFO_BUILD_DATE_ATTRIBUTE_KEY = "build_date";
	public static final String AGENT_INFO_BUILD_NUMBER_ATTRIBUTE_KEY = "build_number";
	public static final String AGENT_INFO_HC_VERSION_ATTRIBUTE_KEY = "hc_version";
	public static final String AGENT_INFO_OTEL_VERSION_ATTRIBUTE_KEY = "otel_version";

	// These properties come from src/main/resources/application.yml or application-ssl.yml
	// which themselves are "filtered" by Maven's resources Plugin to expose
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

	@Bean
	public ConnectorParser connectorParser() {
		return new ConnectorParser();
	}

	@Bean
	public MultiHostsConfigurationDto multiHostsConfigurationDto(final File configFile, final ConnectorsLoaderService connectorsLoader) {

		// Load additional connectors before reading the configuration
		connectorsLoader.load();

		return readConfigurationSafe(configFile);
	}

	@Bean
	public Map<String, IHostMonitoring> hostMonitoringMap(final MultiHostsConfigurationDto multiHostsConfigurationDto) {
		// The host monitoring is instantiated only once (singleton)
		return buildHostMonitoringMap(multiHostsConfigurationDto, ConnectorStore.getInstance().getConnectors().keySet());
	}

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