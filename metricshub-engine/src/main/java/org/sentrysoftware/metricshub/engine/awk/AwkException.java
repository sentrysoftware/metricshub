package org.sentrysoftware.metricshub.engine.awk;

/**
 * Awk Exception
 */
public class AwkException extends Exception {

	private static final long serialVersionUID = 1L;

	/**
	 * Constructs an AWK Exception using the given message
	 *
	 * @param message Exception message
	 */
	public AwkException(String message) {
		super(message);
	}

	/**
	 * Constructs an AWK Exception using the given message and the throwable
	 *
	 * @param message   Exception message
	 * @param throwable Any throwable error
	 */
	public AwkException(String message, Throwable throwable) {
		super(message, throwable);
	}
}
