package com.sentrysoftware.matrix.converter.state.detection.common;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.node.JsonNodeFactory;
import com.fasterxml.jackson.databind.node.ObjectNode;
import com.sentrysoftware.matrix.converter.PreConnector;
import com.sentrysoftware.matrix.converter.state.AbstractStateConverter;

import lombok.AllArgsConstructor;

@AllArgsConstructor
public class ForceSerializationProcessor extends AbstractStateConverter {

	private static final Pattern FORCE_SERIALIZATION_KEY_PATTERN = Pattern.compile(
		"^\\s*detection\\.criteria\\(([1-9]\\d*)\\)\\.forceserialization\\s*$",
		Pattern.CASE_INSENSITIVE
	);

	@Override
	public void convert(String key, String value, JsonNode connector, PreConnector preConnector) {
		((ObjectNode) getLastCriterion(key, connector))
			.set("forceSerialization", JsonNodeFactory.instance.booleanNode(Boolean.valueOf(value.trim())));
	}

	@Override
	protected Matcher getMatcher(String key) {
		return FORCE_SERIALIZATION_KEY_PATTERN.matcher(key);
	}
}