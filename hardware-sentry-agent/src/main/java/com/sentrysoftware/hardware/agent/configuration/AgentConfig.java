package com.sentrysoftware.hardware.agent.configuration;

import static com.sentrysoftware.hardware.agent.configuration.ConfigHelper.buildHostMonitoringMap;
import static com.sentrysoftware.hardware.agent.configuration.ConfigHelper.readConfigurationSafe;

import java.io.File;
import java.util.Map;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import com.sentrysoftware.hardware.agent.dto.MultiHostsConfigurationDTO;
import com.sentrysoftware.matrix.connector.ConnectorStore;
import com.sentrysoftware.matrix.model.monitoring.IHostMonitoring;

@Configuration
public class AgentConfig {

	@Value("${target.config.file}")
	private File targetConfigFile;

	// These properties come from src/main/resources/application.yml or application-ssl.yml
	// which themselves are "filtered" by Maven's resources Plugin to expose
	// pom.xml's values
	@Value("${project.name}")
	private String projectName;

	@Value("${project.version}")
	private String projectVersion;

	@Value("${buildNumber}")
	private String buildNumber;

	@Value("${timestamp}")
	private String timestamp;

	@Value("${hcVersion}")
	private String hcVersion;

	@Bean
	public MultiHostsConfigurationDTO multiHostsConfigurationDto() {
		return readConfigurationSafe(targetConfigFile);
	}

	@Bean
	public Map<String, IHostMonitoring> hostMonitoringMap() {
		// The host monitoring is instantiated only once (singleton)
		return buildHostMonitoringMap(targetConfigFile, ConnectorStore.getInstance().getConnectors().keySet());
	}

	@Bean
	public Map<String, String> agentInfo() {

		return Map.of(
				"project_name", projectName,
				"project_version", projectVersion,
				"build_number", buildNumber,
				"timestamp", timestamp,
				"hc_version", hcVersion
		);
	}

}