package com.sentrysoftware.metricshub.engine.strategy.simple;

import com.sentrysoftware.metricshub.engine.connector.model.monitor.MonitorJob;
import com.sentrysoftware.metricshub.engine.connector.model.monitor.SimpleMonitorJob;
import com.sentrysoftware.metricshub.engine.connector.model.monitor.task.AbstractMonitorTask;
import com.sentrysoftware.metricshub.engine.matsya.MatsyaClientsExecutor;
import com.sentrysoftware.metricshub.engine.strategy.AbstractAllAtOnceStrategy;
import com.sentrysoftware.metricshub.engine.telemetry.TelemetryManager;
import lombok.Builder;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;
import lombok.NonNull;

@NoArgsConstructor
@EqualsAndHashCode(callSuper = true)
public class SimpleStrategy extends AbstractAllAtOnceStrategy {

	private static final String JOB_NAME = "simple";

	@Builder
	public SimpleStrategy(
		@NonNull final TelemetryManager telemetryManager,
		@NonNull final Long strategyTime,
		@NonNull final MatsyaClientsExecutor matsyaClientsExecutor
	) {
		super(telemetryManager, strategyTime, matsyaClientsExecutor);
	}

	@Override
	protected String getJobName() {
		return JOB_NAME;
	}

	@Override
	protected AbstractMonitorTask retrieveTask(MonitorJob monitorJob) {
		if (monitorJob instanceof SimpleMonitorJob simpleMonitorJob) {
			return simpleMonitorJob.getSimple();
		}
		return null;
	}
}
