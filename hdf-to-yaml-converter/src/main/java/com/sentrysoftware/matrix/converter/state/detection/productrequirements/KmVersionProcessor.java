package com.sentrysoftware.matrix.converter.state.detection.productrequirements;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

import com.fasterxml.jackson.databind.JsonNode;
import com.sentrysoftware.matrix.converter.PreConnector;
import com.sentrysoftware.matrix.converter.state.AbstractStateConverter;

public class KmVersionProcessor extends AbstractStateConverter {

	private static final Pattern KMVERSION_KEY_PATTERN = Pattern.compile(
			"^\\s*detection\\.criteria\\(([1-9]\\d*)\\)\\.version\\s*$",
			Pattern.CASE_INSENSITIVE);

	@Override
	public void convert(String key, String value, JsonNode connector, PreConnector preConnector) {
		createCriterionTextNode(key, value, connector, "kmVersion");
	}

	@Override
	protected Matcher getMatcher(String key) {
		return KMVERSION_KEY_PATTERN.matcher(key);
	}
}