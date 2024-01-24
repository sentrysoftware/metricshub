package org.sentrysoftware.metricshub.engine.strategy.discovery;

import lombok.Builder;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;
import lombok.NonNull;
import org.sentrysoftware.metricshub.engine.client.ClientsExecutor;
import org.sentrysoftware.metricshub.engine.connector.model.monitor.MonitorJob;
import org.sentrysoftware.metricshub.engine.connector.model.monitor.StandardMonitorJob;
import org.sentrysoftware.metricshub.engine.connector.model.monitor.task.AbstractMonitorTask;
import org.sentrysoftware.metricshub.engine.strategy.AbstractAllAtOnceStrategy;
import org.sentrysoftware.metricshub.engine.telemetry.TelemetryManager;

/**
 * The {@code DiscoveryStrategy} class represents the strategy for executing discovery tasks for monitors.
 * It extends {@link AbstractAllAtOnceStrategy} and is responsible for coordinating the execution of discovery tasks for all monitors at once.
 *
 * <p>
 * The class uses the TelemetryManager to manage monitors and metrics associated with the discovery process.
 * </p>
 */
@NoArgsConstructor
@EqualsAndHashCode(callSuper = true)
public class DiscoveryStrategy extends AbstractAllAtOnceStrategy {

	private static final String JOB_NAME = "discovery";

	/**
	 * Builder for constructing instances of {@code DiscoveryStrategy}.
	 *
	 * @param telemetryManager The telemetry manager for managing monitors and metrics.
	 * @param strategyTime     The time at which the discovery strategy is executed.
	 * @param clientsExecutor  The executor for running connector clients.
	 */
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
