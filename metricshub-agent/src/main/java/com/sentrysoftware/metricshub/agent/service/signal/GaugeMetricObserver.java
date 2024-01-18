package com.sentrysoftware.metricshub.agent.service.signal;

import com.sentrysoftware.metricshub.engine.telemetry.metric.NumberMetric;
import io.opentelemetry.api.common.Attributes;
import io.opentelemetry.api.metrics.Meter;
import lombok.Builder;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.ToString;

/**
 * A metric observer for gauge metrics.
 */
@Data
@EqualsAndHashCode(callSuper = true)
@ToString(callSuper = true)
public class GaugeMetricObserver extends AbstractNumberMetricObserver {

	/**
	 * Constructs a new {@code GaugeMetricObserver} with the specified parameters.
	 *
	 * @param meter       the meter to which the metric belongs
	 * @param attributes  the attributes associated with the metric
	 * @param metricName  the name of the metric
	 * @param unit        the unit of the metric
	 * @param description the description of the metric
	 * @param metric      the gauge metric to observe
	 */
	@Builder(setterPrefix = "with")
	public GaugeMetricObserver(
		final Meter meter,
		final Attributes attributes,
		final String metricName,
		final String unit,
		final String description,
		final NumberMetric metric
	) {
		super(meter, attributes, metricName, unit, description, metric);
	}

	@Override
	public void init() {
		newDoubleGaugeBuilder().buildWithCallback(super::observeNumberMetric);
	}
}
