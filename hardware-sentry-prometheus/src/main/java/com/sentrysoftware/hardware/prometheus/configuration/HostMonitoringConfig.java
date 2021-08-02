package com.sentrysoftware.hardware.prometheus.configuration;

import com.sentrysoftware.matrix.connector.ConnectorStore;
import com.sentrysoftware.matrix.model.monitoring.HostMonitoring;
import com.sentrysoftware.matrix.model.monitoring.IHostMonitoring;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.util.HashMap;
import java.util.Map;

/**
 * {@link HostMonitoring}, {@link ConnectorStore} as spring beans.
 */
@Configuration
public class HostMonitoringConfig {

	@Bean
	public Map<String, IHostMonitoring> hostMonitoring() {

		// The host monitoring is instantiated only once (singleton)
		return new HashMap<>();
	}

	@Bean 
	public ConnectorStore store() {
		return ConnectorStore.getInstance();
	}
}