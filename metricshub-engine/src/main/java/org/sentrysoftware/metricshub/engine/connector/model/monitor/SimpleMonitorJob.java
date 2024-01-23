package org.sentrysoftware.metricshub.engine.connector.model.monitor;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.sentrysoftware.metricshub.engine.connector.model.monitor.task.Simple;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class SimpleMonitorJob implements MonitorJob {

	private static final long serialVersionUID = 1L;

	private Simple simple;
}
