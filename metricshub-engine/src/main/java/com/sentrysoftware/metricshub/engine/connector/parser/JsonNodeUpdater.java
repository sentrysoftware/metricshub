package com.sentrysoftware.metricshub.engine.connector.parser;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.node.ArrayNode;
import com.fasterxml.jackson.databind.node.ObjectNode;
import com.fasterxml.jackson.databind.node.TextNode;
import java.util.ArrayList;
import java.util.List;
import java.util.function.Predicate;
import java.util.function.UnaryOperator;
import lombok.Builder;
import lombok.NonNull;
import lombok.RequiredArgsConstructor;

/**
 * This utility class traverses a JsonNode, applying updates according to an
 * updater function and a predicate that determines whether the value should be
 * updated.
 */
@Builder(setterPrefix = "with")
@RequiredArgsConstructor
public class JsonNodeUpdater {

	@NonNull
	private final JsonNode jsonNode;

	@NonNull
	private final UnaryOperator<String> updater;

	@NonNull
	private final Predicate<String> predicate;

	/**
	 * Traverse the current JsonNode, applying the updater to each JsonNode child
	 * when the predicate evaluates to true, indicating that the value should be updated.
	 */
	public void update() {
		update(jsonNode);
	}

	/**
	 * Traverse the current JsonNode, applying the updater to each JsonNode child
	 * when the predicate evaluates to true, indicating that the value should be updated.
	 *
	 * @param node the {@link JsonNode} to update
	 */
	private void update(final JsonNode node) {
		if (node == null) {
			return;
		}

		if (node.isObject()) {
			// Get JsonNode fields
			final List<String> fieldNames = new ArrayList<>(node.size());
			node.fieldNames().forEachRemaining(fieldNames::add);

			// Get the corresponding JsonNode for each field
			for (final String fieldName : fieldNames) {
				final JsonNode child = node.get(fieldName);

				// Means it wrap sub JsonNode(s)
				if (child.isContainerNode()) {
					update(child);
				} else {
					// Perform the replacement
					final String oldValue = child.asText();
					// Transformation of the value is unnecessary if it lacks the placeholder
					runUpdate(() -> ((ObjectNode) node).set(fieldName, new TextNode(updater.apply(oldValue))), oldValue);
				}
			}
		} else if (node.isArray()) {
			// Loop over the array and get each JsonNode element
			for (int i = 0; i < node.size(); i++) {
				final JsonNode child = node.get(i);

				// Means this node is a JsonNode element
				if (child.isContainerNode()) {
					update(child);
				} else {
					// Means this is a simple array node
					final String oldValue = child.asText();
					// Transformation of the value is unnecessary if it lacks the placeholder
					final int index = i;
					runUpdate(() -> ((ArrayNode) node).set(index, new TextNode(updater.apply(oldValue))), oldValue);
				}
			}
		}
	}

	/**
	 * Run the update only if the value matches the replacement predicate
	 *
	 * @param update Runnable function, actually the function performing the update
	 * @param value  Value to check
	 */
	private void runUpdate(final Runnable update, final String value) {
		if (predicate.test(value)) {
			update.run();
		}
	}
}
