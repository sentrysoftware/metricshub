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

/**
 * Scheduling class responsible for scheduling monitoring tasks for a specific
 * resource in MetricsHub. This class creates and schedules a
 * {@link MonitoringTask} to collect metrics for the specified resource at a
 * periodic interval based on the configured collect period.
 */
@Slf4j
public class ResourceScheduling extends AbstractScheduling {

	/**
	 * Format for creating unique keys for identifying the scheduled tasks related
	 * to a specific resource.
	 */
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

	/**
	 * Constructs a new instance of {@code ResourceScheduling}.
	 *
	 * @param taskScheduler         The task scheduler to use for scheduling.
	 * @param schedules             The map to store scheduled tasks.
	 * @param otelSdkConfiguration  The OpenTelemetry SDK configuration.
	 * @param resourceGroupKey      Key for identifying the resource group to which
	 *                              the resource belongs.
	 * @param resourceKey           Key for identifying the resource.
	 * @param resourceConfig        Configuration for the monitored resource.
	 * @param telemetryManager      Telemetry manager responsible for collecting and
	 *                              processing metrics for the resource.
	 * @param hostMetricDefinitions Definitions of metrics for the host.
	 */
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
