package com.sentrysoftware.metricshub.engine.connector.model.identity;

public enum ConnectionType {
	REMOTE,
	LOCAL;

	/**
	 * Detect {@link ConnectionType} using the value defined in the connector code
	 *
	 * @param value
	 * @return {@link ConnectionType} instance
	 */
	public static ConnectionType detect(final String value) {
		// Null returns null
		if (value == null) {
			return null;
		}

		try {
			return ConnectionType.valueOf(value.toUpperCase());
		} catch (Exception e) {
			// No match => Exception
			throw new IllegalArgumentException(
				"'" +
				value +
				"' is not a supported connection type." +
				"ConnectionType must be a known connection type (local, remote)."
			);
		}
	}
}
