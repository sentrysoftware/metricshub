package com.sentrysoftware.metricshub.engine.connector.deserializer.custom;

import java.util.LinkedHashMap;
import java.util.Map;

public abstract class AbstractLinkedHashMapDeserializer<T> extends AbstractMapDeserializer<T> {

	@Override
	protected Map<String, T> emptyMap() {
		return new LinkedHashMap<>();
	}

	@Override
	protected Map<String, T> fromMap(Map<String, T> map) {
		return new LinkedHashMap<>(map);
	}

	@Override
	protected boolean isExpectedInstance(Map<String, T> map) {
		return map instanceof LinkedHashMap;
	}
}
