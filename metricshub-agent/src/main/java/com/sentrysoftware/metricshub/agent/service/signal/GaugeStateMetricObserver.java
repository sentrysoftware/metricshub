package com.sentrysoftware.metricshub.agent.service.signal;

import com.sentrysoftware.metricshub.engine.telemetry.metric.StateSetMetric;
import io.opentelemetry.api.common.Attributes;
import io.opentelemetry.api.metrics.Meter;
import lombok.Builder;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.ToString;

/**
 * A metric observer for gauge state metrics.
 */
@Data
@EqualsAndHashCode(callSuper = true)
@ToString(callSuper = true)
public class GaugeStateMetricObserver extends AbstractStateMetricObserver {

	/**
	 * Constructs a GaugeStateMetricObserver with the specified parameters.
	 *
	 * @param meter       the meter to which the metric belongs
	 * @param attributes  the attributes associated with the metric
	 * @param metricName  the name of the metric
	 * @param unit        the unit of the metric
	 * @param description the description of the metric
	 * @param state       the state of the metric
	 * @param metric      the gauge state metric to observe
	 */
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
