package org.sentrysoftware.metricshub.engine.connector.model.monitor;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.sentrysoftware.metricshub.engine.connector.model.monitor.task.AbstractCollect;
import org.sentrysoftware.metricshub.engine.connector.model.monitor.task.Discovery;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class StandardMonitorJob implements MonitorJob {

	private static final long serialVersionUID = 1L;

	private Discovery discovery;
	private AbstractCollect collect;
}
