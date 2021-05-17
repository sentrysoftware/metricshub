package com.sentrysoftware.hardware.prometheus.configuration;

import java.util.UUID;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import com.sentrysoftware.matrix.connector.ConnectorStore;
import com.sentrysoftware.matrix.engine.Engine;
import com.sentrysoftware.matrix.model.monitoring.HostMonitoring;
import com.sentrysoftware.matrix.model.monitoring.HostMonitoringFactory;
import com.sentrysoftware.matrix.model.monitoring.IHostMonitoring;

/**
 * {@link HostMonitoring}, {@link ConnectorStore} and {@link Engine} as spring beans.
 */
@Configuration
public class HostMonitoringConfig {

	private static final String HOST_MONITORING_KEY = UUID.randomUUID().toString();

	@Bean
	public IHostMonitoring hostMonitoring() {

		// The host monitoring is instantiated only once (singleton)
		return HostMonitoringFactory.getInstance().createHostMonitoring(HOST_MONITORING_KEY);
	}

	@Bean 
	public ConnectorStore store() {
		return ConnectorStore.getInstance();
	}

	@Bean
	public Engine engine() {
		return new Engine();
	}
}