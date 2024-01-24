package org.sentrysoftware.metricshub.engine.alert;

import java.util.Objects;
import java.util.function.BiPredicate;
import lombok.AllArgsConstructor;
import lombok.Getter;

/**
 * Represents different comparison operators used in alert conditions, such as
 * equality, greater than, greater than or equal to, less than, less than or equal to,
 * and not equal to.
 */
@AllArgsConstructor
public enum AlertOperator {
	// CHECKSTYLE:OFF
	/**
	 * Equality operator.
	 */
	EQ(
		(paramValue, threshold) -> nonNull(paramValue, threshold) && paramValue.doubleValue() == threshold.doubleValue(),
		"=="
	),
	/**
	 * Greater than operator.
	 */
	GT(
		(paramValue, threshold) -> nonNull(paramValue, threshold) && paramValue.doubleValue() > threshold.doubleValue(),
		">"
	),
	/**
	 * Greater than or equal to operator.
	 */
	GTE(
		(paramValue, threshold) -> nonNull(paramValue, threshold) && paramValue.doubleValue() >= threshold.doubleValue(),
		">="
	),
	/**
	 * Less than operator.
	 */
	LT(
		(paramValue, threshold) -> nonNull(paramValue, threshold) && paramValue.doubleValue() < threshold.doubleValue(),
		"<"
	),
	/**
	 * Less than or equal to operator.
	 */
	LTE(
		(paramValue, threshold) -> nonNull(paramValue, threshold) && paramValue.doubleValue() <= threshold.doubleValue(),
		"<="
	),
	/**
	 * Not equal to operator.
	 */
	NE(
		(paramValue, threshold) -> nonNull(paramValue, threshold) && paramValue.doubleValue() != threshold.doubleValue(),
		"!="
	);

	// CHECKSTYLE:ON

	@Getter
	private BiPredicate<Double, Double> function;

	@Getter
	private String expression;

	private static boolean nonNull(Double paramValue, Double threshold) {
		return Objects.nonNull(paramValue) && Objects.nonNull(threshold);
	}
}
