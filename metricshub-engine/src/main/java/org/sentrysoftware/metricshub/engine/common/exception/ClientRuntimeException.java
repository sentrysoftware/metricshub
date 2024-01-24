package org.sentrysoftware.metricshub.engine.common.exception;

/**
 * This class is used to manage exceptions that can be thrown by the functional interface
 * implementations used to run commands.
 */
public class ClientRuntimeException extends RuntimeException {

	private static final long serialVersionUID = 1L;

	/**
	 * Constructs a new {@code ClientRuntimeException} with the specified cause.
	 *
	 * @param cause The underlying cause of this exception.
	 */
	public ClientRuntimeException(final ClientException cause) {
		super(cause);
	}
}
