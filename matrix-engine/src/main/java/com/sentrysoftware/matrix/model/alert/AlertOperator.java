package com.sentrysoftware.matrix.model.alert;

import java.util.function.BiFunction;

import java.util.Objects;

import lombok.AllArgsConstructor;
import lombok.Getter;

@AllArgsConstructor
public enum AlertOperator {

	EQ((paramValue, threshold) -> nonNull(paramValue, threshold) && paramValue == threshold),
	GT((paramValue, threshold) -> nonNull(paramValue, threshold) && paramValue > threshold),
	GTE((paramValue, threshold) -> nonNull(paramValue, threshold) && paramValue >= threshold),
	LT((paramValue, threshold) -> nonNull(paramValue, threshold) && paramValue < threshold),
	LTE((paramValue, threshold) -> nonNull(paramValue, threshold) && paramValue <= threshold),
	NE((paramValue, threshold) -> nonNull(paramValue, threshold) && paramValue != threshold);

	@Getter
	private BiFunction<Double, Double, Boolean> function;

	private static boolean nonNull(Double paramValue, Double threshold) {
		return  Objects.nonNull(paramValue) && Objects.nonNull(threshold);
	}
}
