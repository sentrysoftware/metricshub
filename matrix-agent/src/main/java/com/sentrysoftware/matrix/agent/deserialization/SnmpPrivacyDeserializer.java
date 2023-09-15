package com.sentrysoftware.matrix.agent.deserialization;

import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.databind.DeserializationContext;
import com.fasterxml.jackson.databind.JsonDeserializer;
import com.sentrysoftware.matrix.configuration.SnmpConfiguration.Privacy;
import java.io.IOException;

public class SnmpPrivacyDeserializer extends JsonDeserializer<Privacy> {

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
