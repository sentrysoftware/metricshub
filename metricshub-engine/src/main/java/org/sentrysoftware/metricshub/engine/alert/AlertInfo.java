package org.sentrysoftware.metricshub.engine.alert;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.sentrysoftware.metricshub.engine.telemetry.Monitor;

/**
 * Represents information related to an alert, including the associated alert rule, monitor,
 * metric name, hostname, host type, and parent monitor.
 */
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
