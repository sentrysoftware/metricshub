package com.sentrysoftware.matrix.converter.state.detection.devicetype;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

import com.fasterxml.jackson.databind.JsonNode;
import com.sentrysoftware.matrix.converter.PreConnector;
import com.sentrysoftware.matrix.converter.state.AbstractStateConverter;

public class KeepOnlyProcessor extends AbstractStateConverter {

	private static final Pattern KEEPONLY_KEY_PATTERN = Pattern.compile(
			"^\\s*detection\\.criteria\\(([1-9]\\d*)\\)\\.keeponly\\s*$",
			Pattern.CASE_INSENSITIVE);

	@Override
	public void convert(String key, String value, JsonNode connector, PreConnector preConnector) {
		createCriterionTextNode(key, value, connector, "keep");
	}

	@Override
	protected Matcher getMatcher(String key) {
		return KEEPONLY_KEY_PATTERN.matcher(key);
	}
}