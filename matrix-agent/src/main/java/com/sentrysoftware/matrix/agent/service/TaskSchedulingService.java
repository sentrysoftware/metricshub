package com.sentrysoftware.matrix.agent.service;

import com.sentrysoftware.matrix.agent.config.AgentConfig;
import com.sentrysoftware.matrix.agent.config.ResourceGroupConfig;
import com.sentrysoftware.matrix.agent.context.AgentInfo;
import com.sentrysoftware.matrix.agent.helper.ConfigHelper;
import com.sentrysoftware.matrix.agent.helper.OtelHelper;
import com.sentrysoftware.matrix.agent.service.signal.ResourceGroupMetricsObserver;
import com.sentrysoftware.matrix.agent.service.signal.SelfObserver;
import com.sentrysoftware.matrix.connector.model.ConnectorStore;
import com.sentrysoftware.matrix.telemetry.TelemetryManager;
import io.opentelemetry.sdk.autoconfigure.AutoConfiguredOpenTelemetrySdk;
import io.opentelemetry.sdk.metrics.SdkMeterProvider;
import io.opentelemetry.sdk.resources.Resource;
import java.io.File;
import java.time.Duration;
import java.time.temporal.ChronoUnit;
import java.util.HashMap;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Objects;
import java.util.concurrent.ScheduledFuture;
import lombok.Builder;
import lombok.Data;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.concurrent.ThreadPoolTaskScheduler;
import org.springframework.scheduling.support.PeriodicTrigger;

@Data
@Builder(setterPrefix = "with")
@Slf4j
public class TaskSchedulingService {

	static final String METRICSHUB_RESOURCE_GROUP_KEY_FORMAT = "metricshub-resource-group-%s";
	static final String METRICSHUB_OVERALL_SELF_TASK_KEY = "metricshub-overall-self-task";

	private ConnectorStore connectorStore;
	private File configFile;
	private AgentConfig agentConfig;
	private AgentInfo agentInfo;
	private ThreadPoolTaskScheduler taskScheduler;
	private Map<String, ScheduledFuture<?>> resourceSchedules;
	private OtelCollectorProcessService otelCollectorProcessService;
	private Map<String, Map<String, TelemetryManager>> telemetryManagers;
	private Map<String, String> otelSdkConfiguration;

	/**
	 * Start scheduling
	 */
	public void start() {
		// Self observer scheduling
		scheduleSelfObserver();

		// Resource Group observers scheduling
		scheduleResourceGroupObservers();
	}

	/**
	 * Initialize the {@link ResourceGroupMetricsObserver} for each resource group and
	 * trigger a periodic task to flush metrics
	 */
	void scheduleResourceGroupObservers() {
		log.info("Scheduling Resource Group Observers.");

		agentConfig
			.getResourceGroups()
			.entrySet()
			.stream()
			.filter(entry -> Objects.nonNull(entry.getValue()))
			.forEach(this::scheduleResourceGroup);

		log.info("Resource Group Observers scheduled.");
	}

	/**
	 * Initialize the {@link ResourceGroupMetricsObserver} for the given resource group
	 * entry and trigger a periodic task to flush metrics
	 *
	 * @param resourceGroupConfigEntry Key-value defining the {@link ResourceGroupConfig} instance
	 */
	void scheduleResourceGroup(final Entry<String, ResourceGroupConfig> resourceGroupConfigEntry) {
		final ResourceGroupConfig resourceGroupConfig = resourceGroupConfigEntry.getValue();
		final String resourceGroupKey = resourceGroupConfigEntry.getKey();

		// Create the service resource
		final Resource resource = OtelHelper.createServiceResource(resourceGroupConfig.getAttributes());

		final AutoConfiguredOpenTelemetrySdk autoConfiguredOpenTelemetrySdk = OtelHelper.initOpenTelemetrySdk(
			resource,
			otelSdkConfiguration
		);

		// Need a periodic trigger because we need the job to be scheduled based on the configured collect period
		final PeriodicTrigger trigger = new PeriodicTrigger(
			Duration.of(agentConfig.getCollectPeriod(), ChronoUnit.SECONDS)
		);

		// Get the SDK Meter provider
		final SdkMeterProvider meterProvider = autoConfiguredOpenTelemetrySdk.getOpenTelemetrySdk().getSdkMeterProvider();

		// Initialize the Observer
		ResourceGroupMetricsObserver
			.builder()
			.resourceGroupKey(resourceGroupKey)
			.resourceGroupConfig(resourceGroupConfig)
			.sdkMeterProvider(meterProvider)
			.build()
			.init();

		// Schedule the flush task
		final ScheduledFuture<?> scheduledFuture = taskScheduler.schedule(meterProvider::forceFlush, trigger);

		// Save the delayed result-bearing action that can be cancelled
		resourceSchedules.put(String.format(METRICSHUB_RESOURCE_GROUP_KEY_FORMAT, resourceGroupKey), scheduledFuture);
	}

	/**
	 * Initialize the {@link SelfObserver} and trigger a periodic task to flush metrics
	 */
	void scheduleSelfObserver() {
		log.info("Scheduling Self Observer.");

		final Map<String, String> resourceAttributes = new HashMap<>();

		// Add our attributes
		ConfigHelper.mergeAttributes(agentInfo.getResourceAttributes(), resourceAttributes);

		// Override with the user's attributes
		ConfigHelper.mergeAttributes(agentConfig.getAttributes(), resourceAttributes);

		// Create the service resource
		final Resource resource = OtelHelper.createServiceResource(resourceAttributes);

		final AutoConfiguredOpenTelemetrySdk autoConfiguredOpenTelemetrySdk = OtelHelper.initOpenTelemetrySdk(
			resource,
			otelSdkConfiguration
		);

		// Need a periodic trigger because we need the job to be scheduled based on the configured collect period
		final PeriodicTrigger trigger = new PeriodicTrigger(
			Duration.of(agentConfig.getCollectPeriod(), ChronoUnit.SECONDS)
		);

		// Get the SDK Meter provider
		final SdkMeterProvider meterProvider = autoConfiguredOpenTelemetrySdk.getOpenTelemetrySdk().getSdkMeterProvider();

		// Initialize the observer
		SelfObserver
			.builder()
			.metricAttributes(agentInfo.getMetricAttributes())
			.userAttributes(agentConfig.getAttributes())
			.sdkMeterProvider(meterProvider)
			.build()
			.init();

		// Here we go
		final ScheduledFuture<?> scheduledFuture = taskScheduler.schedule(meterProvider::forceFlush, trigger);

		// Save the delayed result-bearing action that can be cancelled
		resourceSchedules.put(METRICSHUB_OVERALL_SELF_TASK_KEY, scheduledFuture);

		log.info("Self Observer scheduled.");
	}

	/**
	 * Create and initialize a scheduler instance
	 *
	 * @return new instance of {@link ThreadPoolTaskScheduler}
	 */
	public static ThreadPoolTaskScheduler newScheduler(final int jobPoolSize) {
		// Create the TaskScheduler
		final ThreadPoolTaskScheduler threadPoolTaskScheduler = new ThreadPoolTaskScheduler();

		// Set the maximum pool size.
		threadPoolTaskScheduler.setPoolSize(jobPoolSize);

		// Set the thread name prefix
		threadPoolTaskScheduler.setThreadNamePrefix("metricshub-task-");

		// Initialization
		threadPoolTaskScheduler.initialize();

		return threadPoolTaskScheduler;
	}
}
