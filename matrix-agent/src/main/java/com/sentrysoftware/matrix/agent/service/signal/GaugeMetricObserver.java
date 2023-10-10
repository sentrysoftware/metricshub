package com.sentrysoftware.matrix.agent.service.signal;

import com.sentrysoftware.matrix.telemetry.metric.NumberMetric;
import io.opentelemetry.api.common.Attributes;
import io.opentelemetry.api.metrics.Meter;
import lombok.Builder;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.ToString;

@Data
@EqualsAndHashCode(callSuper = true)
@ToString(callSuper = true)
public class GaugeMetricObserver extends AbstractNumberMetricObserver {

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
