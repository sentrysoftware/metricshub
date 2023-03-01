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

		final JsonNode constantsNode = node.get("constants");

		if (constantsNode != null && constantsNode.isObject()) {
			final List<String> constantKeys = new ArrayList<>(constantsNode.size());
			constantsNode.fieldNames().forEachRemaining(constantKeys::add);

			final Map<String, String> replacements = new HashMap<>();
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
	 * Perform replacements on the given value using the key-value pairs
	 * provided in the replacements {@link Map}
	 * 
	 * @param replacements Key-value pairs of placeholder to value to replace.
	 * E.g { $constants.query1=MyQuery1, $constants.query2=MyQuery2 }
	 * @param value to replace
	 * @return new {@link String} value
	 */
	private String performReplacements(Map<String, String> replacements, String value) {
		if (value == null) {
			return value;
		}

		// Loop over each placeholder and perform replacement
		for (Entry<String, String> entry : replacements.entrySet()) {
			String key = entry.getKey();
			String newValue = entry.getValue();
			if (value.contains(key)) {
				value = value.replace(key, newValue);
			}
		}

		// return the new value
		return value;
	}

	/**
	 * Traverse the given node and replace values
	 * 
	 * @param node {@link JsonNode} instance
	 * @param transformer value transformer function
	 */
	public static void replacePlaceholderValues(final JsonNode node, final UnaryOperator<String> transformer) {
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
					// No need to transform value if it doesn't have the placeholder
					replaceJsonNode(
						() -> ((ObjectNode) node).set(fieldName, new TextNode(transformer.apply(oldValue))),
						oldValue
					);
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
					// No need to transform value if it doesn't have the placeholder
					final int index = i;
					replaceJsonNode(
						() -> ((ArrayNode) node).set(index, new TextNode(transformer.apply(oldValue))),
						oldValue
					);
				}
			}
		}
	}

	/**
	 * Replace oldValue in {@link JsonNode} only if this oldValue matches the placeholder
	 * 
	 * @param replacer
	 * @param oldValue
	 */
	private static void replaceJsonNode(Runnable replacer, String oldValue) {
		if (oldValue.indexOf("$constants.") != -1) {
			replacer.run();
		}
	}
}