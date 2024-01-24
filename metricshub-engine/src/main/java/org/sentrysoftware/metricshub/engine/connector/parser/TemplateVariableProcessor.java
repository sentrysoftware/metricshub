package org.sentrysoftware.metricshub.engine.connector.parser;

import com.fasterxml.jackson.databind.JsonNode;
import java.io.IOException;
import java.util.HashMap;
import java.util.Map;
import java.util.function.Predicate;
import java.util.function.UnaryOperator;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import lombok.Builder;
import lombok.NonNull;

public class TemplateVariableProcessor extends AbstractNodeProcessor {

	@NonNull
	private Map<String, String> connectorVariables = new HashMap<>();

	@Builder
	public TemplateVariableProcessor(@NonNull Map<String, String> connectorVariables, AbstractNodeProcessor next) {
		super(next);
		this.connectorVariables = connectorVariables;
	}

	/**
	 * Processes a given Json node by calling {@link JsonNodeUpdater}
	 * @param node a given json node
	 * @return JsonNode
	 * @throws IOException thrown by {@link AbstractNodeProcessor}
	 */
	@Override
	public JsonNode processNode(JsonNode node) throws IOException {
		// Create the unary operator that replaces the template variable pattern by the agent config defined variable value
		final UnaryOperator<String> variableValueUpdater = value -> performReplacements(connectorVariables, value);

		// Create a predicate to check the matching with the template variable pattern
		final Predicate<String> isMatchingConnectorVariableRegex = str -> str != null && str.contains("${var::");

		// Call JsonNodeUpdater to replace the placeholder by the variable value
		JsonNodeUpdater
			.builder()
			.withJsonNode(node)
			.withPredicate(isMatchingConnectorVariableRegex)
			.withUpdater(variableValueUpdater)
			.build()
			.update();

		return node;
	}

	/**
	 * Replace placeholders in the given value with corresponding values from the provided
	 * key-value pairs in the replacements {@link Map}.
	 *
	 * @param replacements Key-value pairs representing placeholders and their replacement values.
	 *                     <br>Example: { $constants.query1=MyQuery1, $constants.query2=MyQuery2 }
	 * @param value        The string to be replaced.
	 * @return A new {@link String} with the placeholders replaced.
	 */
	private String performReplacements(final Map<String, String> replacements, String value) {
		if (value == null || value.isEmpty()) {
			return value;
		}

		// Loop over each placeholder and perform replacement
		for (final Map.Entry<String, String> entry : replacements.entrySet()) {
			final String key = entry.getKey();
			final Pattern pattern = Pattern.compile(String.format("\\$\\{var\\:\\:%s\\}", key));
			final Matcher matcher = pattern.matcher(value);
			while (matcher.find()) {
				value = value.replace(matcher.group(), entry.getValue());
			}
		}

		// return the new value
		return value;
	}
}
