package com.sentrysoftware.matrix.connector.deserializer.custom;

import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.DeserializationContext;
import java.util.Map;
import java.util.TreeMap;

public class CaseInsensitiveTreeMapDeserializer extends AbstractMapDeserializer<String> {

	@Override
	protected void updateMapValues(JsonParser parser, DeserializationContext ctxt, Map<String, String> map) {
		// No updates
	}

	@Override
	protected String messageOnInvalidMap(String nodeKey) {
		return String.format("The key referenced by '%s' cannot be empty.", nodeKey);
	}

	@Override
	protected Map<String, String> fromMap(Map<String, String> map) {
		final Map<String, String> treeMap = new TreeMap<>(String.CASE_INSENSITIVE_ORDER);
		treeMap.putAll(map);
		return treeMap;
	}

	@Override
	protected boolean isExpectedInstance(Map<String, String> map) {
		return map instanceof TreeMap;
	}

	@Override
	protected Map<String, String> emptyMap() {
		return new TreeMap<>(String.CASE_INSENSITIVE_ORDER);
	}

	@Override
	protected boolean isValidMap(Map<String, String> map) {
		return map.keySet().stream().noneMatch(key -> key == null || key.isBlank());
	}

	@Override
	protected TypeReference<Map<String, String>> getTypeReference() {
		return new TypeReference<Map<String, String>>() {};
	}
}
