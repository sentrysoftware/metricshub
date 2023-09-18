package com.sentrysoftware.matrix.common.exception;

/**
 * This class is used to manage exceptions that can be thrown by the functional interface
 * implementations used to run commands through Matsya.
 */
public class MatsyaRuntimeException extends RuntimeException {

	private static final long serialVersionUID = 1L;

	public MatsyaRuntimeException(final MatsyaException cause) {
		super(cause);
	}
}
