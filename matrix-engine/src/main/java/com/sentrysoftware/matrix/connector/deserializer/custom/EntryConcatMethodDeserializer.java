package com.sentrysoftware.matrix.connector.deserializer.custom;

import java.io.IOException;

import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.databind.DeserializationContext;
import com.fasterxml.jackson.databind.JsonDeserializer;
import com.fasterxml.jackson.databind.exc.InvalidFormatException;
import com.sentrysoftware.matrix.connector.model.common.CustomConcatMethod;
import com.sentrysoftware.matrix.connector.model.common.EntryConcatMethod;
import com.sentrysoftware.matrix.connector.model.common.IEntryConcatMethod;

public class EntryConcatMethodDeserializer extends JsonDeserializer<IEntryConcatMethod> {

	@Override
	public IEntryConcatMethod deserialize(JsonParser parser, DeserializationContext ctxt)
			throws IOException {
		if (parser == null)
			return null;

		if (parser.isExpectedStartObjectToken()) {
			return parser.readValueAs(CustomConcatMethod.class);
		}

		final String valueAsString = parser.getValueAsString();
		if (valueAsString == null)
			return null;

		try {
			return EntryConcatMethod.getByName(valueAsString);
		} catch (IllegalArgumentException e) {
			throw new InvalidFormatException(
				parser,
				e.getMessage(),
				valueAsString,
				EntryConcatMethod.class
			);
		}

	}

}
