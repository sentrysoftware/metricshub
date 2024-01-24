package org.sentrysoftware.metricshub.engine.connector.deserializer.custom;

import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.databind.DeserializationContext;
import com.fasterxml.jackson.databind.JsonDeserializer;
import com.fasterxml.jackson.databind.exc.InvalidFormatException;
import java.io.IOException;

/**
 * Custom deserializer for deserializing timeout values.
 */
public class TimeoutDeserializer extends JsonDeserializer<Long> {

	@Override
	public Long deserialize(JsonParser parser, DeserializationContext ctxt) throws IOException {
		if (parser == null) {
			return null;
		}

		final String key = parser.getCurrentName();

		final String str = parser.getValueAsString();
		if (str == null) {
			return null;
		}

		final Long value;
		try {
			value = Long.parseLong(str);
		} catch (Exception e) {
			throw new InvalidFormatException(
				parser,
				String.format("Invalid value encountered for property '%s'. Error: %s", key, e.getMessage()),
				str,
				Integer.class
			);
		}

		if (value > 0) {
			return value;
		}

		throw new InvalidFormatException(
			parser,
			String.format("Invalid negative or zero value encountered for property '%s'.", key),
			value,
			Long.class
		);
	}
}
