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
import io.opentelemetry.api.metrics.ObservableDoubleMeasurement;
import java.util.Optional;
import lombok.EqualsAndHashCode;
import lombok.ToString;
import org.sentrysoftware.metricshub.engine.telemetry.metric.StateSetMetric;

/**
 * Abstract class for observing state-based metrics. Extends {@link AbstractMetricObserver}.
 * This class provides a common structure for classes that observe metrics associated with different states.
 * It extends {@link AbstractMetricObserver} to inherit common metric observation functionalities.
 */
@EqualsAndHashCode(callSuper = true)
@ToString(callSuper = true)
public abstract class AbstractStateMetricObserver extends AbstractMetricObserver {

	protected AbstractStateMetricObserver(
		final Meter meter,
		final Attributes attributes,
		final String metricName,
		final String unit,
		final String description,
		final String state,
		final StateSetMetric metric
	) {
		super(meter, attributes, metricName, unit, description);
		this.state = state;
		this.metric = metric;
	}

	protected String state;
	protected StateSetMetric metric;

	/**
	 * Observe the given state metric value
	 *
	 * @param recorder An interface for observing measurements with double values.
	 */
	protected void observeStateMetric(final ObservableDoubleMeasurement recorder) {
		getMetricValue().ifPresent(value -> recorder.record(value.equalsIgnoreCase(state) ? 1 : 0, attributes));
	}

	/**
	 * Get the metric value
	 *
	 * @return Optional of a {@link String} value
	 */
	public Optional<String> getMetricValue() {
		if (metric != null && metric.isUpdated()) {
			return Optional.ofNullable(metric.getValue());
		}

		return Optional.empty();
	}
}
