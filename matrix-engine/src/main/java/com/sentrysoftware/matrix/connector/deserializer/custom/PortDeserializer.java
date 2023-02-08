package com.sentrysoftware.matrix.connector.deserializer.custom;

import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.databind.DeserializationContext;
import com.fasterxml.jackson.databind.JsonDeserializer;
import com.fasterxml.jackson.databind.exc.InvalidFormatException;
import java.io.IOException;

public class PortDeserializer extends JsonDeserializer<Integer> {

	@Override
	public Integer deserialize(JsonParser parser, DeserializationContext ctxt) throws IOException {
		if (parser == null)
			return null;

		final String key = parser.getCurrentName();

		if (!parser.isExpectedNumberIntToken()) {
			throw new InvalidFormatException(
				parser,
				String.format("Invalid value encountered for property '%s'.", key),
				parser.getValueAsString(),
				Integer.class
			);
		}

		final Integer value = parser.getIntValue();

		if (value <= 0) {
			throw new InvalidFormatException(
				parser,
				String.format("Invalid negative or zero value encountered for property '%s'.", key),
				value,
				Integer.class
			);
		}

		return value;

	}
}