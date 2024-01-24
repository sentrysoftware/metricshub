package org.sentrysoftware.metricshub.agent.service.signal;

/*-
 * ╱╲╱╲╱╲╱╲╱╲╱╲╱╲╱╲╱╲╱╲╱╲╱╲╱╲╱╲╱╲╱╲╱╲╱╲╱╲╱╲
 * MetricsHub Agent
 * ჻჻჻჻჻჻
 * Copyright 2023 - 2024 Sentry Software
 * ჻჻჻჻჻჻
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Affero General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU Affero General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 * ╲╱╲╱╲╱╲╱╲╱╲╱╲╱╲╱╲╱╲╱╲╱╲╱╲╱╲╱╲╱╲╱╲╱╲╱╲╱╲╱
 */

import io.opentelemetry.api.common.Attributes;
import io.opentelemetry.api.metrics.Meter;
import lombok.Builder;
import org.sentrysoftware.metricshub.engine.telemetry.metric.NumberMetric;

/**
 * CounterMetricObserver is an observer for OpenTelemetry counter metrics. It extends
 * AbstractNumberMetricObserver and is responsible for initializing and observing
 * counter metrics.
 *
 * <p>This class provides a builder pattern for convenient instantiation and configuration
 * of counter metric observers.</p>
 *
 * @see AbstractNumberMetricObserver
 */
public class CounterMetricObserver extends AbstractNumberMetricObserver {

	/**
	 * Constructs a new {@code CounterMetricObserver} with the specified parameters.
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
