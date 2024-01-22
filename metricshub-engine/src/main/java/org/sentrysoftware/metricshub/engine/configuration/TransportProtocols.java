package org.sentrysoftware.metricshub.engine.configuration;

public enum TransportProtocols {
	HTTP,
	HTTPS;

	/**
	 * Interpret the specified name and returns corresponding value.
	 *
	 * @param label String to be interpreted
	 * @return Corresponding {@link TransportProtocols} value
	 */
	public static TransportProtocols interpretValueOf(final String label) {
		if ("http".equalsIgnoreCase(label)) {
			return HTTP;
		}

		if ("https".equalsIgnoreCase(label)) {
			return HTTPS;
		}

		throw new IllegalArgumentException("Invalid protocol value: " + label);
	}

	@Override
	public String toString() {
		if (this == HTTP) {
			return "http";
		}
		return "https";
	}
}
