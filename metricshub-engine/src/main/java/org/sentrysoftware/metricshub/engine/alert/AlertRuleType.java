package org.sentrysoftware.metricshub.engine.alert;

/**
 * Enumeration representing types of alert rules.
 */
public enum AlertRuleType {
	/**
	 * {@code STATIC}: A static or predefined alert rule.
	 */
	STATIC,
	/**
	 * {@code INSTANCE}: An alert rule associated with a specific instance.
	 */
	INSTANCE,
	/**
	 * {@code USER}: A user-defined alert rule.
	 */
	USER
}
