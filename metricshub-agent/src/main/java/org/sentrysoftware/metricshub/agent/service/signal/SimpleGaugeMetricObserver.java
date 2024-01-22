package org.sentrysoftware.metricshub.agent.service.signal;

import io.opentelemetry.api.common.Attributes;
import io.opentelemetry.api.metrics.Meter;
import lombok.Builder;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.ToString;

/**
 * A simple implementation of {@link AbstractMetricObserver} for observing
 * OpenTelemetry double gauge metrics. This observer records a pre-defined
 * metric value when initialized.
 */
@Data
@EqualsAndHashCode(callSuper = true)
@ToString(callSuper = true)
public class SimpleGaugeMetricObserver extends AbstractMetricObserver {

	private final Double metricValue;

	/**
	 * Constructs a new {@code SimpleGaugeMetricObserver} with the specified parameters.
	 *
	 * @param meter       The OpenTelemetry meter to use for metric recording.
	 * @param metricName  The name of the metric.
	 * @param unit        The unit of the metric.
	 * @param description The description of the metric.
	 * @param attributes  The additional attributes associated with the metric.
	 * @param metricValue The pre-defined metric value to be recorded.
	 */
	@Builder(setterPrefix = "with")
	public SimpleGaugeMetricObserver(
		final Meter meter,
		final String metricName,
		final String unit,
		final String description,
		final Attributes attributes,
		final Double metricValue
	) {
		super(meter, attributes, metricName, unit, description);
		this.metricValue = metricValue;
	}

	@Override
	public void init() {
		newDoubleGaugeBuilder().buildWithCallback(recorder -> recorder.record(metricValue, attributes));
	}
}
