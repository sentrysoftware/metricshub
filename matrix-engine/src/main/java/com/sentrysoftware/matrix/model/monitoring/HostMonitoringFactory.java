package com.sentrysoftware.matrix.model.monitoring;

import java.util.HashMap;
import java.util.Map;

public class HostMonitoringFactory {

	private static final HostMonitoringFactory INSTANCE = new HostMonitoringFactory();

	private final Map<String, IHostMonitoring> hostMonitoring = new HashMap<>();

	public synchronized IHostMonitoring createHostMonitoring(final String hostMonitoringId) {

		return null == hostMonitoringId ? HostMonitoring.HOST_MONITORING
				: hostMonitoring.computeIfAbsent(hostMonitoringId, k -> new HostMonitoring());
	}

	public static HostMonitoringFactory getInstance() {

		return INSTANCE;
	}
}
