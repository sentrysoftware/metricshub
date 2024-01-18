package com.sentrysoftware.metricshub.agent.service.signal;

import com.sentrysoftware.metricshub.engine.telemetry.metric.NumberMetric;
import io.opentelemetry.api.common.Attributes;
import io.opentelemetry.api.metrics.Meter;
import lombok.Builder;

/**
 * CounterMetricObserver is an observer for OpenTelemetry counter metrics. It extends
 * AbstractNumberMetricObserver and is responsible for initializing and observing
 * counter metrics.
 *
 * <p>This class provides a builder pattern for convenient instantiation and configuration
 * of counter metric observers.
 *
 * @see AbstractNumberMetricObserver
 */
public class CounterMetricObserver extends AbstractNumberMetricObserver {

	/**
	 * Constructs a new CounterMetricObserver with the specified parameters.
	 *
	 * @param meter       The OpenTelemetry Meter used for metric creation.
	 * @param attributes  The attributes associated with the metric.
	 * @param metricName  The name of the metric.
	 * @param unit        The unit of the metric.
	 * @param description A description of the metric.
	 * @param metric      The NumberMetric instance associated with the observer.
	 */
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

	/**
	 * Initializes the counter metric observer. It sets up the OpenTelemetry
	 * double counter builder and builds it with the provided callback for
	 * observing number metrics.
	 */
	@Override
	public void init() {
		newDoubleCounterBuilder().buildWithCallback(super::observeNumberMetric);
	}
}
