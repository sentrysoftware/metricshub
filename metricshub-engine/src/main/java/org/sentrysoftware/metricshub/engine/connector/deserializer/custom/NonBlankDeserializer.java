package org.sentrysoftware.metricshub.engine.connector.deserializer.custom;

import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.databind.DeserializationContext;
import com.fasterxml.jackson.databind.JsonDeserializer;
import com.fasterxml.jackson.databind.exc.InvalidFormatException;
import java.io.IOException;

/**
 * Custom deserializer for ensuring that a deserialized string property is non-blank.
 */
public class NonBlankDeserializer extends JsonDeserializer<String> {

	@Override
	public String deserialize(JsonParser parser, DeserializationContext ctxt) throws IOException {
		if (parser == null) {
			return null;
		}

		final String key = parser.getCurrentName();
		final String value = parser.getValueAsString();

		if (value == null || !value.isBlank()) {
			return value;
		}

		throw new InvalidFormatException(
			parser,
			String.format("Invalid blank value encountered for property '%s'.", key),
			value,
			String.class
		);
	}
}
