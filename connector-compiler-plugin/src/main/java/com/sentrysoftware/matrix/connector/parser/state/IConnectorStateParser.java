package com.sentrysoftware.matrix.connector.parser.state;

import com.sentrysoftware.matrix.connector.model.Connector;

public interface IConnectorStateParser {

	/**
	 * Detect the given line key - value. Use the Connector parameter to check the actual context
	 * @param key		The property name
	 * @param value		The value of the property
	 * @param connector	The contextual {@link Connector}
	 * @return <code>true</code> if the line key is detected
	 */
	boolean detect(final String key, final String value, final Connector connector);

	/**
	 * Parse the given line and update the passed {@link Connector} object
	 * @param key		The property name
	 * @param value		The value of the property
	 * @param connector	The contextual {@link Connector}
	 */
	void parse(final String key, final String value, final Connector connector);
}
