package com.sentrysoftware.matrix.common.exception;

/**
 * This exception must be thrown by the controlled SSH executions
 */
public class ControlledSshException extends Exception {

	private static final long serialVersionUID = 1L;

	public ControlledSshException(String message) {
		super(message);
	}

}
