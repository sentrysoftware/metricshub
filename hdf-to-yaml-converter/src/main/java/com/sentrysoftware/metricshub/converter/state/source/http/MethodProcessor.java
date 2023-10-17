package com.sentrysoftware.metricshub.converter.state.source.http;

import com.fasterxml.jackson.databind.JsonNode;
import com.sentrysoftware.metricshub.converter.PreConnector;
import com.sentrysoftware.metricshub.converter.state.ConversionHelper;
import com.sentrysoftware.metricshub.converter.state.common.AbstractHttpConverter;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

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
