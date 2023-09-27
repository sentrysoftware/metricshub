package com.sentrysoftware.matrix.connector.deserializer.custom;

import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.DeserializationContext;
import com.fasterxml.jackson.databind.JsonDeserializer;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.exc.InvalidFormatException;
import com.sentrysoftware.matrix.connector.model.common.ITranslationTable;
import com.sentrysoftware.matrix.connector.model.common.ReferenceTranslationTable;
import com.sentrysoftware.matrix.connector.model.common.TranslationTable;
import java.io.IOException;
import java.util.TreeMap;

public class TranslationTableDeserializer extends JsonDeserializer<ITranslationTable> {

	@Override
	public ITranslationTable deserialize(JsonParser parser, DeserializationContext context) throws IOException {
		if (parser == null) {
			return null;
		}

		final String key = parser.getCurrentName();
		final JsonNode node = parser.readValueAsTree();

		if (node != null) {
			if (node.isTextual()) {
				return new ReferenceTranslationTable(node.asText());
			} else if (node.isObject()) {
				return new TranslationTable(
					new ObjectMapper().convertValue(node, new TypeReference<TreeMap<String, String>>() {})
				);
			}
		}

		throw new InvalidFormatException(
			parser,
			String.format("Invalid translation table value encountered for property '%s'.", key),
			node,
			JsonNode.class
		);
	}
}
