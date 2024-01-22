package org.sentrysoftware.metricshub.engine.alert;

import java.util.Objects;
import java.util.function.BiPredicate;
import lombok.AllArgsConstructor;
import lombok.Getter;

@AllArgsConstructor
public enum AlertOperator {
	// CHECKSTYLE:OFF
	EQ(
		(paramValue, threshold) -> nonNull(paramValue, threshold) && paramValue.doubleValue() == threshold.doubleValue(),
		"=="
	),
	GT(
		(paramValue, threshold) -> nonNull(paramValue, threshold) && paramValue.doubleValue() > threshold.doubleValue(),
		">"
	),
	GTE(
		(paramValue, threshold) -> nonNull(paramValue, threshold) && paramValue.doubleValue() >= threshold.doubleValue(),
		">="
	),
	LT(
		(paramValue, threshold) -> nonNull(paramValue, threshold) && paramValue.doubleValue() < threshold.doubleValue(),
		"<"
	),
	LTE(
		(paramValue, threshold) -> nonNull(paramValue, threshold) && paramValue.doubleValue() <= threshold.doubleValue(),
		"<="
	),
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
