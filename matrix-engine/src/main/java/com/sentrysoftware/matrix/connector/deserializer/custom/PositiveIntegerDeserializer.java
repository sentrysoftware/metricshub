package com.sentrysoftware.matrix.connector.deserializer.custom;

import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.databind.DeserializationContext;
import com.fasterxml.jackson.databind.JsonDeserializer;
import com.fasterxml.jackson.databind.exc.InvalidFormatException;
import java.io.IOException;

public class PositiveIntegerDeserializer extends JsonDeserializer<Integer> {

	@Override
	public Integer deserialize(JsonParser parser, DeserializationContext ctxt) throws IOException {
		if (parser == null) {
			return null;
		}

		final String key = parser.getCurrentName();

		final String str = parser.getValueAsString();
		if (str == null) {
			return null;
		}

		final Integer value;
		try {
			value = Integer.parseInt(str);
		} catch (Exception e) {
			throw new InvalidFormatException(
				parser,
				String.format("Invalid value encountered for property '%s'. Error: %s", key, e.getMessage()),
				str,
				Integer.class
			);
		}

		if (value >= 0) {
			return value;
		}

		throw new InvalidFormatException(
			parser,
			String.format("Invalid negative value encountered for property '%s'.", key),
			value,
			Integer.class
		);
	}
}
