package com.sentrysoftware.metricshub.engine.strategy.discovery;

import com.sentrysoftware.metricshub.engine.client.ClientsExecutor;
import com.sentrysoftware.metricshub.engine.connector.model.monitor.MonitorJob;
import com.sentrysoftware.metricshub.engine.connector.model.monitor.StandardMonitorJob;
import com.sentrysoftware.metricshub.engine.connector.model.monitor.task.AbstractMonitorTask;
import com.sentrysoftware.metricshub.engine.strategy.AbstractAllAtOnceStrategy;
import com.sentrysoftware.metricshub.engine.telemetry.TelemetryManager;
import lombok.Builder;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;
import lombok.NonNull;

@NoArgsConstructor
@EqualsAndHashCode(callSuper = true)
public class DiscoveryStrategy extends AbstractAllAtOnceStrategy {

	private static final String JOB_NAME = "discovery";

	@Builder
	public DiscoveryStrategy(
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
		if (monitorJob instanceof StandardMonitorJob standardMonitorJob) {
			return standardMonitorJob.getDiscovery();
		}
		return null;
	}
}
