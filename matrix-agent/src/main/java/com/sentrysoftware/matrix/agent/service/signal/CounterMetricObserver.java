package com.sentrysoftware.matrix.agent.service.signal;

import com.sentrysoftware.matrix.telemetry.metric.NumberMetric;
import io.opentelemetry.api.common.Attributes;
import io.opentelemetry.api.metrics.Meter;
import lombok.Builder;

public class CounterMetricObserver extends AbstractNumberMetricObserver {

	@Builder(setterPrefix = "with")
	public CounterMetricObserver(
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
		newDoubleCounterBuilder().buildWithCallback(super::observeNumberMetric);
	}
}
