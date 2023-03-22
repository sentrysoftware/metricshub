package com.sentrysoftware.matrix.converter.state.source.http;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

import com.fasterxml.jackson.databind.JsonNode;
import com.sentrysoftware.matrix.converter.PreConnector;
import com.sentrysoftware.matrix.converter.state.ConversionHelper;
import com.sentrysoftware.matrix.converter.state.common.AbstractHttpConverter;

public class MethodProcessor extends AbstractHttpConverter {

	private static final Pattern PATTERN = Pattern.compile(
		ConversionHelper.buildSourceKeyRegex("method"),
		Pattern.CASE_INSENSITIVE
	);

	@Override
	public void convert(String key, String value, JsonNode connector, PreConnector preConnector) {
		createSourceTextNode(key, extractHttpMethod(key, value), connector, "method");
	}

	@Override
	protected Matcher getMatcher(String key) {
		return PATTERN.matcher(key);
	}
}