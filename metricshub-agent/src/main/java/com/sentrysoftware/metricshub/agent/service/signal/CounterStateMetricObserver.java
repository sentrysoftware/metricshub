package com.sentrysoftware.metricshub.agent.service.signal;

import com.sentrysoftware.metricshub.engine.telemetry.metric.StateSetMetric;
import io.opentelemetry.api.common.Attributes;
import io.opentelemetry.api.metrics.Meter;
import lombok.Builder;

public class CounterStateMetricObserver extends AbstractStateMetricObserver {

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
