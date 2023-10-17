package com.sentrysoftware.metricshub.engine.security;

import lombok.NoArgsConstructor;

@NoArgsConstructor
public class MetricsHubSecurityException extends Exception {

	private static final long serialVersionUID = 1L;

	public MetricsHubSecurityException(String message) {
		super(message);
	}

	public MetricsHubSecurityException(Throwable cause) {
		super(cause);
	}

	public MetricsHubSecurityException(String message, Throwable cause) {
		super(message, cause);
	}
}
