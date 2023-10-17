package com.sentrysoftware.metricshub.agent.deserialization;

import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.databind.DeserializationContext;
import com.fasterxml.jackson.databind.JsonDeserializer;
import com.sentrysoftware.metricshub.engine.configuration.SnmpConfiguration.SnmpVersion;
import java.io.IOException;

public class SnmpVersionDeserializer extends JsonDeserializer<SnmpVersion> {

	@Override
	public SnmpVersion deserialize(JsonParser parser, DeserializationContext ctxt) throws IOException {
		if (parser == null) {
			return null;
		}

		try {
			return SnmpVersion.interpretValueOf(parser.getValueAsString());
		} catch (IllegalArgumentException e) {
			throw new IOException(e.getMessage());
		}
	}
}
