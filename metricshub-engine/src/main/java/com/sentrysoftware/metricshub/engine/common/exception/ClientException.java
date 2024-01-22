package com.sentrysoftware.metricshub.engine.common.exception;

public class ClientException extends Exception {

	private static final long serialVersionUID = 1L;

	public ClientException() {
		super();
	}

	public ClientException(Exception cause) {
		super(cause);
	}

	public ClientException(String message) {
		super(message);
	}

	public ClientException(String message, Exception cause) {
		super(message, cause);
	}
}
