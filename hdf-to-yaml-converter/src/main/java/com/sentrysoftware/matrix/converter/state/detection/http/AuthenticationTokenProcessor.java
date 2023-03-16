package com.sentrysoftware.matrix.converter.state.detection.http;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

import com.fasterxml.jackson.databind.JsonNode;
import com.sentrysoftware.matrix.converter.PreConnector;
import com.sentrysoftware.matrix.converter.state.AbstractStateConverter;

public class AuthenticationTokenProcessor extends AbstractStateConverter {

	private static final Pattern AUTHENTICATION_TOKEN_KEY_PATTERN = Pattern.compile(
		"^\\s*detection\\.criteria\\(([1-9]\\d*)\\)\\.authenticationtoken\\s*$",
		Pattern.CASE_INSENSITIVE
	);

	@Override
	public void convert(String key, String value, JsonNode connector, PreConnector preConnector) {
		createCriterionTextNode(key, value, connector, "authenticationToken");
	}

	@Override
	protected Matcher getMatcher(String key) {
		return AUTHENTICATION_TOKEN_KEY_PATTERN.matcher(key);
	}

}