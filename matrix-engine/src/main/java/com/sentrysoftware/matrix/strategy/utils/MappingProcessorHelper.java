package com.sentrysoftware.matrix.strategy.utils;

import com.sentrysoftware.matrix.telemetry.Monitor;
import com.sentrysoftware.matrix.telemetry.metric.NumberMetric;
import lombok.AccessLevel;
import lombok.NoArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@NoArgsConstructor(access = AccessLevel.PRIVATE)
public class MappingProcessorHelper {

	/**
	 * Get the {@link NumberMetric} value
	 *
	 * @param monitor    The {@link Monitor} instance we wish to extract the {@link NumberMetric} value
	 * @param metricName The name of the {@link NumberMetric} instance
	 * @param previous   Indicate whether we should return the <code>value</code> or the <code>previousValue</code>.
	 * @return a {@link Double} value
	 */
	public static Double getNumberMetricValue(Monitor monitor, String metricName, boolean previous) {
		final NumberMetric metric = monitor.getMetric(metricName, NumberMetric.class);

		if (metric == null) {
			return null;
		}

		return previous ? getDoubleValue(metric.getPreviousValue()) : getDoubleValue(metric.getValue());
	}

	/**
	 * Get the {@link NumberMetric} collect time
	 *
	 * @param monitor       The {@link Monitor} instance we wish to extract the {@link NumberMetric} collect time
	 * @param metricName The name of the {@link NumberMetric} instance
	 * @param previous      Indicate whether we should return the <code>collectTime</code> or the <code>previousCollectTime</code>.
	 * @return a {@link Double} value
	 */
	public static Double getNumberMetricCollectTime(Monitor monitor, String metricRateName, boolean previous) {
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
	 * Perform a subtraction arithmetic operation
	 *
	 * @param metricName	The name of the metric
	 * @param minuend		Minuend of the subtraction
	 * @param subtrahend	Subtrahend of the subtraction
	 * @param hostname      Current hostname used for logging only
	 *
	 * @return {@link Double} value
	 */
	public static Double subtract(
		final String metricName,
		final Double minuend,
		final Double subtrahend,
		final String hostname
	) {
		if (minuend == null || subtrahend == null) {
			return null;
		}

		final double result = minuend - subtrahend;

		if (result < 0) {
			log.warn(
				"Hostname {} - Suspicious negative value ({} - {}) = {} for metric {}.",
				hostname,
				minuend,
				subtrahend,
				result,
				metricName
			);
			return null;
		}

		return result;
	}

	/**
	 * Perform a division arithmetic operation
	 *
	 * @param metricName The metric we wish to compute using a division (Rate, Percentage...)
	 * @param dividend   The dividend to use
	 * @param divisor    The divisor to use
	 * @param hostname   Current hostname used for logging only
	 * @return {@link Double} value
	 */
	public static Double divide(
		final String metricName,
		final Double dividend,
		final Double divisor,
		final String hostname
	) {
		if (dividend == null || divisor == null) {
			return null;
		}

		if (divisor == 0) {
			log.debug(
				"Hostname {} - Couldn't compute ({} / {}) for metric {}. Division by zero is not allowed.",
				hostname,
				dividend,
				divisor,
				metricName
			);
			return null;
		}

		final double result = dividend / divisor;

		if (result < 0) {
			log.warn(
				"Hostname {} - Suspicious negative value ({} / {}) = {} for metric {}.",
				hostname,
				dividend,
				divisor,
				result,
				metricName
			);
			return null;
		}

		return result;
	}

	/**
	 * Perform a multiplication arithmetic operation
	 *
	 * @param metricName   The name of the metric we currently compute the value for
	 * @param multiplier   The multiplier to use
	 * @param multiplicand The multiplicand to use
	 * @param hostname     Current hostname used for logging only
	 * @return {@link Double} value
	 */
	public static Double multiply(
		final String metricName,
		final Double multiplier,
		final Double multiplicand,
		final String hostname
	) {
		if (multiplier == null || multiplicand == null) {
			return null;
		}

		double result = multiplier * multiplicand;

		if (result < 0) {
			log.warn(
				"Hostname {} - Suspicious negative value ({} * {}) = {} for metric {}.",
				hostname,
				multiplier,
				multiplicand,
				result,
				metricName
			);
			return null;
		}

		return result;
	}

	/**
	 * Compute a rate (value - previousValue) / (collectTime - previousCollectTime)
	 *
	 * @param metricName          The metric name we wish to compute its rate value
	 * @param value               The value from the current collect
	 * @param previousValue       The value from the previous collect
	 * @param collectTime         The time of the current collect
	 * @param previousCollectTime The time of the previous collect
	 * @param hostname            Current hostname used for logging only
	 *
	 * @return {@link Double} value
	 */
	public static Double rate(
		String metricName,
		Double value,
		Double previousValue,
		Double collectTime,
		Double previousCollectTime,
		String hostname
	) {
		return divide(
			metricName,
			subtract(metricName, value, previousValue, hostname),
			subtract(metricName, collectTime, previousCollectTime, hostname),
			hostname
		);
	}
}
