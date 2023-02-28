package com.sentrysoftware.matrix.connector.parser;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.function.UnaryOperator;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.node.ArrayNode;
import com.fasterxml.jackson.databind.node.ObjectNode;
import com.fasterxml.jackson.databind.node.TextNode;

public class ConstantsProcessor implements NodeProcessor {

	@Override
	public JsonNode process(JsonNode node) {

		JsonNode constantsNode = node.get("constants");

		if (constantsNode != null && constantsNode.isObject()) {
			final List<String> constantKeys = new ArrayList<>(constantsNode.size());
			constantsNode.fieldNames().forEachRemaining(constantKeys::add);

			Map<String, String> replacements = new HashMap<>();
			for (String key : constantKeys) {
				final JsonNode child = constantsNode.get(key);
				replacements.put(String.format("$constants.%s", key), child.asText());
			}

			final UnaryOperator<String> transformer = value ->  performReplacements(replacements, value);

			replacePlaceholderValues(node, transformer);

			((ObjectNode) node).remove("constants");
		}

		return node;
	}

	/**
	 * 
	 * @param replacements
	 * @param value
	 * @return
	 */
	private String performReplacements(Map<String, String> replacements, String value) {
		if (value == null) {
			return value;
		}

		for (Entry<String, String> entry : replacements.entrySet()) {
			String key = entry.getKey();
			String newValue = entry.getValue();
			if (value.contains(key)) {
				value = value.replace(key, newValue);
			}
		}

		return value;
	}

	/**
	 * 
	 * @param node
	 * @param transformer
	 */
	public static void replacePlaceholderValues(JsonNode node, UnaryOperator<String> transformer) {
		if (node.isObject()) {
			// Get JsonNode fields
			final List<String> fieldNames = new ArrayList<>(node.size());
			node.fieldNames().forEachRemaining(fieldNames::add);

			// Get the corresponding JsonNode for each field
			for (String fieldName : fieldNames) {
				final JsonNode child = node.get(fieldName);
				// Means it wrap sub JsonNode(s)
				if (child.isContainerNode()) {
					replacePlaceholderValues(child, transformer);
				} else {
					// Perform the replacement
					final String oldValue = child.asText();
					((ObjectNode) node).set(fieldName, new TextNode(transformer.apply(oldValue)));
				}
			}
		} else if (node.isArray()) {
			// Loop over the array and get each JsonNode element 
			for (int i = 0; i < node.size(); i++) {
				final JsonNode child = node.get(i);
				// Means this node is a JsonNode element
				if (child.isContainerNode()) {
					replacePlaceholderValues(child, transformer);
				} else {
					// Means this is a simple array node
					final String oldValue = child.asText();
					((ArrayNode) node).set(i, new TextNode(transformer.apply(oldValue)));
				}
			}
		}
	}
}