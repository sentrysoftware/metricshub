package org.sentrysoftware.metricshub.engine.connector.model.identity;

/**
 * Enum representing the types of connections for a connector.
 */
public enum ConnectionType {
	/**
	 * Remote connection
	 */
	REMOTE,
	/**
	 * Local connection
	 */
	LOCAL;

	/**
	 * Detects {@link ConnectionType} using the value defined in the connector code.
	 *
	 * @param value The value to detect.
	 * @return {@link ConnectionType} instance.
	 * @throws IllegalArgumentException If the provided value is not a supported connection type.
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
