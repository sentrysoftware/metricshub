package com.sentrysoftware.metricshub.agent.service.signal;

import com.sentrysoftware.metricshub.engine.telemetry.metric.StateSetMetric;
import io.opentelemetry.api.common.Attributes;
import io.opentelemetry.api.metrics.Meter;
import lombok.Builder;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.ToString;

@Data
@EqualsAndHashCode(callSuper = true)
@ToString(callSuper = true)
public class GaugeStateMetricObserver extends AbstractStateMetricObserver {

	@Builder(setterPrefix = "with")
	public GaugeStateMetricObserver(
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
		newDoubleGaugeBuilder().buildWithCallback(super::observeStateMetric);
	}
}
