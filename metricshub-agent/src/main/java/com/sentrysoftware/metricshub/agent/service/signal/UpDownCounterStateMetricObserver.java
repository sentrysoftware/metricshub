package com.sentrysoftware.metricshub.agent.service.signal;

import com.sentrysoftware.metricshub.engine.telemetry.metric.StateSetMetric;
import io.opentelemetry.api.common.Attributes;
import io.opentelemetry.api.metrics.Meter;
import lombok.Builder;

/**
 * Observer for UpDownCounter state metrics.
 * Extends {@link AbstractStateMetricObserver}.
 */
public class UpDownCounterStateMetricObserver extends AbstractStateMetricObserver {

	/**
	 * Constructs an instance of UpDownCounterStateMetricObserver.
	 *
	 * @param meter       The OpenTelemetry meter to use for creating the metric.
	 * @param attributes  The attributes to associate with the metric.
	 * @param metricName  The name of the metric.
	 * @param unit        The unit of the metric.
	 * @param description The description of the metric.
	 * @param state       The initial state of the metric.
	 * @param metric      The StateSetMetric to observe.
	 */
	@Builder(setterPrefix = "with")
	public UpDownCounterStateMetricObserver(
		final Meter meter,
		final Attributes attributes,
		final String metricName,
		final String unit,
		final String description,
		final String state,
		final StateSetMetric metric
	) {
		super(meter, attributes, metricName, unit, description, state, metric);
	}

	@Override
	public void init() {
		newDoubleUpDownCounterBuilder().buildWithCallback(super::observeStateMetric);
	}
}
