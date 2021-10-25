package com.sentrysoftware.matrix.security;

import lombok.NoArgsConstructor;

@NoArgsConstructor
public class HardwareSecurityException extends Exception {

	private static final long serialVersionUID = 1L;

	public HardwareSecurityException(String message) {

		super(message);
	}

	public HardwareSecurityException(Throwable cause) {

		super(cause);
	}

	public HardwareSecurityException(String message, Throwable cause) {

		super(message, cause);
	}

}