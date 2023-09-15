package com.sentrysoftware.matrix.connector;

public class ConnectorSerializationException extends RuntimeException {

	private static final long serialVersionUID = 1L;

	public ConnectorSerializationException() {
		super();
	}

	public ConnectorSerializationException(String message) {
		super(message);
	}

	public ConnectorSerializationException(Throwable cause) {
		super(cause);
	}

	public ConnectorSerializationException(String message, Throwable cause) {
		super(message, cause);
	}
}
