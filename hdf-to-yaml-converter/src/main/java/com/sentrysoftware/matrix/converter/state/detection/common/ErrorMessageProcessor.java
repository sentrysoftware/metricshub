package com.sentrysoftware.matrix.converter.state.detection.common;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

import com.fasterxml.jackson.databind.JsonNode;
import com.sentrysoftware.matrix.converter.PreConnector;
import com.sentrysoftware.matrix.converter.state.AbstractStateConverter;

import lombok.AllArgsConstructor;

@AllArgsConstructor
public class ErrorMessageProcessor extends AbstractStateConverter {

	private static final Pattern ERROR_MESSAGE_KEY_PATTERN = Pattern.compile(
		"^\\s*detection\\.criteria\\(([1-9]\\d*)\\)\\.errormessage\\s*$",
		Pattern.CASE_INSENSITIVE
	);

	@Override
	protected Matcher getMatcher(String key) {
		return ERROR_MESSAGE_KEY_PATTERN.matcher(key);
	}

	@Override
	public void convert(final String key, final String value, final JsonNode connector, PreConnector preConnector) {
		createCriterionTextNode(key, value, connector, "errorMessage");
	}
}

