package org.sentrysoftware.metricshub.hardware.threshold;

import java.util.Map;
import java.util.Optional;

import org.sentrysoftware.metricshub.engine.telemetry.MetricFactory;

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

import org.sentrysoftware.metricshub.engine.telemetry.Monitor;
import org.sentrysoftware.metricshub.engine.telemetry.metric.NumberMetric;

/**
 * The FanMetricNormalizer class is responsible for normalizing FAN metrics.
 * It extends the AbstractMetricNormalizer class to provide specific
 * normalization logic for FAN monitor hardware metrics.
 */
public class FanMetricNormalizer extends AbstractMetricNormalizer {

	/**
	 * Constructs a new instance with the specified strategy time.
	 * @param strategyTime The strategy time in milliseconds
	 * @param hostname     The hostname of the monitor
	 */
	public FanMetricNormalizer(long strategyTime, String hostname) {
		super(strategyTime, hostname);
	}

	/**
	 * Normalizes fan speed limit metrics
	 * @param monitor A given {@link Monitor}
	 */
	@Override
	public void normalize(Monitor monitor) {
		normalizeSpeedLimitMetric(monitor, "hw.fan.speed.limit" , 500D , 0D );
		normalizeSpeedLimitMetric(monitor, "hw.fan.speed_ratio.limit" , 0.05D , 0D );
	}
	
	
	/**
	 * Normalizes the speed limit metrics.
	 * @param monitor The monitor to normalize
	 * @param metricPrefix The prefix of the metric name to be normalized.
	 * @param lowDegradedValueMetric The default value for the low degraded limit metric.
	 * @param lowCriticalValueMetric The default value for the low critical limit metric.
	 */
	public void normalizeSpeedLimitMetric(
			Monitor monitor,
			final String metricPrefix,
			final Double DefaultLowDegradedValueMetric ,
			final Double DefaultLowCriticalValueMetric 
	) {
		
		if (!isMetricCollected(monitor, metricPrefix.replace(".limit", ""))) {
			return;
		}

		// Get the degraded metric
		final Optional<NumberMetric> maybeLowDegradedMetric = findMetricByNamePrefixAndAttributes(
			monitor,
			metricPrefix,
			Map.of("limit_type", "low.degraded", "hw.type", monitor.getType())
		);

		// Get the critical metric
		final Optional<NumberMetric> maybeLowCriticalMetric = findMetricByNamePrefixAndAttributes(
			monitor,
			metricPrefix,
			Map.of("limit_type", "low.critical", "hw.type", monitor.getType())
		);

		// If neither degraded nor critical metrics are available, create both
		if (!maybeLowDegradedMetric.isPresent() && !maybeLowCriticalMetric.isPresent()) {
			final MetricFactory metricFactory = new MetricFactory(hostname);
			metricFactory.collectNumberMetric(
				monitor,
				String.format(metricPrefix + "{limit_type=\"low.degraded\", hw.type=\"%s\"}", monitor.getType()),
				DefaultLowDegradedValueMetric,
				strategyTime
			);
			metricFactory.collectNumberMetric(
					monitor,
					String.format(metricPrefix + "{limit_type=\"low.critical\", hw.type=\"%s\"}", monitor.getType()),
					DefaultLowCriticalValueMetric,
					strategyTime
				);
		} else if (maybeLowDegradedMetric.isPresent() && maybeLowCriticalMetric.isPresent()) {
			// If both the degraded and critical metrics are available, adjust the values
			final NumberMetric lowDegradedMetric = maybeLowDegradedMetric.get();
			final NumberMetric lowCriticalMetric = maybeLowCriticalMetric.get();

			final Double lowDegradedValue = lowDegradedMetric.getValue();
			final Double lowCriticalValue = lowCriticalMetric.getValue();

			// If the degraded value is smaller than the critical value, swap the values
			if (lowDegradedValue < lowCriticalValue) {
				final Double temp = lowDegradedValue;
				lowDegradedMetric.setValue(lowCriticalValue);
				lowCriticalMetric.setValue(temp);
			}
		}
		else if (!maybeLowDegradedMetric.isPresent() && maybeLowCriticalMetric.isPresent()) {
			// If only critical metric is available, adjust degraded metric
			final NumberMetric lowCriticalMetric = maybeLowCriticalMetric.get();
			final Double lowCriticalValue = lowCriticalMetric.getValue();
			final MetricFactory metricFactory = new MetricFactory(hostname);
			metricFactory.collectNumberMetric(
				monitor,
				String.format(metricPrefix + "{limit_type=\"low.degraded\", hw.type=\"%s\"}", monitor.getType()),
				lowCriticalValue * 1.1,
				strategyTime
			);
		}
		
		else if (maybeLowDegradedMetric.isPresent() && !maybeLowCriticalMetric.isPresent()) {
			// If only degraded metric is available, adjust critical metric
			final NumberMetric lowDegradedMetric = maybeLowDegradedMetric.get();
			final Double lowDegradedValue = lowDegradedMetric.getValue();
			final MetricFactory metricFactory = new MetricFactory(hostname);
			metricFactory.collectNumberMetric(
				monitor,
				String.format(metricPrefix + "{limit_type=\"low.critical\", hw.type=\"%s\"}", monitor.getType()),
				lowDegradedValue * 0.9,
				strategyTime
			);
		}
	}
}
