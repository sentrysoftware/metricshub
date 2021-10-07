package com.sentrysoftware.matrix.common.exception;

public class StepException extends Exception {

	private static final long serialVersionUID = 1L;

	public StepException(final String message) {
		super(message);
	}

	public StepException(final String message, final Throwable cause) {
		super(message, cause);
	}
}
