package com.sentrysoftware.matrix.converter.state.source.common;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

import com.fasterxml.jackson.databind.JsonNode;
import com.sentrysoftware.matrix.converter.PreConnector;
import com.sentrysoftware.matrix.converter.state.AbstractStateConverter;

public class ForceSerializationProcessor extends AbstractStateConverter {

	private static final Pattern FORCE_SERIALIZATION_KEY_PATTERN = Pattern.compile(
		"^\\s*((.*)\\.(discovery|collect)\\.source\\(([1-9]\\d*)\\))\\.forceserialization\\s*$",
		Pattern.CASE_INSENSITIVE
	);

	@Override
	public Matcher getMatcher(String key) {
		return FORCE_SERIALIZATION_KEY_PATTERN.matcher(key);
	}

	@Override
	public void convert(String key, String value, JsonNode connector, PreConnector preConnector) {
		createSourceBooleanNode(key, value, connector, "forceSerialization");
	}

}
