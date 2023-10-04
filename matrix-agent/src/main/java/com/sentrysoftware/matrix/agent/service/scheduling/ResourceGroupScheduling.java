package com.sentrysoftware.matrix.agent.service.scheduling;

import com.sentrysoftware.matrix.agent.config.AgentConfig;
import com.sentrysoftware.matrix.agent.config.ResourceGroupConfig;
import com.sentrysoftware.matrix.agent.helper.OtelHelper;
import com.sentrysoftware.matrix.agent.service.signal.ResourceGroupMetricsObserver;
import io.opentelemetry.sdk.autoconfigure.AutoConfiguredOpenTelemetrySdk;
import io.opentelemetry.sdk.metrics.SdkMeterProvider;
import io.opentelemetry.sdk.resources.Resource;
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
public class ResourceGroupScheduling extends AbstractScheduling {

	public static final String METRICSHUB_RESOURCE_GROUP_KEY_FORMAT = "metricshub-resource-group-%s";

	@NonNull
	private ResourceGroupConfig resourceGroupConfig;

	@NonNull
	private String resourceGroupKey;

	@NonNull
	private AgentConfig agentConfig;

	@Builder(setterPrefix = "with")
	public ResourceGroupScheduling(
		@NonNull final TaskScheduler taskScheduler,
		@NonNull final Map<String, ScheduledFuture<?>> schedules,
		@NonNull final Map<String, String> otelSdkConfiguration,
		@NonNull final String resourceGroupKey,
		@NonNull final ResourceGroupConfig resourceGroupConfig,
		@NonNull final AgentConfig agentConfig
	) {
		super(taskScheduler, schedules, otelSdkConfiguration);
		this.resourceGroupConfig = resourceGroupConfig;
		this.resourceGroupKey = resourceGroupKey;
		this.agentConfig = agentConfig;
	}

	@Override
	public void schedule() {
		// Create the resource group OTEL Resource
		final Resource resource = OtelHelper.createOpenTelemetryResource(resourceGroupConfig.getAttributes());

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
		schedules.put(String.format(METRICSHUB_RESOURCE_GROUP_KEY_FORMAT, resourceGroupKey), scheduledFuture);

		log.info("Resource Group {} scheduled.", resourceGroupKey);
	}
}
