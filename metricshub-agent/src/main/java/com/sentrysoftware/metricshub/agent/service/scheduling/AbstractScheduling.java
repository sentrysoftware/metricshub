package com.sentrysoftware.metricshub.agent.service.scheduling;

import java.util.Map;
import java.util.concurrent.ScheduledFuture;
import lombok.NonNull;
import lombok.RequiredArgsConstructor;
import org.springframework.scheduling.TaskScheduler;

/**
 * AbstractScheduling is the base class for implementing scheduling-related tasks in the MetricsHub agent.
 * It provides common functionality for scheduling tasks using a TaskScheduler.
 */
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
