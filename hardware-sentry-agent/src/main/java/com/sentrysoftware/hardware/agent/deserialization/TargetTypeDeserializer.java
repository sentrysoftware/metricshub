package com.sentrysoftware.hardware.agent.deserialization;

import java.io.IOException;

import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.databind.DeserializationContext;
import com.fasterxml.jackson.databind.JsonDeserializer;
import com.sentrysoftware.matrix.engine.target.TargetType;

public class TargetTypeDeserializer extends JsonDeserializer<TargetType> {

	@Override
	public TargetType deserialize(JsonParser parser, DeserializationContext ctxt) throws IOException {
		if (parser == null)
			return null;

		try {
			return TargetType.interpretValueOf(parser.getValueAsString());
		} catch (IllegalArgumentException e) {
			throw new IOException(e.getMessage());
		}

	}

}
