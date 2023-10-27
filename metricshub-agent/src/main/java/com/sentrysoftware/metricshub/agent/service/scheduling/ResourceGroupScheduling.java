package com.sentrysoftware.metricshub.agent.service.scheduling;

import com.sentrysoftware.metricshub.agent.config.AgentConfig;
import com.sentrysoftware.metricshub.agent.config.ResourceGroupConfig;
import com.sentrysoftware.metricshub.agent.helper.OtelHelper;
import com.sentrysoftware.metricshub.agent.service.signal.SimpleGaugeMetricObserver;
import com.sentrysoftware.metricshub.engine.telemetry.MetricFactory;
import io.opentelemetry.api.common.Attributes;
import io.opentelemetry.api.metrics.Meter;
import io.opentelemetry.api.metrics.MeterProvider;
import io.opentelemetry.sdk.autoconfigure.AutoConfiguredOpenTelemetrySdk;
import io.opentelemetry.sdk.metrics.SdkMeterProvider;
import io.opentelemetry.sdk.resources.Resource;
import java.time.Duration;
import java.time.temporal.ChronoUnit;
import java.util.Map;
import java.util.Objects;
import java.util.concurrent.ScheduledFuture;
import lombok.Builder;
import lombok.NonNull;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.TaskScheduler;
import org.springframework.scheduling.support.PeriodicTrigger;

@Slf4j
public class ResourceGroupScheduling extends AbstractScheduling {

	public static final String METRICSHUB_RESOURCE_GROUP_KEY_FORMAT = "metricshub-resource-group-%s";

	/**
	 * Power Usage Effectiveness
	 */
	public static final String HW_SITE_PUE_METRIC = "hw.site.pue";

	/**
	 * Electricity cost per kilowatt-hour
	 */
	public static final String HW_SITE_ELECTRICITY_COST_METRIC = "hw.site.electricity_cost";

	/**
	 * Carbon dioxide produced per kilowatt-hour
	 */
	public static final String HW_SITE_CARBON_INTENSITY_METRIC = "hw.site.carbon_intensity";

	/**
	 * Known metric units
	 */
	private static final Map<String, String> KNOWN_METRIC_UNITS = Map.of(
		HW_SITE_CARBON_INTENSITY_METRIC,
		"g",
		HW_SITE_ELECTRICITY_COST_METRIC,
		"",
		HW_SITE_PUE_METRIC,
		"1"
	);

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

		// Build resource group attributes
		final Attributes resourceGroupAttributes = OtelHelper.buildOtelAttributesFromMap(
			resourceGroupConfig.getAttributes()
		);

		// Initialize a simple metric observer for each metric defined in the configuration
		resourceGroupConfig
			.getMetrics()
			.entrySet()
			.stream()
			.filter(metricEntry -> Objects.nonNull(metricEntry.getValue()))
			.forEach(metricEntry -> {
				final String metricKey = metricEntry.getKey();

				// The metric name can define a set of attributes
				final String metricName = MetricFactory.extractName(metricKey);

				// Build OTEL SDK Attributes
				final Attributes attributes = OtelHelper.mergeOtelAttributes(
					resourceGroupAttributes,
					OtelHelper.buildOtelAttributesFromMap(MetricFactory.extractAttributesFromMetricName(metricKey))
				);

				// Initialize the Observer
				SimpleGaugeMetricObserver
					.builder()
					.withMetricName(metricName)
					.withMetricValue(metricEntry.getValue())
					.withMeter(getMeter(meterProvider, metricKey))
					.withAttributes(attributes)
					.withUnit(KNOWN_METRIC_UNITS.get(metricName))
					.withDescription(String.format("Reports metric %s", metricName))
					.build()
					.init();
			});

		// Schedule the flush task
		final ScheduledFuture<?> scheduledFuture = taskScheduler.schedule(meterProvider::forceFlush, trigger);

		// Save the delayed result-bearing action that can be cancelled
		schedules.put(String.format(METRICSHUB_RESOURCE_GROUP_KEY_FORMAT, resourceGroupKey), scheduledFuture);

		log.info("Resource Group {} scheduled.", resourceGroupKey);
	}

	/**
	 * Build this resource group metric meter
	 *
	 * @param sdkMeterProvider SDK implementation for {@link MeterProvider}
	 * @param metricName       the name of the metric in this {@link ResourceGroupConfig}
	 * @return Meter instruments used to record measurements
	 */
	private Meter getMeter(final SdkMeterProvider sdkMeterProvider, final String metricName) {
		return sdkMeterProvider.get(
			String.format("com.sentrysoftware.metricshub.resource.group.%s.%s", resourceGroupKey, metricName)
		);
	}
}