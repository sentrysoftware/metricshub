package com.sentrysoftware.matrix.converter.state.detection.devicetype;

import java.util.Arrays;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import com.fasterxml.jackson.databind.JsonNode;
import com.sentrysoftware.matrix.converter.PreConnector;
import com.sentrysoftware.matrix.converter.state.AbstractStateConverter;

public class ExcludeProcessor extends AbstractStateConverter {

	private static final Pattern EXCLUDE_KEY_PATTERN = Pattern.compile(
			"^\\s*detection\\.criteria\\(([1-9]\\d*)\\)\\.exclude\\s*$",
			Pattern.CASE_INSENSITIVE);

	@Override
	public void convert(String key, String value, JsonNode connector, PreConnector preConnector) {
		createCriterionStringArrayNode(
			key, 
			Arrays
				.stream(value.split(","))
				.map(String::trim).
				toArray(String[]::new),
			connector,
			"exclude"
		);
	}

	@Override
	protected Matcher getMatcher(String key) {
		return EXCLUDE_KEY_PATTERN.matcher(key);
	}
}