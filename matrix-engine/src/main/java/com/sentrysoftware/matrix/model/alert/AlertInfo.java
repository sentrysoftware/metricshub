package com.sentrysoftware.matrix.model.alert;

import com.sentrysoftware.matrix.model.monitor.Monitor;
import com.sentrysoftware.matrix.model.monitoring.IHostMonitoring;

import com.sentrysoftware.matrix.engine.host.HardwareHost;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class AlertInfo {

	private AlertRule alertRule;
	private Monitor monitor;
	private String parameterName;
	private HardwareHost hardwareHost;
	private IHostMonitoring hostMonitoring;

}
