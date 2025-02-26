package org.sentrysoftware.metricshub.agent.opentelemetry.metric;

/*-
 * ╱╲╱╲╱╲╱╲╱╲╱╲╱╲╱╲╱╲╱╲╱╲╱╲╱╲╱╲╱╲╱╲╱╲╱╲╱╲╱╲
 * MetricsHub Agent
 * ჻჻჻჻჻჻
 * Copyright 2023 - 2025 Sentry Software
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

import lombok.Builder;
import lombok.Getter;
import lombok.NonNull;
import org.sentrysoftware.metricshub.agent.config.StateSetMetricCompression;
import org.sentrysoftware.metricshub.engine.connector.model.metric.MetricType;

/**
 * Metric context class used to store metric information. (Type, unit, description, etc.)
 */
@Builder(setterPrefix = "with")
public class MetricContext {

	@Getter
	@NonNull
	private final MetricType type;

	@Getter
	private final String unit;

	@Getter
	private final String description;

	@Getter
	private final boolean isSuppressZerosCompression;

	/**
	 * Checks if the given compression is suppress zeros.
	 *
	 * @param compression the compression to check.
	 * @return true if the compression is suppress zeros, false otherwise.
	 */
	public static boolean isSuppressZerosCompression(final String compression) {
		return StateSetMetricCompression.SUPPRESS_ZEROS.equalsIgnoreCase(compression);
	}
}
