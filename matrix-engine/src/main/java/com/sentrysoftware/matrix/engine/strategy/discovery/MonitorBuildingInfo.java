package com.sentrysoftware.matrix.engine.strategy.discovery;

import com.sentrysoftware.matrix.connector.model.monitor.MonitorType;
import com.sentrysoftware.matrix.model.monitor.Monitor;
import com.sentrysoftware.matrix.model.monitoring.IHostMonitoring;

import com.sentrysoftware.matrix.engine.host.HostType;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Builder
@Data
@AllArgsConstructor
@NoArgsConstructor
public class MonitorBuildingInfo {

	private Monitor monitor;
	private String connectorName;
	private Monitor targetMonitor;
	private IHostMonitoring hostMonitoring;
	private MonitorType monitorType;
	private HostType hostType;
	private String hostname;

}
