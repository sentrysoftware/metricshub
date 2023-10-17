package com.sentrysoftware.metricshub.agent.service.scheduling;

import java.util.Map;
import java.util.concurrent.ScheduledFuture;
import lombok.NonNull;
import lombok.RequiredArgsConstructor;
import org.springframework.scheduling.TaskScheduler;

@RequiredArgsConstructor
public abstract class AbstractScheduling {

	@NonNull
	protected TaskScheduler taskScheduler;

	@NonNull
	protected Map<String, ScheduledFuture<?>> schedules;

	@NonNull
	protected Map<String, String> otelSdkConfiguration;

	/**
	 * Schedules a task
	 */
	public abstract void schedule();
}
