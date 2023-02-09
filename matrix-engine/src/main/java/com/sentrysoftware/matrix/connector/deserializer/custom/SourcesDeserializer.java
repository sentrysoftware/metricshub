package com.sentrysoftware.matrix.connector.deserializer.custom;

import java.io.IOException;
import java.util.Map;

import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.databind.DeserializationContext;
import com.fasterxml.jackson.databind.JsonDeserializer;
import com.sentrysoftware.matrix.connector.model.monitor.task.source.Source;

public class SourcesDeserializer extends JsonDeserializer<Map<String, Source>> {

	@Override
	public Map<String, Source> deserialize(JsonParser p, DeserializationContext ctxt) throws IOException {

		return null;
	}

}
