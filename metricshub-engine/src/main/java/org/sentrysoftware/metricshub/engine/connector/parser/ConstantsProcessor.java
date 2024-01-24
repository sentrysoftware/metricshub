package org.sentrysoftware.metricshub.engine.connector.parser;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.node.ObjectNode;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Objects;
import java.util.function.Predicate;
import java.util.function.UnaryOperator;
import lombok.Data;
import lombok.EqualsAndHashCode;

@Data
@EqualsAndHashCode(callSuper = true)
public class ConstantsProcessor extends AbstractNodeProcessor {

	/**
	 * Constructs a ConstantsProcessor with a next processor.
	 */
	private ConstantsProcessor(AbstractNodeProcessor next) {
		super(next);
	}

	/**
	 * Constructs a ConstantsProcessor without a next processor.
	 */
	public ConstantsProcessor() {
		this(null);
	}

	@Override
	public JsonNode processNode(JsonNode node) {
		final JsonNode constantsNode = node.get("constants");

		if (constantsNode != null && constantsNode.isObject()) {
			final List<String> constantKeys = new ArrayList<>(constantsNode.size());
			constantsNode.fieldNames().forEachRemaining(constantKeys::add);

			final Map<String, String> replacements = new HashMap<>();
			for (String key : constantKeys) {
				final JsonNode child = constantsNode.get(key);
				replacements.put(key, child.asText());
			}

			final UnaryOperator<String> updater = value -> performReplacements(replacements, value);

			final Predicate<String> replacementPredicate = Objects::nonNull;

			JsonNodeUpdater
				.builder()
				.withJsonNode(node)
				.withPredicate(replacementPredicate)
				.withUpdater(updater)
				.build()
				.update();

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
	private String performReplacements(final Map<String, String> replacements, String value) {
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
}
