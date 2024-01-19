package com.sentrysoftware.metricshub.agent.deserialization;

import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.DeserializationContext;
import com.fasterxml.jackson.databind.JsonDeserializer;
import com.sentrysoftware.metricshub.engine.common.helpers.StringHelper;
import java.io.IOException;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * AttributesDeserializer is a custom JSON deserializer for converting JSON attributes into a {@link Map<String, String>} format.
 * It parses a JSON object, deserializes its attributes, and converts them into a map where keys are strings and values are stringified representations.
 */
public class AttributesDeserializer extends JsonDeserializer<Map<String, String>> {

	/**
	 * Deserialize JSON attributes into a {@link Map<String, String>} format.
	 *
	 * @param parser  JSON parser.
	 * @param context Deserialization context.
	 * @return A map representing the deserialized attributes.
	 * @throws IOException If an I/O error occurs during deserialization.
	 */
	@Override
	public Map<String, String> deserialize(JsonParser parser, DeserializationContext context) throws IOException {
		if (parser == null) {
			return new HashMap<>();
		}

		final Map<String, Object> deserializedAttributes = parser.readValueAs(new TypeReference<Map<String, Object>>() {});

		return deserializedAttributes
			.entrySet()
			.stream()
			.collect(
				Collectors.toMap(
					Map.Entry::getKey,
					value -> StringHelper.stringify(value.getValue()),
					(oldValue, newValue) -> oldValue,
					LinkedHashMap::new
				)
			);
	}
}
