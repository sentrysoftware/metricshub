package org.sentrysoftware.metricshub.agent.service.signal;

import io.opentelemetry.api.common.Attributes;
import io.opentelemetry.api.metrics.DoubleCounterBuilder;
import io.opentelemetry.api.metrics.DoubleGaugeBuilder;
import io.opentelemetry.api.metrics.DoubleUpDownCounterBuilder;
import io.opentelemetry.api.metrics.Meter;
import lombok.AllArgsConstructor;
import lombok.Data;

/**
 * AbstractMetricObserver is the base class for defining generic metric observers in the MetricsHub agent.
 * It provides common functionality and methods for initializing metric observers, as well as creating
 * OpenTelemetry SDK builders.
 */
@AllArgsConstructor
@Data
public abstract class AbstractMetricObserver {

	protected final Meter meter;
	protected final Attributes attributes;
	protected final String metricName;
	protected final String unit;
	protected final String description;

	/**
	 * Initialize the observer
	 */
	public abstract void init();

	/**
	 * Create a new OpenTelemetry SDK {@link DoubleCounterBuilder} instance
	 *
	 * @return {@link DoubleCounterBuilder} instance
	 */
	protected DoubleCounterBuilder newDoubleCounterBuilder() {
		final DoubleCounterBuilder builder = meter.counterBuilder(metricName).setDescription(description).ofDoubles();

		// Set the unit if it is available
		if (unit != null && !unit.isBlank()) {
			builder.setUnit(unit);
		}

		return builder;
	}

	/**
	 * Create a new OpenTelemetry SDK {@link DoubleGaugeBuilder} instance
	 *
	 * @return {@link DoubleGaugeBuilder} instance
	 */
	protected DoubleGaugeBuilder newDoubleGaugeBuilder() {
		final DoubleGaugeBuilder builder = meter.gaugeBuilder(metricName).setDescription(description);

		// Set the unit if it is available
		if (unit != null && !unit.isBlank()) {
			builder.setUnit(unit);
		}
		return builder;
	}

	/**
	 * Create a new OpenTelemetry SDK {@link DoubleUpDownCounterBuilder} instance
	 *
	 * @return {@link DoubleUpDownCounterBuilder} instance
	 */
	protected DoubleUpDownCounterBuilder newDoubleUpDownCounterBuilder() {
		final DoubleUpDownCounterBuilder builder = meter
			.upDownCounterBuilder(metricName)
			.setDescription(description)
			.ofDoubles();

		// Set the unit if it is available
		if (unit != null && !unit.isBlank()) {
			builder.setUnit(unit);
		}
		return builder;
	}
}
