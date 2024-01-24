package org.sentrysoftware.metricshub.engine.connector.deserializer.custom;

import java.util.LinkedHashMap;
import java.util.Map;

/**
 * An abstract class providing deserialization support for custom types using Jackson's ObjectMapper
 * and representing the deserialization of a {@link LinkedHashMap} instance.
 *
 * @param <T> The type of values stored in the map.
 */
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
