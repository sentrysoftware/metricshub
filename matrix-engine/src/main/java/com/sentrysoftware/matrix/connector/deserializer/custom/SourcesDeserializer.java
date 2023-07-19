package com.sentrysoftware.matrix.connector.deserializer.custom;

import java.util.Map;

import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.DeserializationContext;
import com.sentrysoftware.matrix.connector.model.monitor.task.source.Source;

public class SourcesDeserializer extends AbstractLinkedHashMapDeserializer<Source> {

	@Override
	protected String messageOnInvalidMap(String nodeKey) {
		return String.format("The source key referenced by '%s' cannot be empty.", nodeKey);
	}

	@Override
	protected boolean isValidMap(Map<String, Source> map) {
		return map.keySet().stream().noneMatch(key -> key == null || key.isBlank());
	}

	@Override
	protected void updateMapValues(JsonParser parser, DeserializationContext ctxt, Map<String, Source> map) {
		map.forEach((key, source) -> source.setKey(String.format("%s.%s}", nodePath, key)));
	}

	@Override
	protected TypeReference<Map<String, Source>> getTypeReference() {
		 return new TypeReference<Map<String, Source>>() {};
	}

}
