package com.sentrysoftware.hardware.prometheus.deserialization;

import java.io.IOException;
import java.util.Optional;

import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.databind.DeserializationContext;
import com.fasterxml.jackson.databind.JsonDeserializer;
import com.sentrysoftware.matrix.model.parameter.ParameterState;

public class UnknownStatusDeserializer extends JsonDeserializer<Optional<ParameterState>> {

	@Override
	public Optional<ParameterState> deserialize(JsonParser parser, DeserializationContext ctxt)
			throws IOException {
		if (parser == null) {
			return Optional.empty();
		}

		try {
			return ParameterState.interpretValueOf(parser.getValueAsString());
		} catch (IllegalArgumentException e) {
			throw new IOException("Problem with unknownStatus. " + e.getMessage());
		}
	}

}
