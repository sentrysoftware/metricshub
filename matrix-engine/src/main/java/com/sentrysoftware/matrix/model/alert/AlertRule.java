package com.sentrysoftware.matrix.model.alert;

import java.util.List;
import java.util.function.BiFunction;

import com.sentrysoftware.matrix.model.monitor.Monitor;
import com.sentrysoftware.matrix.model.parameter.ParameterState;

import lombok.Data;

@Data
public class AlertRule {
	private BiFunction<Monitor, List<AlertCondition>, AlertDetails> conditionsChecker;
	private long period;
	private List<AlertCondition> conditions;
	private ParameterState severity;
	private Long firstTriggerTimestamp;
	private AlertDetails details;
	private AlertRuleState active = AlertRuleState.INACTIVE;

	public AlertRule(BiFunction<Monitor, List<AlertCondition>, AlertDetails> conditionsChecker, List<AlertCondition> conditions, long period,
			ParameterState severity) {
		this.conditionsChecker = conditionsChecker;
		this.conditions = conditions;
		this.period = period;
		this.severity = severity;
	}

	public AlertRule(BiFunction<Monitor, List<AlertCondition>, AlertDetails> conditionsChecker, List<AlertCondition> conditions,
			ParameterState severity) {
		this.conditionsChecker = conditionsChecker;
		this.conditions = conditions;
		this.severity = severity;
	}

	public enum AlertRuleState { INACTIVE, PENDING, ACTIVE }

	/**
	 * Evaluate the current {@link AlertRule}
	 * 
	 * @param monitor The monitor on wish we apply the condition
	 */
	public void evaluate(Monitor monitor) {
		if (conditions != null) {
			details = conditionsChecker.apply(monitor, conditions);
			refresh();
		}
	}

	/**
	 * Get the alert rule state. This method refreshes the current AlertRule before returning its state
	 * 
	 * @return {@link AlertRuleState}: INACTIVE, PENDING or ACTIVE
	 */
	public AlertRuleState getActive() {
		refresh();
		return active;
	}

	/**
	 * Check if the current AlertRule is active
	 * 
	 * @return <code>true</code> or <code>false</code>
	 */
	public boolean isActive() {
		refresh();
		return AlertRuleState.ACTIVE.equals(active);
	}

	/**
	 * Refresh the current {@link AlertRule}.
	 */
	private void refresh() {
		// We have a details ? means the condition returned true (unfortunately)
		if (details != null) {
			long currentTimeMillis = System.currentTimeMillis();
			firstTriggerTimestamp = firstTriggerTimestamp == null ? currentTimeMillis : firstTriggerTimestamp;
			// If we reach the time limit defined by the period then the AlertRule becomes ACTIVE otherwise it is PENDING
			if (currentTimeMillis - firstTriggerTimestamp >= period) {
				active = AlertRuleState.ACTIVE;
			} else {
				active = AlertRuleState.PENDING;
			}
		}
	}

}
