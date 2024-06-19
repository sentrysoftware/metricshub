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
import org.sentrysoftware.metricshub.engine.telemetry.MetricFactory;
import org.sentrysoftware.metricshub.engine.telemetry.metric.AbstractMetric;

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
	 * @param firstMap the map to be checked for containing all entries of the second map
	 * @param secondMap the map whose entries are to be checked against the first map
	 * @return {@code true} if all entries of the second map are contained in the first map,
	 *         {@code false} otherwise
	 */
	private boolean containsAllEntries(Map<String, String> firstMap, Map<String, String> secondMap) {
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
	 * @param metricName the name of the metric to check
	 * @param prefix the prefix to compare against the extracted metric name prefix
	 * @param attributes the attributes to verify against the extracted attributes from the metric name
	 * @return {@code true} if the metric name has the specified prefix and contains all the specified attributes,
	 *         {@code false} otherwise
	 */
	protected boolean isMetricAvailable(
		final String metricName,
		final String prefix,
		final Map<String, String> attributes
	) {
		// Extract the metric name prefix
		final String metricNamePrefix = MetricFactory.extractName(metricName);

		if (!prefix.equals(metricNamePrefix)) {
			return false;
		}

		// Extract the metric attributes
		final Map<String, String> metricAttributes = MetricFactory.extractAttributesFromMetricName(metricName);

		// Check all the attributes are available in the extracted metric attributes
		return containsAllEntries(metricAttributes, attributes);
	}

	/**
	 * Adjusts the corresponding monitor's metric as follows:
	 * If the hw.MONITOR_TYPE.METRIC{limit_type="low.critical"} metric is not available while the  hw.MONITOR_TYPE.METRIC{limit_type="low.degraded"}
	 * metric is, set hw.MONITOR_TYPE.METRIC{limit_type="low.critical"} to hw.MONITOR_TYPE.METRIC{limit_type="low.degraded"} * 0.9.
	 * We need to manage the following use case as well:
	 * If the hw.MONITOR_TYPE.METRIC{limit_type="low.critical"} metric is not available while the hw.MONITOR_TYPE.METRIC{limit_type="low.degraded",
	 * unknown_attr="value"} metric is, set hw.MONITOR_TYPE.METRIC{limit_type="low.critical", unknown_attr="value"} to
	 * hw.MONITOR_TYPE.METRIC{limit_type="low.degraded", unknown_attr="value"} * 0.9.
	 * @param metric A given monitor's Number metric
	 */
	public abstract void normalize(AbstractMetric metric);

	/**
	 * Adjusts the metric hw.errors.limit.
	 * @param metric A given monitor's Number metric
	 */
	public abstract void normalizeErrorsLimitMetric(AbstractMetric metric);
}
