package org.sentrysoftware.metricshub.engine.common.exception;

/**
 * This exception must be thrown by the controlled SSH executions
 */
public class ControlledSshException extends Exception {

	private static final long serialVersionUID = 1L;

	/**
	 * Constructs a new ControlledSshException with the specified detail message.
	 *
	 * @param message the detail message.
	 */
	public ControlledSshException(String message) {
		super(message);
	}
}
