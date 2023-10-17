package com.sentrysoftware.metricshub.agent.service.signal;

import com.sentrysoftware.metricshub.engine.telemetry.metric.NumberMetric;
import io.opentelemetry.api.common.Attributes;
import io.opentelemetry.api.metrics.Meter;
import io.opentelemetry.api.metrics.ObservableDoubleMeasurement;
import java.util.Optional;
import lombok.EqualsAndHashCode;
import lombok.ToString;

@EqualsAndHashCode(callSuper = true)
@ToString(callSuper = true)
public abstract class AbstractNumberMetricObserver extends AbstractMetricObserver {

	protected AbstractNumberMetricObserver(
		final Meter meter,
		final Attributes attributes,
		final String metricName,
		final String unit,
		final String description,
		final NumberMetric metric
	) {
		super(meter, attributes, metricName, unit, description);
		this.metric = metric;
	}

	protected NumberMetric metric;

	/**
	 * Observe the given metric value
	 *
	 * @param recorder An interface for observing measurements with double values.
	 */
	protected void observeNumberMetric(final ObservableDoubleMeasurement recorder) {
		getMetricValue()
			.ifPresent(value -> {
				// Special case for energy that must be increased
				if ("hw.energy".equals(metricName) && !increasedEnergyUsage(value)) {
					return;
				}

				recorder.record(value, attributes);
			});
	}

	/**
	 * Get the metric value
	 *
	 * @return Optional of a {@link Double} value
	 */
	public Optional<Double> getMetricValue() {
		if (metric != null && metric.isUpdated()) {
			return Optional.ofNullable(metric.getValue());
		}

		return Optional.empty();
	}

	/**
	 * Return true if the given energy metric value has increased its usage. Means the current energy value
	 * is greater than the previous one.
	 *
	 * @param energy Energy in joules
	 *
	 * @return boolean value
	 */
	boolean increasedEnergyUsage(final Double energy) {
		final Double previousValue = metric.getPreviousValue();

		// This is the first time energy is collected
		if (previousValue == null) {
			return true;
		}

		return energy > previousValue;
	}
}
