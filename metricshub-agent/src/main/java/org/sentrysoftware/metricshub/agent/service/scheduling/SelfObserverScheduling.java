package org.sentrysoftware.metricshub.agent.service.scheduling;

/*-
 * ╱╲╱╲╱╲╱╲╱╲╱╲╱╲╱╲╱╲╱╲╱╲╱╲╱╲╱╲╱╲╱╲╱╲╱╲╱╲╱╲
 * MetricsHub Agent
 * ჻჻჻჻჻჻
 * Copyright 2023 - 2024 Sentry Software
 * ჻჻჻჻჻჻
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Affero General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU Affero General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 * ╲╱╲╱╲╱╲╱╲╱╲╱╲╱╲╱╲╱╲╱╲╱╲╱╲╱╲╱╲╱╲╱╲╱╲╱╲╱╲╱
 */

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
import org.sentrysoftware.metricshub.agent.config.AgentConfig;
import org.sentrysoftware.metricshub.agent.context.AgentInfo;
import org.sentrysoftware.metricshub.agent.helper.ConfigHelper;
import org.sentrysoftware.metricshub.agent.helper.OtelHelper;
import org.sentrysoftware.metricshub.agent.service.signal.SimpleGaugeMetricObserver;
import org.springframework.scheduling.TaskScheduler;
import org.springframework.scheduling.support.PeriodicTrigger;

/**
 * Scheduling class responsible for self-observation tasks in MetricsHub.
 * This class creates and schedules an observer for collecting MetricsHub agent information.
 */
@Slf4j
public class SelfObserverScheduling extends AbstractScheduling {

	/**
	 * Key used to identify the self-observation task in the schedules map.
	 */
	public static final String METRICSHUB_OVERALL_SELF_TASK_KEY = "metricshub-overall-self-task";
	/**
	 * Description for the overall MetricsHub agent information metric.
	 */
	private static final String METRICS_HUB_AGENT_INFORMATION = "MetricsHub agent information.";

	@NonNull
	private AgentInfo agentInfo;

	@NonNull
	private AgentConfig agentConfig;

	/**
	 * Constructs a new instance of {@code SelfObserverScheduling}.
	 *
	 * @param taskScheduler        The task scheduler to use for scheduling.
	 * @param schedules            The map to store scheduled tasks.
	 * @param otelSdkConfiguration The OpenTelemetry SDK configuration.
	 * @param agentInfo            The information about the MetricsHub agent.
	 * @param agentConfig          The configuration for the MetricsHub agent.
	 */
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
			.withMeter(meterProvider.get("org.sentrysoftware.metricshub.agent"))
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
