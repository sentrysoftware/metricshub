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
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.ToString;
import org.sentrysoftware.metricshub.engine.telemetry.metric.NumberMetric;

/**
 * A metric observer for gauge metrics.
 */
@Data
@EqualsAndHashCode(callSuper = true)
@ToString(callSuper = true)
public class GaugeMetricObserver extends AbstractNumberMetricObserver {

	/**
	 * Constructs a new {@code GaugeMetricObserver} with the specified parameters.
	 *
	 * @param meter       the meter to which the metric belongs
	 * @param attributes  the attributes associated with the metric
	 * @param metricName  the name of the metric
	 * @param unit        the unit of the metric
	 * @param description the description of the metric
	 * @param metric      the gauge metric to observe
	 */
	@Builder(setterPrefix = "with")
	public GaugeMetricObserver(
		final Meter meter,
		final Attributes attributes,
		final String metricName,
		final String unit,
		final String description,
		final NumberMetric metric
	) {
		super(meter, attributes, metricName, unit, description, metric);
	}

	@Override
	public void init() {
		newDoubleGaugeBuilder().buildWithCallback(super::observeNumberMetric);
	}
}
