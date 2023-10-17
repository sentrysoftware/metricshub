package com.sentrysoftware.metricshub.agent.service.scheduling;

import com.sentrysoftware.metricshub.agent.config.AgentConfig;
import com.sentrysoftware.metricshub.agent.context.AgentInfo;
import com.sentrysoftware.metricshub.agent.helper.ConfigHelper;
import com.sentrysoftware.metricshub.agent.helper.OtelHelper;
import com.sentrysoftware.metricshub.agent.service.signal.SimpleGaugeMetricObserver;
import io.opentelemetry.api.common.Attributes;
import io.opentelemetry.sdk.autoconfigure.AutoConfiguredOpenTelemetrySdk;
import io.opentelemetry.sdk.metrics.SdkMeterProvider;
import io.opentelemetry.sdk.resources.Resource;
import java.time.Duration;
import java.time.temporal.ChronoUnit;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.ScheduledFuture;
import lombok.Builder;
import lombok.NonNull;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.TaskScheduler;
import org.springframework.scheduling.support.PeriodicTrigger;

@Slf4j
public class SelfObserverScheduling extends AbstractScheduling {

	public static final String METRICSHUB_OVERALL_SELF_TASK_KEY = "metricshub-overall-self-task";
	private static final String METRICS_HUB_AGENT_INFORMATION = "MetricsHub agent information.";

	@NonNull
	private AgentInfo agentInfo;

	@NonNull
	private AgentConfig agentConfig;

	@Builder(setterPrefix = "with")
	public SelfObserverScheduling(
		@NonNull final TaskScheduler taskScheduler,
		@NonNull final Map<String, ScheduledFuture<?>> schedules,
		@NonNull final Map<String, String> otelSdkConfiguration,
		@NonNull final AgentInfo agentInfo,
		@NonNull final AgentConfig agentConfig
	) {
		super(taskScheduler, schedules, otelSdkConfiguration);
		this.agentConfig = agentConfig;
		this.agentInfo = agentInfo;
	}

	@Override
	public void schedule() {
		final Map<String, String> resourceAttributes = new HashMap<>();

		// Add our attributes
		ConfigHelper.mergeAttributes(agentInfo.getResourceAttributes(), resourceAttributes);

		// Override with the user's attributes
		ConfigHelper.mergeAttributes(agentConfig.getAttributes(), resourceAttributes);

		// Create the service resource
		final Resource resource = OtelHelper.createOpenTelemetryResource(resourceAttributes);

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

		final Map<String, String> attributeMap = new HashMap<>();

		// Add our attributes
		ConfigHelper.mergeAttributes(agentInfo.getMetricAttributes(), attributeMap);

		// Override with the user's attributes
		ConfigHelper.mergeAttributes(agentConfig.getAttributes(), attributeMap);

		// Build the OTEL attributes instance
		final Attributes attributes = OtelHelper.buildOtelAttributesFromMap(attributeMap);

		// Initialize the observer
		SimpleGaugeMetricObserver
			.builder()
			.withDescription(METRICS_HUB_AGENT_INFORMATION)
			.withMeter(meterProvider.get("com.sentrysoftware.metricshub.agent"))
			.withMetricValue(1.0)
			.withAttributes(attributes)
			.withMetricName(AgentInfo.METRICS_HUB_AGENT_METRIC_NAME)
			.build()
			.init();

		// Here we go
		final ScheduledFuture<?> scheduledFuture = taskScheduler.schedule(meterProvider::forceFlush, trigger);

		// Save the delayed result-bearing action that can be cancelled
		schedules.put(METRICSHUB_OVERALL_SELF_TASK_KEY, scheduledFuture);

		log.info("Self Observer scheduled.");
	}
}
