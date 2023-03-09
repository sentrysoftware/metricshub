package com.sentrysoftware.matrix.converter.state;

import com.fasterxml.jackson.databind.JsonNode;

public interface IConnectorStateConverter {

	/**
	 * Detect the given line key - value. Use the connector parameter to check
	 * the actual context
	 * 
	 * @param key The property name
	 * @param value The value of the property
	 * @param connector The contextual {@link JsonNode}
	 * @return <code>true</code> if the line key is detected
	 */
	boolean detect(final String key, final String value, final JsonNode connector);

	/**
	 * Parse the given line and update the passed {@link JsonNode} object
	 * 
	 * @param key The property name
	 * @param value The value of the property
	 * @param connector The contextual {@link JsonNode}
	 */
	void convert(final String key, final String value, final JsonNode connector);
}