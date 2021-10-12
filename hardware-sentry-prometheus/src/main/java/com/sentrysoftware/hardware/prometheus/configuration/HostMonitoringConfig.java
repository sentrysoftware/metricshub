package com.sentrysoftware.hardware.prometheus.configuration;

import static com.sentrysoftware.hardware.prometheus.configuration.ConfigHelper.buildHostMonitoringMap;
import static com.sentrysoftware.hardware.prometheus.configuration.ConfigHelper.readConfigurationSafe;

import java.io.File;
import java.util.Map;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import com.sentrysoftware.hardware.prometheus.dto.MultiHostsConfigurationDTO;
import com.sentrysoftware.matrix.connector.ConnectorStore;
import com.sentrysoftware.matrix.model.monitoring.HostMonitoring;
import com.sentrysoftware.matrix.model.monitoring.IHostMonitoring;

/**
 * {@link HostMonitoring}, {@link ConnectorStore} as spring beans.
 */
@Configuration
public class HostMonitoringConfig {

	@Value("${target.config.file}")
	private File targetConfigFile;

	@Bean
	public MultiHostsConfigurationDTO multiHostsConfigurationDto() {
		return readConfigurationSafe(targetConfigFile);
	}

	@Bean
	public Map<String, IHostMonitoring> hostMonitoringMap() {
		// The host monitoring is instantiated only once (singleton)
		return buildHostMonitoringMap(targetConfigFile, ConnectorStore.getInstance().getConnectors().keySet());
	}

}