package com.sentrysoftware.metricshub.agent.service.signal;

import com.sentrysoftware.metricshub.engine.telemetry.metric.NumberMetric;
import io.opentelemetry.api.common.Attributes;
import io.opentelemetry.api.metrics.Meter;
import lombok.Builder;

/**
 * Observer for UpDownCounter metrics. Extends
 * {@link AbstractNumberMetricObserver}.
 */
public class UpDownCounterMetricObserver extends AbstractNumberMetricObserver {

	/**
	 * Constructs an instance of UpDownCounterMetricObserver.
	 *
	 * @param meter       The OpenTelemetry meter to use for creating the metric.
	 * @param attributes  The attributes to associate with the metric.
	 * @param metricName  The name of the metric.
	 * @param unit        The unit of the metric.
	 * @param description The description of the metric.
	 * @param metric      The NumberMetric to observe.
	 */
	@Builder(setterPrefix = "with")
	public UpDownCounterMetricObserver(
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
		newDoubleUpDownCounterBuilder().buildWithCallback(super::observeNumberMetric);
	}
}
