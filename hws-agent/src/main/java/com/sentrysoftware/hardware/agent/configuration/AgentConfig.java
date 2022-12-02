package com.sentrysoftware.hardware.agent.configuration;

import static com.sentrysoftware.hardware.agent.configuration.ConfigHelper.buildHostMonitoringMap;
import static com.sentrysoftware.hardware.agent.configuration.ConfigHelper.readConfigurationSafe;

import java.io.File;
import java.util.Map;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import com.sentrysoftware.hardware.agent.dto.MultiHostsConfigurationDto;
import com.sentrysoftware.hardware.agent.service.ConnectorsLoaderService;
import com.sentrysoftware.matrix.connector.ConnectorStore;
import com.sentrysoftware.matrix.connector.parser.ConnectorParser;
import com.sentrysoftware.matrix.model.monitoring.IHostMonitoring;

@Configuration
public class AgentConfig {

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

}