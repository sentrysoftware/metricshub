package com.sentrysoftware.matrix.agent.service.signal;

import com.sentrysoftware.matrix.agent.config.ResourceGroupConfig;
import com.sentrysoftware.matrix.agent.helper.OtelHelper;
import io.opentelemetry.api.common.Attributes;
import io.opentelemetry.api.metrics.DoubleGaugeBuilder;
import io.opentelemetry.api.metrics.Meter;
import io.opentelemetry.sdk.metrics.SdkMeterProvider;
import java.util.Map;
import java.util.Objects;
import lombok.Builder;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.ToString;

@Data
@EqualsAndHashCode(callSuper = true)
@ToString(callSuper = true)
public class ResourceGroupMetricsObserver extends AbstractObserver {

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

	private final ResourceGroupConfig resourceGroupConfig;
	private final String resourceGroupKey;

	@Builder
	public ResourceGroupMetricsObserver(
		final SdkMeterProvider sdkMeterProvider,
		final String resourceGroupKey,
		final ResourceGroupConfig resourceGroupConfig
	) {
		super(sdkMeterProvider);
		this.resourceGroupKey = resourceGroupKey;
		this.resourceGroupConfig = resourceGroupConfig;
	}

	@Override
	public void init() {
		final Attributes resourceGroupAttributes = OtelHelper.buildOtelAttributesFromMap(
			resourceGroupConfig.getAttributes()
		);

		final Map<String, Double> userMetrics = resourceGroupConfig.getMetrics();
		userMetrics
			.entrySet()
			.stream()
			.filter(metricEntry -> Objects.nonNull(metricEntry.getValue()))
			.forEach(metricEntry -> {
				final String metricName = metricEntry.getKey();
				final DoubleGaugeBuilder builder = getMeter(metricName)
					.gaugeBuilder(metricName)
					.setDescription(String.format("Reports metric %s", metricName));

				// Get the known unit for this metric
				final String unit = KNOWN_METRIC_UNITS.get(metricName);
				if (unit != null) {
					builder.setUnit(unit);
				}

				builder.buildWithCallback(recorder -> recorder.record(metricEntry.getValue(), resourceGroupAttributes));
			});
	}

	/**
	 * Build this resource group metric meter
	 *
	 * @param metricName the name of the metric in this {@link ResourceGroupConfig}
	 * @return Meter instruments used to record measurements
	 */
	private Meter getMeter(final String metricName) {
		return sdkMeterProvider.get(
			String.format("com.sentrysoftware.metricshub.resource.group.%s.%s", resourceGroupKey, metricName)
		);
	}
}
