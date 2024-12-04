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
import org.sentrysoftware.metricshub.engine.telemetry.metric.NumberMetric;

/**
 * AbstractNumberMetricObserver is an extension of AbstractMetricObserver and serves as the base class
 * for defining generic number metric observers in the MetricsHub agent. It introduces functionality specific
 * to observing metrics with numerical values and is further extended by concrete number metric observer classes.
 *
 * <p>This class includes methods for observing number metrics, retrieving metric values, and handling special
 * cases such as energy metric observations where an increase in usage is checked.</p>
 */
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
