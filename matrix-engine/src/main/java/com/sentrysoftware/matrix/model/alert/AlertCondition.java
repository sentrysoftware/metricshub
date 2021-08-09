package com.sentrysoftware.matrix.model.alert;

import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class AlertCondition {

	private AlertOperator operator;
	private Double threshold;

	public AlertCondition copy() {
		return AlertCondition.builder()
				.operator(operator)
				.threshold(threshold)
				.build();
	}
}
