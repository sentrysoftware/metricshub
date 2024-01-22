package org.sentrysoftware.metricshub.engine.strategy.simple;

import lombok.Builder;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;
import lombok.NonNull;
import org.sentrysoftware.metricshub.engine.client.ClientsExecutor;
import org.sentrysoftware.metricshub.engine.connector.model.monitor.MonitorJob;
import org.sentrysoftware.metricshub.engine.connector.model.monitor.SimpleMonitorJob;
import org.sentrysoftware.metricshub.engine.connector.model.monitor.task.AbstractMonitorTask;
import org.sentrysoftware.metricshub.engine.strategy.AbstractAllAtOnceStrategy;
import org.sentrysoftware.metricshub.engine.telemetry.TelemetryManager;

@NoArgsConstructor
@EqualsAndHashCode(callSuper = true)
public class SimpleStrategy extends AbstractAllAtOnceStrategy {

	private static final String JOB_NAME = "simple";

	@Builder
	public SimpleStrategy(
		@NonNull final TelemetryManager telemetryManager,
		@NonNull final Long strategyTime,
		@NonNull final ClientsExecutor clientsExecutor
	) {
		super(telemetryManager, strategyTime, clientsExecutor);
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
