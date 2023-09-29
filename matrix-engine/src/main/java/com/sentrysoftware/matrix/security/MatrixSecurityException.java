package com.sentrysoftware.matrix.security;

import lombok.NoArgsConstructor;

@NoArgsConstructor
public class MatrixSecurityException extends Exception {

	private static final long serialVersionUID = 1L;

	public MatrixSecurityException(String message) {
		super(message);
	}

	public MatrixSecurityException(Throwable cause) {
		super(cause);
	}

	public MatrixSecurityException(String message, Throwable cause) {
		super(message, cause);
	}
}
