package com.sentrysoftware.matrix.model.alert;

import java.util.function.BiFunction;

import java.util.Objects;

import lombok.AllArgsConstructor;
import lombok.Getter;

@AllArgsConstructor
public enum AlertOperator {

	EQ((paramValue, threshold) -> nonNull(paramValue, threshold) && paramValue.doubleValue() == threshold.doubleValue(), "=="),
	GT((paramValue, threshold) -> nonNull(paramValue, threshold) && paramValue.doubleValue() > threshold.doubleValue(), ">"),
	GTE((paramValue, threshold) -> nonNull(paramValue, threshold) && paramValue.doubleValue() >= threshold.doubleValue(), ">="),
	LT((paramValue, threshold) -> nonNull(paramValue, threshold) && paramValue.doubleValue() < threshold.doubleValue(), "<"),
	LTE((paramValue, threshold) -> nonNull(paramValue, threshold) && paramValue.doubleValue() <= threshold.doubleValue(), "<="),
	NE((paramValue, threshold) -> nonNull(paramValue, threshold) && paramValue.doubleValue() != threshold.doubleValue(), "!=");

	@Getter
	private BiFunction<Double, Double, Boolean> function;
	@Getter
	private String expression;

	private static boolean nonNull(Double paramValue, Double threshold) {
		return  Objects.nonNull(paramValue) && Objects.nonNull(threshold);
	}
}
