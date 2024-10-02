package org.sentrysoftware.metricshub.hardware.threshold;

/*-
 * ╱╲╱╲╱╲╱╲╱╲╱╲╱╲╱╲╱╲╱╲╱╲╱╲╱╲╱╲╱╲╱╲╱╲╱╲╱╲╱╲
 * MetricsHub Hardware Energy and Sustainability Module
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

import java.util.Map;
import java.util.Optional;
import org.sentrysoftware.metricshub.engine.telemetry.Monitor;
import org.sentrysoftware.metricshub.engine.telemetry.metric.NumberMetric;

/**
 * The VoltageMetricNormalizer class is responsible for normalizing voltage metrics.
 * It extends the AbstractMetricNormalizer class to provide specific
 * normalization logic for voltage monitor hardware metrics.
 */
public class VoltageMetricNormalizer extends AbstractMetricNormalizer {

	/**
	 * Constructs new instance of VoltageMetricNormalizer with the specified strategy time.
	 * @param strategyTime The strategy time in milliseconds
	 * @param hostname     The hostname of the monitor
	 */
	public VoltageMetricNormalizer(long strategyTime, String hostname) {
		super(strategyTime, hostname);
	}

	/**
	 * Normalizes the metrics of the given monitor.
	 * @param monitor The monitor containing the metrics to be normalized
	 */
	@Override
	public void normalize(Monitor monitor) {
		normalizeVoltageLimitMetric(monitor, "hw.voltage");
	}

	/**
	 * Normalizes the speed limit metrics.
	 * @param monitor The monitor to normalize
	 * @param metricNamePrefix The prefix of the metric name.
	 */
	private void normalizeVoltageLimitMetric(final Monitor monitor, final String metricNamePrefix) {
		if (!isMetricCollected(monitor, metricNamePrefix)) {
			return;
		}

		// Get the high critical metric
		final Optional<NumberMetric> maybeHighCriticaldMetric = findMetricByNamePrefixAndAttributes(
			monitor,
			String.format("%s.limit", metricNamePrefix),
			Map.of("limit_type", "high.critical")
		);

		// Get the low critical metric
		final Optional<NumberMetric> maybeLowCriticalMetric = findMetricByNamePrefixAndAttributes(
			monitor,
			String.format("%s.limit", metricNamePrefix),
			Map.of("limit_type", "low.critical")
		);

		if (maybeHighCriticaldMetric.isPresent() && maybeLowCriticalMetric.isPresent()) {
			// Adjust values if both metrics are present
			swapIfFirstLessThanSecond(maybeHighCriticaldMetric.get(), maybeLowCriticalMetric.get());
		} else if (maybeHighCriticaldMetric.isPresent()) {
			// Create low critical metric if only high critical is present
			final NumberMetric highCriticalMetric = maybeHighCriticaldMetric.get();
			final Double maybeLowCriticalMetricValue = highCriticalMetric.getValue();
			final String lowCriticalMetricName = replaceLimitType(
				highCriticalMetric.getName(),
				"limit_type=\"low.critical\""
			);
			Double lowCriticalMetricValue = maybeLowCriticalMetricValue;
			final Double maybeHighCriticalMetricValue = maybeLowCriticalMetricValue * 1.1;
			Double highCriticalMetricValue = maybeHighCriticalMetricValue;

			if (lowCriticalMetricValue <= 0) {
				highCriticalMetricValue = maybeLowCriticalMetricValue;
				lowCriticalMetricValue = maybeHighCriticalMetricValue;
			}

			highCriticalMetric.setValue(highCriticalMetricValue);
			collectMetric(monitor, lowCriticalMetricName, lowCriticalMetricValue);
		} else if (maybeLowCriticalMetric.isPresent()) {
			// Create high critical metric if only low critical is present
			final NumberMetric lowCriticalMetric = maybeLowCriticalMetric.get();
			final Double maybeHighCriticalMetricValue = lowCriticalMetric.getValue();
			final String highCriticalMetricName = replaceLimitType(
				lowCriticalMetric.getName(),
				"limit_type=\"high.critical\""
			);
			Double highCriticalMetricValue = maybeHighCriticalMetricValue;
			final Double maybeLowCriticalMetricValue = maybeHighCriticalMetricValue * 0.9;
			Double lowCriticalMetricValue = maybeLowCriticalMetricValue;

			if (highCriticalMetricValue <= 0) {
				highCriticalMetricValue = maybeLowCriticalMetricValue;
				lowCriticalMetricValue = maybeHighCriticalMetricValue;
			}

			lowCriticalMetric.setValue(lowCriticalMetricValue);
			collectMetric(monitor, highCriticalMetricName, highCriticalMetricValue);
		}
	}
}
