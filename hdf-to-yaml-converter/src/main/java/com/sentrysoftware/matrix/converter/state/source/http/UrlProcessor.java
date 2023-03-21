package com.sentrysoftware.matrix.converter.state.source.http;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

import com.fasterxml.jackson.databind.JsonNode;
import com.sentrysoftware.matrix.converter.PreConnector;
import com.sentrysoftware.matrix.converter.state.AbstractStateConverter;

public class UrlProcessor extends AbstractStateConverter {

	private static final Pattern PATTERN = Pattern.compile(
		SOURCE_REGEX_START + "url" + REGEX_END,
		Pattern.CASE_INSENSITIVE
	);

	@Override
	public void convert(String key, String value, JsonNode connector, PreConnector preConnector) {
		createSourceTextNode(key, value, connector, "url");
	}

	@Override
	protected Matcher getMatcher(String key) {
		return PATTERN.matcher(key);
	}
}
