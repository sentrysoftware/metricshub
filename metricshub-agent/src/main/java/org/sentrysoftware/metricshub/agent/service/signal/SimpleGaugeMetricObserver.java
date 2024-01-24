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
