package com.sentrysoftware.matrix.connector.deserializer.custom;

import java.io.IOException;

import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.databind.DeserializationContext;
import com.fasterxml.jackson.databind.JsonDeserializer;
import com.sentrysoftware.matrix.connector.model.common.ResultContent;

public class ResultContentDeserializer extends JsonDeserializer<ResultContent> {

	@Override
	public ResultContent deserialize(JsonParser parser, DeserializationContext ctxt) throws IOException {
		if (parser == null)
			return null;

		try {
			return ResultContent.detect(parser.getValueAsString());
		} catch (IllegalArgumentException e) {
			throw new IOException(e.getMessage());
		}

	}

}
