package com.sentrysoftware.matrix.strategy.discovery;

import com.sentrysoftware.matrix.connector.model.monitor.MonitorJob;
import com.sentrysoftware.matrix.connector.model.monitor.StandardMonitorJob;
import com.sentrysoftware.matrix.connector.model.monitor.task.AbstractMonitorTask;
import com.sentrysoftware.matrix.strategy.AbstractAllAtOnceStrategy;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;

@NoArgsConstructor
@EqualsAndHashCode(callSuper = true)
public class DiscoveryStrategy extends AbstractAllAtOnceStrategy {

	private static final String JOB_NAME = "discovery";

	@Override
	protected String getJobName() {
		return JOB_NAME;
	}

	@Override
	protected AbstractMonitorTask retrieveTask(MonitorJob monitorJob) {
		if (monitorJob instanceof StandardMonitorJob standardMonitorJob) {
			return standardMonitorJob.getDiscovery();
		}
		return null;
	}
}
