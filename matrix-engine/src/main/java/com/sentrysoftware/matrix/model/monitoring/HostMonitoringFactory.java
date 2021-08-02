package com.sentrysoftware.matrix.model.monitoring;

import com.sentrysoftware.matrix.engine.EngineConfiguration;

import java.util.HashMap;
import java.util.Map;

public class HostMonitoringFactory {

	private static final HostMonitoringFactory INSTANCE = new HostMonitoringFactory();

	private final Map<String, IHostMonitoring> hostMonitoring = new HashMap<>();

	public synchronized IHostMonitoring createHostMonitoring(final String hostMonitoringId,
															 EngineConfiguration engineConfiguration) {

		if (hostMonitoringId != null) {

			HostMonitoring monitoring = new HostMonitoring();
			monitoring.setEngineConfiguration(engineConfiguration);

			this.hostMonitoring.putIfAbsent(hostMonitoringId, monitoring);

			return monitoring;
		}

		return HostMonitoring.HOST_MONITORING;
	}

	public static HostMonitoringFactory getInstance() {

		return INSTANCE;
	}
}
