package com.sentrysoftware.matrix.model.alert;

import com.sentrysoftware.matrix.engine.target.HardwareTarget;
import com.sentrysoftware.matrix.model.monitor.Monitor;
import com.sentrysoftware.matrix.model.monitoring.IHostMonitoring;

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
	private HardwareTarget hardwareTarget;
	private IHostMonitoring hostMonitoring;

}
