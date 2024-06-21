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
import java.util.concurrent.atomic.AtomicReference;

import org.sentrysoftware.metricshub.engine.telemetry.MetricFactory;
import org.sentrysoftware.metricshub.engine.telemetry.Monitor;
import org.sentrysoftware.metricshub.engine.telemetry.metric.AbstractMetric;
import org.sentrysoftware.metricshub.engine.telemetry.metric.NumberMetric;

import static org.sentrysoftware.metricshub.hardware.util.HwConstants.HW_ERRORS_LIMIT;

/**
 * An abstract class that provides methods for normalizing metrics in a monitoring system.
 * This class contains utility methods to check the availability of the monitor metrics and to adjust their values
 * based on specified conditions.
 */
public abstract class AbstractMetricNormalizer {

	/**
	 * Checks if all entries of the second map are contained in the first map.
	 * This method iterates through all entries of the second map and checks if each entry is present
	 * in the first map with the same key and value.
	 *
	 * @param firstMap  the map to be checked for containing all entries of the second map
	 * @param secondMap the map whose entries are to be checked against the first map
	 * @return {@code true} if all entries of the second map are contained in the first map,
	 * {@code false} otherwise
	 */
	static boolean containsAllEntries(Map<String, String> firstMap, Map<String, String> secondMap) {
		// Checks if the second map entries are all contained within the first map
		return secondMap
				.entrySet()
				.stream()
				.allMatch(entry -> firstMap.containsKey(entry.getKey()) && firstMap.get(entry.getKey()).equals(entry.getValue()));
	}

	/**
	 * Checks if a metric with the specified name and attributes is available.
	 * This method first extracts the metric name prefix and compares it with the provided prefix.
	 * If they do not match, the metric is considered unavailable. It then extracts the attributes
	 * from the metric name and checks if all the specified attributes are present in the extracted
	 * attributes.
	 *
	 * @param prefix     the prefix to compare against the extracted metric name prefix
	 * @param attributes the attributes to verify against the extracted attributes from the metric name
	 * @return {@code true} if the metric name has the specified prefix and contains all the specified attributes,
	 * {@code false} otherwise
	 */
	protected boolean isMetricAvailable(
			final Map<String, AbstractMetric> metrics,
			final String prefix,
			final Map<String, String> attributes,
			final AtomicReference<NumberMetric> matchingMetric
	) {
		return metrics.values().stream().anyMatch(metric -> {
			// Extract the metric name prefix
			final String metricNamePrefix = MetricFactory.extractName(metric.getName());

			if (!prefix.equals(metricNamePrefix)) {
				return false;
			}

			// Extract the metric attributes
			final Map<String, String> metricAttributes = MetricFactory.extractAttributesFromMetricName(metric.getName());

			// Check if all the attributes are available in the extracted metric attributes
			final boolean containsAllAttributes = containsAllEntries(metricAttributes, attributes);
			if (containsAllAttributes) {
				matchingMetric.set(NumberMetric.builder()
						.value(metric.getValue())
						.name(metric.getName())
						.collectTime(metric.getCollectTime())
						.attributes(metricAttributes)
						.build());
			}
			return containsAllAttributes;
		});
	}

	/**
	 * Adjusts the corresponding monitor's metric as follows:
	 * For example:
	 * If the hw.fan.speed.limit{limit_type="low.critical"} metric is not available while the hw.fan.speed.limit{limit_type="low.degraded"}
	 * metric is, set hw.fan.speed.limit{limit_type="low.critical"} to hw.fan.speed.limit{limit_type="low.degraded"} * 0.9.
	 * We need to manage the following use case as well:
	 * If the hw.fan.speed.limit{limit_type="low.critical"} metric is not available while the hw.fan.speed.limit{limit_type="low.degraded",
	 * unknown_attr="value"} metric is, set hw.fan.speed.limit{limit_type="low.critical", unknown_attr="value"} to
	 * hw.fan.speed.limit{limit_type="low.degraded", unknown_attr="value"} * 0.9.
	 *
	 * @param monitor A given {@link Monitor}
	 */
	public void normalize(final Monitor monitor) {
		AtomicReference<NumberMetric> matchingMetric = new AtomicReference<>();

		// Define the attributes for critical and degraded metrics
		final Map<String, String> criticalMetricAttributes = Map.of("limit_type", "low.critical");
		final Map<String, String> degradedMetricAttributes = Map.of("limit_type", "low.degraded");

		// Create an instance of MetricFactory
		final MetricFactory metricFactory = MetricFactory.builder().build();

		// Iterate over all metrics in the monitor

					// Check if a critical metric with the same name is available
					final boolean isCriticalMetricAvailable = isMetricAvailable(
							monitor.getMetrics(),
							HW_ERRORS_LIMIT,
							criticalMetricAttributes,
							matchingMetric
					);

					// Check if a degraded metric with the same name is available
					final boolean isDegradedMetricAvailable = isMetricAvailable(
							monitor.getMetrics(),
							HW_ERRORS_LIMIT,
							degradedMetricAttributes,
							matchingMetric
					);

					// If the critical metric is not available but the degraded metric is available,
					// create a new critical metric with adjusted value
					if (!isCriticalMetricAvailable && isDegradedMetricAvailable) {
						final String criticalMetricName = matchingMetric.get().getName().replace("low.degraded", "low.critical");

						// Collect the new critical metric with the value equals to the degraded metric value multiplied by 0.9
						metricFactory.collectNumberMetric(
								monitor,
								criticalMetricName,
								matchingMetric.get().getValue() * 0.9,
								System.currentTimeMillis()
						);
					}

	}

	/**
	 * Adjusts the metric hw.errors.limit.
	 *
	 * @param monitor A given {@link Monitor}
	 */
	public abstract void normalizeErrorsLimitMetric(Monitor monitor);

	/**
	 * Swaps the values of two metrics for a given monitor.
	 *
	 * @param monitor      the {@link Monitor} associated with the metrics
	 * @param firstMetric  the first metric to swap
	 * @param secondMetric the second metric to swap
	 */
	public void swapMetricsValues(
			final Monitor monitor,
			final NumberMetric firstMetric,
			final NumberMetric secondMetric
	) {
		// Use an auxiliary variable to save first metric value
		Double temp = 0.0;
		temp = firstMetric.getValue();
		// Create an instance of MetricFactory
		final MetricFactory metricFactory = MetricFactory.builder().build();
		// Collect the metric for the first metric name with the value of the second metric
		metricFactory.collectNumberMetric(
				monitor,
				firstMetric.getName(),
				secondMetric.getValue(),
				System.currentTimeMillis()
		);
		// Collect the metric for the second metric name with the value of the first metric
		metricFactory.collectNumberMetric(
				monitor,
				secondMetric.getName(),
				temp,
				System.currentTimeMillis()
		);
	}
}
