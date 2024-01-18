package com.sentrysoftware.metricshub.agent.deserialization;

import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.databind.DeserializationContext;
import com.fasterxml.jackson.databind.JsonDeserializer;
import com.sentrysoftware.metricshub.engine.configuration.SnmpConfiguration.Privacy;
import java.io.IOException;

/**
 * Custom deserializer for converting SNMP privacy string representations to {@link Privacy}.
 * It is used in conjunction with Jackson's JSON deserialization to convert JSON values to the appropriate enum type.
 */
public class SnmpPrivacyDeserializer extends JsonDeserializer<Privacy> {

	/**
	 * Deserializes a JSON value (in the form of a JsonParser) into the corresponding {@link Privacy} enum.
	 *
	 * @param parser The JsonParser containing the JSON value to be deserialized
	 * @param ctxt   The DeserializationContext
	 * @return The deserialized {@link Privacy} enum value
	 * @throws IOException If an I/O error occurs during deserialization
	 */
	@Override
	public Privacy deserialize(JsonParser parser, DeserializationContext ctxt) throws IOException {
		if (parser == null) {
			return null;
		}

		try {
			return Privacy.interpretValueOf(parser.getValueAsString());
		} catch (IllegalArgumentException e) {
			throw new IOException(e.getMessage());
		}
	}
}
