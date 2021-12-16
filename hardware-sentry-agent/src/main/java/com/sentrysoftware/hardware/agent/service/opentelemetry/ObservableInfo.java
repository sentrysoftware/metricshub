package com.sentrysoftware.hardware.agent.service.opentelemetry;

import com.sentrysoftware.matrix.connector.model.monitor.MonitorType;
import com.sentrysoftware.matrix.model.monitor.Monitor;
import com.sentrysoftware.matrix.model.monitoring.IHostMonitoring;

import lombok.Data;
import lombok.NonNull;
import lombok.RequiredArgsConstructor;

@Data
@RequiredArgsConstructor
public class ObservableInfo {

	@NonNull
	private String monitorId;
	@NonNull
	private MonitorType monitorType;
	@NonNull
	private IHostMonitoring hostMonitoring;

	/**
	 * Get monitor by its id and type from the {@link IHostMonitoring} instance
	 * 
	 * @return The current monitor located in the {@link IHostMonitoring} instance
	 */
	public Monitor getMonitor() {
		return hostMonitoring.getMonitorByTypeAndId(monitorType, monitorId);
	}
}
