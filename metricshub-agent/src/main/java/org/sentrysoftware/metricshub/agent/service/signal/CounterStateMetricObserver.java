package org.sentrysoftware.metricshub.agent.service.signal;

import io.opentelemetry.api.common.Attributes;
import io.opentelemetry.api.metrics.Meter;
import lombok.Builder;
import org.sentrysoftware.metricshub.engine.telemetry.metric.StateSetMetric;

/**
 * Observer for a counter state metric, extending {@link AbstractStateMetricObserver}.
 * This observer initializes the metric and provides a callback for observing state changes.
 */
public class CounterStateMetricObserver extends AbstractStateMetricObserver {

	/**
	 * Constructs a {@code CounterStateMetricObserver} with the specified parameters.
	 *
	 * @param meter        the OpenTelemetry meter
	 * @param attributes   the attributes associated with the metric
	 * @param metricName   the name of the metric
	 * @param unit         the unit of the metric
	 * @param description  the description of the metric
	 * @param state        the initial state of the metric
	 * @param metric       the state set metric
	 */
	@Builder(setterPrefix = "with")
	public CounterStateMetricObserver(
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
		newDoubleCounterBuilder().buildWithCallback(super::observeStateMetric);
	}
}
