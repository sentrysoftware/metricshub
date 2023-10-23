package com.sentrysoftware.metricshub.engine.strategy.utils;

import com.sentrysoftware.metricshub.engine.telemetry.Monitor;
import com.sentrysoftware.metricshub.engine.telemetry.metric.NumberMetric;
import com.sentrysoftware.metricshub.engine.telemetry.metric.StateSetMetric;
import lombok.AccessLevel;
import lombok.NoArgsConstructor;

@NoArgsConstructor(access = AccessLevel.PRIVATE)
public class CollectHelper {

	/**
	 * Get the {@link NumberMetric} value
	 *
	 * @param monitor    The {@link Monitor} instance we wish to extract the {@link NumberMetric} value
	 * @param metricName The name of the {@link NumberMetric} instance
	 * @param previous   Indicate whether we should return the <code>value</code> or the <code>previousValue</code>.
	 * @return a {@link Double} value
	 */
	public static Double getNumberMetricValue(final Monitor monitor, final String metricName, final boolean previous) {
		final NumberMetric metric = monitor.getMetric(metricName, NumberMetric.class);

		if (metric == null) {
			return null;
		}

		return previous ? getDoubleValue(metric.getPreviousValue()) : getDoubleValue(metric.getValue());
	}

	/**
	 * Get the {@link StateSetMetric} value
	 *
	 * @param monitor    The {@link Monitor} instance we wish to extract the {@link StateSetMetric} value
	 * @param metricName The name of the {@link StateSetMetric} instance
	 * @return a {@link Double} value
	 */
	public static String getStateSetMetricValue(final Monitor monitor, final String metricName, final boolean previous) {
		final StateSetMetric stateSetMetric = monitor.getMetric(metricName, StateSetMetric.class);

		if (stateSetMetric == null) {
			return null;
		}

		return previous ? stateSetMetric.getPreviousValue() : stateSetMetric.getValue();
	}

	/**
	 * Get the {@link NumberMetric} collect time
	 *
	 * @param monitor       The {@link Monitor} instance we wish to extract the {@link NumberMetric} collect time
	 * @param metricRateName The name of the {@link NumberMetric} instance
	 * @param previous      Indicate whether we should return the <code>collectTime</code> or the <code>previousCollectTime</code>.
	 * @return a {@link Double} value
	 */
	public static Double getNumberMetricCollectTime(
		final Monitor monitor,
		final String metricRateName,
		final boolean previous
	) {
		final NumberMetric metric = monitor.getMetric(metricRateName, NumberMetric.class);

		if (metric == null) {
			return null;
		}

		return previous ? getDoubleValue(metric.getPreviousCollectTime()) : getDoubleValue(metric.getCollectTime());
	}

	/**
	 * Return the {@link Double} value of the given {@link Number} instance
	 *
	 * @param number	The {@link Number} whose {@link Double} value should be extracted from
	 * @return {@link Double} instance
	 */
	public static Double getDoubleValue(final Number number) {
		if (number == null) {
			return null;
		}

		return number.doubleValue();
	}

	/**
	 * Get the updated {@link NumberMetric} value
	 *
	 * @param monitor    The {@link Monitor} instance we wish to extract the {@link NumberMetric} value
	 * @param metricName The name of the {@link NumberMetric} instance
	 * @return a {@link Double} value
	 */
	public static Double getUpdatedNumberMetricValue(final Monitor monitor, final String metricName) {
		final NumberMetric metric = monitor.getMetric(metricName, NumberMetric.class);

		if (metric == null) {
			return null;
		}

		return metric.isUpdated() ? getDoubleValue(metric.getValue()) : null;
	}
}
