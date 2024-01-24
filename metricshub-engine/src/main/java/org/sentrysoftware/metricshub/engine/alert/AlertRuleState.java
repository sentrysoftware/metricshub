package org.sentrysoftware.metricshub.engine.alert;

/**
 * Represents the state of an alert rule.
 */
public enum AlertRuleState {
	/**
	 * Inactive state indicating that the alert rule is not triggered.
	 */
	INACTIVE,
	/**
	 * Pending state indicating that the alert rule is waiting to become active.
	 */
	PENDING,
	/**
	 * Active state indicating that the alert rule is triggered.
	 */
	ACTIVE
}
