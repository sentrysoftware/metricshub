package com.sentrysoftware.matrix.connector.deserializer.custom;

import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.databind.DeserializationContext;
import com.fasterxml.jackson.databind.JsonDeserializer;
import com.fasterxml.jackson.databind.exc.InvalidFormatException;
import java.io.IOException;

public class TimeoutDeserializer extends JsonDeserializer<Long> {

	@Override
	public Long deserialize(JsonParser parser, DeserializationContext ctxt) throws IOException {
		if (parser == null)
			return null;

		final String key = parser.getCurrentName();
		final Long value = parser.getValueAsLong();

		if (value == null || value > 0) {
			return value;
		}

		throw new InvalidFormatException(
			parser,
			String.format("Invalid negative or zero value encountered for property '%s'.", key),
			value,
			String.class
		);
	}
}