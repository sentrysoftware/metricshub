package org.sentrysoftware.metricshub.engine.connector.parser;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.node.ArrayNode;
import com.fasterxml.jackson.databind.node.ObjectNode;
import com.fasterxml.jackson.databind.node.TextNode;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Objects;
import java.util.function.Predicate;
import java.util.function.UnaryOperator;

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
				replacements.put(key, child.asText());
			}

			final UnaryOperator<String> transformer = value -> performReplacements(replacements, value);

			final Predicate<String> replacementPredicate = Objects::nonNull;
			replacePlaceholderValues(node, transformer, replacementPredicate);

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
		if (value == null || value.isEmpty()) {
			return value;
		}

		// Loop over each placeholder and perform replacement
		for (final Entry<String, String> entry : replacements.entrySet()) {
			final String key = entry.getKey();
			if (value.contains(key)) {
				value = value.replace(key, entry.getValue());
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
	 * @param replacementPredicate replacement predicate
	 */
	public static void replacePlaceholderValues(
		final JsonNode node,
		final UnaryOperator<String> transformer,
		final Predicate<String> replacementPredicate
	) {
		if (node.isObject()) {
			// Get JsonNode fields
			final List<String> fieldNames = new ArrayList<>(node.size());
			node.fieldNames().forEachRemaining(fieldNames::add);

			// Get the corresponding JsonNode for each field
			for (final String fieldName : fieldNames) {
				final JsonNode child = node.get(fieldName);

				// Means it wrap sub JsonNode(s)
				if (child.isContainerNode()) {
					replacePlaceholderValues(child, transformer, replacementPredicate);
				} else {
					// Perform the replacement
					final String oldValue = child.asText();
					// No need to transform value if it doesn't have the placeholder
					replaceJsonNode(
						() -> ((ObjectNode) node).set(fieldName, new TextNode(transformer.apply(oldValue))),
						oldValue,
						replacementPredicate
					);
				}
			}
		} else if (node.isArray()) {
			// Loop over the array and get each JsonNode element
			for (int i = 0; i < node.size(); i++) {
				final JsonNode child = node.get(i);

				// Means this node is a JsonNode element
				if (child.isContainerNode()) {
					replacePlaceholderValues(child, transformer, replacementPredicate);
				} else {
					// Means this is a simple array node
					final String oldValue = child.asText();
					// No need to transform value if it doesn't have the placeholder
					final int index = i;
					replaceJsonNode(
						() -> ((ArrayNode) node).set(index, new TextNode(transformer.apply(oldValue))),
						oldValue,
						replacementPredicate
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
	private static void replaceJsonNode(Runnable replacer, String oldValue, Predicate<String> replacementPredicate) {
		if (replacementPredicate.test(oldValue)) {
			replacer.run();
		}
	}
}
