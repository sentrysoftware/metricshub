package com.sentrysoftware.metricshub.engine.alert;

/**
 * Enum representing different severity levels for alerts.
 * <p>
 * The severity levels are used to categorize alerts based on their impact and urgency.
 * </p>
 */
public enum Severity {
	/**
	 * Indicates an informational alert with no immediate action required.
	 */
	INFO,
	/**
	 * Indicates a warning alert that requires attention but is not critical.
	 */
	WARN,
	/**
	 * Indicates an alarm alert that requires immediate attention due to critical issues.
	 */
	ALARM
}
