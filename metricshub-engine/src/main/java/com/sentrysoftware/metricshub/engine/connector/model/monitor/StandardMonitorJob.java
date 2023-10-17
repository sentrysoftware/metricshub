package com.sentrysoftware.metricshub.engine.connector.model.monitor;

import com.sentrysoftware.metricshub.engine.connector.model.monitor.task.AbstractCollect;
import com.sentrysoftware.metricshub.engine.connector.model.monitor.task.Discovery;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class StandardMonitorJob implements MonitorJob {

	private static final long serialVersionUID = 1L;

	private Discovery discovery;
	private AbstractCollect collect;
}
