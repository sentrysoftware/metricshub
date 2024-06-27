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
import org.sentrysoftware.metricshub.engine.telemetry.MetricFactory;
import org.sentrysoftware.metricshub.engine.telemetry.Monitor;
import org.sentrysoftware.metricshub.engine.telemetry.metric.NumberMetric;

/**
 * The FanMetricNormalizer class is responsible for normalizing FAN metrics.
 * It extends the AbstractMetricNormalizer class to provide specific
 * normalization logic for FAN monitor hardware metrics.
 */
public class FanMetricNormalizer extends AbstractMetricNormalizer {

	private static final Double DEFAULT_LOW_DEGRADED_VALUE_SPEED_LIMIT_METRIC = 500.0;
	private static final Double DEFAULT_LOW_CRITICAL_VALUE_SPEED_LIMIT_METRIC = 0.0;
	private static final Double DEFAULT_LOW_DEGRADED_VALUE_SPEED_RATIO_LIMIT_METRIC = 0.05;
	private static final Double DEFAULT_LOW_CRITICAL_VALUE_SPEED_RATIO_LIMIT_METRIC = 0.0;

	/**
	 * Constructs a new instance with the specified strategy time.
	 * @param strategyTime The strategy time in milliseconds
	 * @param hostname The hostname of the monitor
	 */
	public FanMetricNormalizer(long strategyTime, String hostname) {
		super(strategyTime, hostname);
	}

	/**
	 * Normalizes fan speed and speed ratio limit metrics
	 * @param monitor A given {@link Monitor}
	 */
	@Override
	public void normalize(Monitor monitor) {
		normalizeSpeedLimitMetric(
			monitor,
			"hw.fan.speed.limit",
			DEFAULT_LOW_DEGRADED_VALUE_SPEED_LIMIT_METRIC,
			DEFAULT_LOW_CRITICAL_VALUE_SPEED_LIMIT_METRIC
		);
		normalizeSpeedLimitMetric(
			monitor,
			"hw.fan.speed_ratio.limit",
			DEFAULT_LOW_DEGRADED_VALUE_SPEED_RATIO_LIMIT_METRIC,
			DEFAULT_LOW_CRITICAL_VALUE_SPEED_RATIO_LIMIT_METRIC
		);
	}

	/**
	 * Normalizes the speed limit metrics.
	 * @param monitor The monitor to normalize
	 * @param metricNamePrefix The prefix of the metric name.
	 * @param defaultLowDegradedValueMetric The default value for the low degraded limit metric.
	 * @param defaultLowCriticalValueMetric The default value for the low critical limit metric.
	 */
	public void normalizeSpeedLimitMetric(
		Monitor monitor,
		final String metricNamePrefix,
		final Double defaultLowDegradedValueMetric,
		final Double defaultLowCriticalValueMetric
	) {
		if (!isMetricCollected(monitor, metricNamePrefix.replace(".limit", ""))) {
			return;
		}

		// Get the low degraded metric
		final Optional<NumberMetric> maybeLowDegradedMetric = findMetricByNamePrefixAndAttributes(
			monitor,
			metricNamePrefix,
			Map.of("limit_type", "low.degraded")
		);

		// Get the low critical metric
		final Optional<NumberMetric> maybeLowCriticalMetric = findMetricByNamePrefixAndAttributes(
			monitor,
			metricNamePrefix,
			Map.of("limit_type", "low.critical")
		);

		// If neither low degraded nor low critical metrics are available, create both
		if (!maybeLowDegradedMetric.isPresent() && !maybeLowCriticalMetric.isPresent()) {
			final MetricFactory metricFactory = new MetricFactory(hostname);
			metricFactory.collectNumberMetric(
				monitor,
				String.format("%s{limit_type=\"low.degraded\"}", metricNamePrefix),
				defaultLowDegradedValueMetric,
				strategyTime
			);
			metricFactory.collectNumberMetric(
				monitor,
				String.format("%s{limit_type=\"low.critical\"}", metricNamePrefix),
				defaultLowCriticalValueMetric,
				strategyTime
			);
		} else if (maybeLowDegradedMetric.isPresent() && maybeLowCriticalMetric.isPresent()) {
			// If both the low degraded and low critical metrics are available, adjust the values
			final NumberMetric lowDegradedMetric = maybeLowDegradedMetric.get();
			final NumberMetric lowCriticalMetric = maybeLowCriticalMetric.get();

			final Double lowDegradedValue = lowDegradedMetric.getValue();
			final Double lowCriticalValue = lowCriticalMetric.getValue();

			// If the low degraded value is smaller than the low critical value, swap the values
			if (lowDegradedValue < lowCriticalValue) {
				final Double temp = lowDegradedValue;
				lowDegradedMetric.setValue(lowCriticalValue);
				lowCriticalMetric.setValue(temp);
			}
		} else if (!maybeLowDegradedMetric.isPresent() && maybeLowCriticalMetric.isPresent()) {
			// If only low critical metric is available, adjust low degraded metric
			final NumberMetric lowCriticalMetric = maybeLowCriticalMetric.get();
			final Double lowCriticalValue = lowCriticalMetric.getValue();
			final MetricFactory metricFactory = new MetricFactory(hostname);
			metricFactory.collectNumberMetric(
				monitor,
				String.format("%s{limit_type=\"low.degraded\"}", metricNamePrefix),
				lowCriticalValue * 1.1,
				strategyTime
			);
		} else if (maybeLowDegradedMetric.isPresent() && !maybeLowCriticalMetric.isPresent()) {
			// If only low degraded metric is available, adjust low critical metric
			final NumberMetric lowDegradedMetric = maybeLowDegradedMetric.get();
			final Double lowDegradedValue = lowDegradedMetric.getValue();
			final MetricFactory metricFactory = new MetricFactory(hostname);
			metricFactory.collectNumberMetric(
				monitor,
				String.format("%s{limit_type=\"low.critical\"}", metricNamePrefix),
				lowDegradedValue * 0.9,
				strategyTime
			);
		}
	}
}
