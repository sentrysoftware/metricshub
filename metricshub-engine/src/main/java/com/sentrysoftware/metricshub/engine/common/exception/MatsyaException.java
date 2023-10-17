package com.sentrysoftware.metricshub.engine.common.exception;

public class MatsyaException extends Exception {

	private static final long serialVersionUID = 1L;

	public MatsyaException() {
		super();
	}

	public MatsyaException(Exception cause) {
		super(cause);
	}

	public MatsyaException(String message) {
		super(message);
	}

	public MatsyaException(String message, Exception cause) {
		super(message, cause);
	}
}
