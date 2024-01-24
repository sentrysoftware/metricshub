package org.sentrysoftware.metricshub.agent.service.signal;

import io.opentelemetry.api.common.Attributes;
import io.opentelemetry.api.metrics.Meter;
import lombok.Builder;
import org.sentrysoftware.metricshub.engine.telemetry.metric.NumberMetric;

/**
 * Observer for UpDownCounter metrics. Extends
 * {@link AbstractNumberMetricObserver}.
 */
public class UpDownCounterMetricObserver extends AbstractNumberMetricObserver {

	/**
	 * Constructs a new {@code UpDownCounterMetricObserver} with the specified parameters.
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
