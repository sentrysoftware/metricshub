package com.sentrysoftware.matrix.alert;

import lombok.AllArgsConstructor;
import lombok.Getter;

import java.util.Objects;
import java.util.function.BiPredicate;

@AllArgsConstructor
public enum AlertOperator {

	EQ((paramValue, threshold) -> nonNull(paramValue, threshold) && paramValue.doubleValue() == threshold.doubleValue(), "=="),
	GT((paramValue, threshold) -> nonNull(paramValue, threshold) && paramValue.doubleValue() > threshold.doubleValue(), ">"),
	GTE((paramValue, threshold) -> nonNull(paramValue, threshold) && paramValue.doubleValue() >= threshold.doubleValue(), ">="),
	LT((paramValue, threshold) -> nonNull(paramValue, threshold) && paramValue.doubleValue() < threshold.doubleValue(), "<"),
	LTE((paramValue, threshold) -> nonNull(paramValue, threshold) && paramValue.doubleValue() <= threshold.doubleValue(), "<="),
	NE((paramValue, threshold) -> nonNull(paramValue, threshold) && paramValue.doubleValue() != threshold.doubleValue(), "!=");

	@Getter
	private BiPredicate<Double, Double> function;
	@Getter
	private String expression;

	private static boolean nonNull(Double paramValue, Double threshold) {
		return Objects.nonNull(paramValue) && Objects.nonNull(threshold);
	}
}
