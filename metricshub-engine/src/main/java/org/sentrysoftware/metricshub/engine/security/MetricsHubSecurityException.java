package org.sentrysoftware.metricshub.engine.security;

import lombok.NoArgsConstructor;

/**
 * The MetricsHubSecurityException is an exception class specifically designed for security-related issues
 * within the MetricsHub engine.
 */
@NoArgsConstructor
public class MetricsHubSecurityException extends Exception {

	private static final long serialVersionUID = 1L;

	/**
	 * Constructs a new MetricsHubSecurityException with the specified detail message.
	 *
	 * @param message the detail message (which is saved for later retrieval by the getMessage() method)
	 */
	public MetricsHubSecurityException(String message) {
		super(message);
	}

	/**
	 * Constructs a new MetricsHubSecurityException with the specified cause.
	 *
	 * @param cause the cause (which is saved for later retrieval by the getCause() method)
	 */
	public MetricsHubSecurityException(Throwable cause) {
		super(cause);
	}

	/**
	 * Constructs a new MetricsHubSecurityException with the specified detail message and cause.
	 *
	 * @param message the detail message (which is saved for later retrieval by the getMessage() method)
	 * @param cause   the cause (which is saved for later retrieval by the getCause() method)
	 */
	public MetricsHubSecurityException(String message, Throwable cause) {
		super(message, cause);
	}
}
