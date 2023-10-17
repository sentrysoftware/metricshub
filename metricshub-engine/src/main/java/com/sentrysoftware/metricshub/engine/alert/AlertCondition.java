package com.sentrysoftware.metricshub.engine.alert;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class AlertCondition {

	private AlertOperator operator;
	private Double threshold;

	public AlertCondition copy() {
		return AlertCondition.builder().operator(operator).threshold(threshold).build();
	}
}
