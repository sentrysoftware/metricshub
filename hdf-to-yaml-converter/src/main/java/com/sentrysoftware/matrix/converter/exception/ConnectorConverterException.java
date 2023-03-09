package com.sentrysoftware.matrix.converter.exception;

public class ConnectorConverterException extends RuntimeException {

	private static final long serialVersionUID = 1L;

	public ConnectorConverterException() {
		super();
	}

	public ConnectorConverterException(String message) {
		super(message);
	}

	public ConnectorConverterException(Throwable cause) {
		super(cause);
	}

	public ConnectorConverterException(String message, Throwable cause) {
		super(message, cause);
	}

}
