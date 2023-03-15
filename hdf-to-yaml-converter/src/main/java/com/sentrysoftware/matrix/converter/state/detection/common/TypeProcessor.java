package com.sentrysoftware.matrix.converter.state.detection.common;

import static com.sentrysoftware.matrix.converter.ConverterConstants.CONNECTOR;
import static com.sentrysoftware.matrix.converter.ConverterConstants.CRITERIA;
import static com.sentrysoftware.matrix.converter.ConverterConstants.DETECTION;

import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.node.ArrayNode;
import com.fasterxml.jackson.databind.node.JsonNodeFactory;
import com.fasterxml.jackson.databind.node.ObjectNode;
import com.sentrysoftware.matrix.converter.PreConnector;
import com.sentrysoftware.matrix.converter.state.AbstractStateConverter;

import lombok.AllArgsConstructor;
import lombok.Getter;

@AllArgsConstructor
public class TypeProcessor extends AbstractStateConverter {

	@Getter
	private final String hdfType;
	private final String yamlType;

	private static final Pattern TYPE_KEY_PATTERN = Pattern.compile(
		"^\\s*detection\\.criteria\\(([1-9]\\d*)\\)\\.type\\s*$",
		Pattern.CASE_INSENSITIVE
	);

	@Override
	public void convert(String key, String value, JsonNode connector, PreConnector preConnector) {

		final JsonNode criteria = getOrCreateCriteria(connector);

		final JsonNode criterion = JsonNodeFactory.instance.objectNode();
		if (preConnector.getComments().containsKey(key)) {
			final String comments = preConnector.getComments().get(key).stream().collect(Collectors.joining("\n"));
			((ObjectNode) criterion).set("_comment", JsonNodeFactory.instance.textNode(comments));
		}

		((ObjectNode) criterion).set("type", JsonNodeFactory.instance.textNode(yamlType));
		((ArrayNode) criteria).add(criterion);
	}

	/**
	 * 
	 * @param connector
	 * @return
	 */
	public static JsonNode getOrCreateCriteria(final JsonNode connector) {
		JsonNode connectorSection = connector.get(CONNECTOR);
		if (connectorSection == null) {
			connectorSection = JsonNodeFactory.instance.objectNode();
			((ObjectNode) connector).set(CONNECTOR, connectorSection);
		}

		JsonNode detection = connectorSection.get(DETECTION);
		if (detection == null) {
			detection = JsonNodeFactory.instance.objectNode();
			((ObjectNode) connectorSection).set(DETECTION, detection);
		}

		JsonNode criteria = detection.get(CRITERIA);
		if (criteria == null) {
			criteria = JsonNodeFactory.instance.arrayNode();
			((ObjectNode) detection).set(CRITERIA, criteria);
		}
		return criteria;
	}

	@Override
	public Matcher getMatcher(String key) {
		return TYPE_KEY_PATTERN.matcher(key);
	}

}
