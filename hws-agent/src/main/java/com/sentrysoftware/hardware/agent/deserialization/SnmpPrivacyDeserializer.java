package com.sentrysoftware.hardware.agent.deserialization;

import java.io.IOException;

import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.databind.DeserializationContext;
import com.fasterxml.jackson.databind.JsonDeserializer;
import com.sentrysoftware.matrix.engine.protocol.SnmpProtocol.Privacy;

public class SnmpPrivacyDeserializer extends JsonDeserializer<Privacy> {

	@Override
	public Privacy deserialize(JsonParser parser, DeserializationContext ctxt)
			throws IOException {
		if (parser == null)
			return null;

		try {
			return Privacy.interpretValueOf(parser.getValueAsString());
		} catch (IllegalArgumentException e) {
			throw new IOException(e.getMessage());
		}

	}

}