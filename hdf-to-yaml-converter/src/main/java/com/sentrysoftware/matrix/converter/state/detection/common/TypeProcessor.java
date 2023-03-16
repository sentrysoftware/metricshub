package com.sentrysoftware.matrix.converter.state.detection.common;

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

		final ArrayNode criteria = getOrCreateCriteria(connector);

		final ObjectNode criterion = JsonNodeFactory.instance.objectNode();
		if (preConnector.getComments().containsKey(key)) {
			final String comments = preConnector.getComments().get(key).stream().collect(Collectors.joining("\n"));
			createTextNode("_comment", comments, criterion);
		}

		createTextNode("type", yamlType, criterion);

		criteria.add(criterion);
	}

	@Override
	public Matcher getMatcher(String key) {
		return TYPE_KEY_PATTERN.matcher(key);
	}

}