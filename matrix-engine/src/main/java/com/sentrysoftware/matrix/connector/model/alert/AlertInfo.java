package com.sentrysoftware.matrix.connector.model.alert;

import com.sentrysoftware.matrix.telemetry.Monitor;
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
	private String metricName;
	private String hostname;
	private String hostType;
	private Monitor parentMonitor;
}
