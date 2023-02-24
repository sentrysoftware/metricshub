package com.sentrysoftware.matrix.connector.deserializer.custom;

import java.util.HashMap;
import java.util.Map;

import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.DeserializationContext;

public class EmbeddedFilesDeserializer extends AbstractMapDeserializer<String> {

	@Override
	protected void updateMapValues(JsonParser parser, DeserializationContext ctxt, Map<String, String> map) {
		// No updates on the embedded files
	}

	@Override
	protected String messageOnInvalidMap(String nodeKey) {
		return String.format("The embedded file referenced by '%s' cannot be empty.", nodeKey);
	}

	@Override
	protected Map<String, String> fromMap(Map<String, String> map) {
		return new HashMap<>(map);
	}

	@Override
	protected boolean isExpectedInstance(Map<String, String> map) {
		// We want to force our HashMap because the LinkedHashMap is a sub class of HashMap
		// Using "map instanceof HashMap" will always return true since Jackson
		// creates by default a LinkedHashMap.
		return false;
	}

	@Override
	protected Map<String, String> emptyMap() {
		return new HashMap<>();
	}

	@Override
	protected boolean isValidMap(Map<String, String> map) {
		return map
			.entrySet()
			.stream()
			.noneMatch(entry -> 
				entry.getKey() == null
				|| entry.getKey().isBlank()
				|| entry.getValue() == null
			);
	}

	@Override
	protected TypeReference<Map<String, String>> getTypeReference() {
		 return new TypeReference<Map<String, String>>() {};
	}

}
