package com.sentrysoftware.metricshub.engine.common.exception;

/**
 * This class is used to manage exceptions that can be thrown by the functional interface
 * implementations used to run commands.
 */
public class ClientRuntimeException extends RuntimeException {

	private static final long serialVersionUID = 1L;

	public ClientRuntimeException(final ClientException cause) {
		super(cause);
	}
}
