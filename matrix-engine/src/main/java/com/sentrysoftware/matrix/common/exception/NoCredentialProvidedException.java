package com.sentrysoftware.matrix.common.exception;

public class NoCredentialProvidedException  extends Exception {

	private static final long serialVersionUID = 1L;

	public NoCredentialProvidedException() {
		super("No credentials provided.");
	}
}
