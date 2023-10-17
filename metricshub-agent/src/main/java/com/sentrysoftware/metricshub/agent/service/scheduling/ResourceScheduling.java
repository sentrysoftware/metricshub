package com.sentrysoftware.metricshub.agent.service.scheduling;

import com.sentrysoftware.metricshub.agent.config.ResourceConfig;
import com.sentrysoftware.metricshub.agent.context.MetricDefinitions;
import com.sentrysoftware.metricshub.agent.service.task.MonitoringTask;
import com.sentrysoftware.metricshub.agent.service.task.MonitoringTaskInfo;
import com.sentrysoftware.metricshub.engine.telemetry.TelemetryManager;
import java.time.Duration;
import java.time.temporal.ChronoUnit;
import java.util.Map;
import java.util.concurrent.ScheduledFuture;
import lombok.Builder;
import lombok.NonNull;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.TaskScheduler;
import org.springframework.scheduling.support.PeriodicTrigger;

@Slf4j
public class ResourceScheduling extends AbstractScheduling {

	public static final String METRICSHUB_RESOURCE_KEY_FORMAT = "metricshub-resource-%s-%s";

	@NonNull
	private String resourceGroupKey;

	@NonNull
	private String resourceKey;

	@NonNull
	private ResourceConfig resourceConfig;

	@NonNull
	private TelemetryManager telemetryManager;

	@NonNull
	private MetricDefinitions hostMetricDefinitions;

	@Builder(setterPrefix = "with")
	public ResourceScheduling(
		@NonNull final TaskScheduler taskScheduler,
		@NonNull final Map<String, ScheduledFuture<?>> schedules,
		@NonNull final Map<String, String> otelSdkConfiguration,
		@NonNull final String resourceGroupKey,
		@NonNull final String resourceKey,
		@NonNull final ResourceConfig resourceConfig,
		@NonNull final TelemetryManager telemetryManager,
		@NonNull final MetricDefinitions hostMetricDefinitions
	) {
		super(taskScheduler, schedules, otelSdkConfiguration);
		this.resourceGroupKey = resourceGroupKey;
		this.resourceKey = resourceKey;
		this.resourceConfig = resourceConfig;
		this.telemetryManager = telemetryManager;
		this.hostMetricDefinitions = hostMetricDefinitions;
	}

	@Override
	public void schedule() {
		// Need a periodic trigger because we need the job to be scheduled based on the configured collect period
		final PeriodicTrigger trigger = new PeriodicTrigger(
			Duration.of(resourceConfig.getCollectPeriod(), ChronoUnit.SECONDS)
		);

		// Create the monitoring task
		final MonitoringTask monitoringTask = new MonitoringTask(
			MonitoringTaskInfo
				.builder()
				.telemetryManager(telemetryManager)
				.resourceConfig(resourceConfig)
				.resourceGroupKey(resourceGroupKey)
				.resourceKey(resourceKey)
				.otelSdkConfiguration(otelSdkConfiguration)
				.hostMetricDefinitions(hostMetricDefinitions)
				.build()
		);

		// Here we go
		final ScheduledFuture<?> scheduledFuture = taskScheduler.schedule(monitoringTask, trigger);

		// Don't forget to store the scheduled task in case we want to cancel it due to a configuration change
		schedules.put(String.format(METRICSHUB_RESOURCE_KEY_FORMAT, resourceGroupKey, resourceKey), scheduledFuture);

		log.info("Scheduled job for resource id {} defined in resource group id {}.", resourceKey, resourceGroupKey);
	}
}
