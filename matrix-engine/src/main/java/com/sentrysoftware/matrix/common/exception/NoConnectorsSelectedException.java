package com.sentrysoftware.matrix.common.exception;

public class NoConnectorsSelectedException extends Exception {

	private static final long serialVersionUID = 1L;

	public NoConnectorsSelectedException() {
		super("No connectors were selected during DetectionOperation");
	}
}
