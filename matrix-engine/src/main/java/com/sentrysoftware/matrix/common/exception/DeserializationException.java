package com.sentrysoftware.matrix.common.exception;

public class DeserializationException extends RuntimeException {

	private static final long serialVersionUID = -5293423254348627190L;

	public DeserializationException(final String message, final Throwable cause) {
		super(message, cause);
	}

	public DeserializationException(final String message) {
		super(message);
	}
}
