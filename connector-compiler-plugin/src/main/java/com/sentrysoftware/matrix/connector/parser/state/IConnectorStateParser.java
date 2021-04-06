package com.sentrysoftware.matrix.connector.parser.state;

import com.sentrysoftware.matrix.connector.model.Connector;

public interface IConnectorStateParser {

	/**
	 * Detect the given line key
	 * @param key
	 * @param connector
	 * @return <code>true</code> if the line key is detected
	 */
	public boolean detect(final String key, final Connector connector);

	/**
	 * Parse the given line and update the passed {@link Connector} object
	 * @param key
	 * @param value
	 * @param connector
	 */
	public void parse(final String key, final String value, final Connector connector);
}
