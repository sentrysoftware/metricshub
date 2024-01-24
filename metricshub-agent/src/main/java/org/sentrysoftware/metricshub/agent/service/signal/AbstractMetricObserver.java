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
