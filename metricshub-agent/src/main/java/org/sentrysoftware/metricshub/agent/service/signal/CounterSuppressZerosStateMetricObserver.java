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
import org.sentrysoftware.metricshub.engine.telemetry.metric.StateSetMetric;

/**
 * Observer for a counter state metric with suppression of zero values, extending {@link AbstractSuppressZerosStateMetricObserver}.
 * This observer initializes the metric and provides a callback for observing state changes.
 */
public class CounterSuppressZerosStateMetricObserver extends AbstractSuppressZerosStateMetricObserver {

	/**
	 * Constructs a {@code CounterSuppressZerosStateMetricObserver} with the specified parameters.
	 *
	 * @param meter       The OpenTelemetry meter to use for creating the metric.
	 * @param attributes  The attributes to associate with the metric.
	 * @param metricName  The name of the metric.
	 * @param unit        The unit of the metric.
	 * @param description The description of the metric.
	 * @param state       The state used to observe the metric. E.g. "ok".
	 * @param metric      The StateSetMetric to observe.
	 */
	@Builder(setterPrefix = "with")
	public CounterSuppressZerosStateMetricObserver(
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
