package com.sentrysoftware.matrix.security;

public class HardwareCipherException extends Exception {

	private static final long serialVersionUID = -5157667741518113669L;

	public HardwareCipherException() {

	}

	public HardwareCipherException(String message) {

		super(message);
	}

	public HardwareCipherException(Throwable cause) {

		super(cause);
	}

	public HardwareCipherException(String message, Throwable cause) {

		super(message, cause);
	}

}