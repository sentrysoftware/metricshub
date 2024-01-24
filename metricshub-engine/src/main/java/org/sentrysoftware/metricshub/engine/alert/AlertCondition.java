package org.sentrysoftware.metricshub.engine.alert;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * Represents a condition for triggering alerts based on monitored data.
 * An alert condition includes an operator and a threshold value.
 * Provides a method to create a copy of the condition.
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class AlertCondition {

	private AlertOperator operator;
	private Double threshold;

	/**
	 * Creates a copy of the alert condition.
	 *
	 * @return A new {@code AlertCondition} instance with the same operator and threshold.
	 */
	public AlertCondition copy() {
		return AlertCondition.builder().operator(operator).threshold(threshold).build();
	}
}
