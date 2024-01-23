package org.sentrysoftware.metricshub.engine.strategy.utils;

import lombok.AccessLevel;
import lombok.NoArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@NoArgsConstructor(access = AccessLevel.PRIVATE)
@Slf4j
public class MathOperationsHelper {

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

	/**
	 * Compute the minimum between two {@link Double} operands. If one of the operands is null, return the other operand.
	 * @param operandOne
	 * @param operandTwo
	 * @return {@link Double} value
	 */
	public static Double min(final Double operandOne, final Double operandTwo) {
		if (operandOne == null) {
			return operandTwo;
		} else if (operandTwo == null) {
			return operandOne;
		} else {
			return Math.min(operandOne, operandTwo);
		}
	}
}
