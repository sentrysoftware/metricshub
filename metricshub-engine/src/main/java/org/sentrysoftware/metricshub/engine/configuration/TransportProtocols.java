package org.sentrysoftware.metricshub.engine.configuration;

/**
 * Enum representing transport protocols for communication.
 * Supports HTTP and HTTPS protocols.
 */
public enum TransportProtocols {
	/**
	 * HTTP transport protocol.
	 */
	HTTP,
	/**
	 * HTTPS transport protocol.
	 */
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
