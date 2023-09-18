package com.sentrysoftware.matrix.converter.state.detection.http;

import com.fasterxml.jackson.databind.JsonNode;
import com.sentrysoftware.matrix.converter.PreConnector;
import com.sentrysoftware.matrix.converter.state.ConversionHelper;
import com.sentrysoftware.matrix.converter.state.common.AbstractHttpConverter;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class ResultContentProcessor extends AbstractHttpConverter {

	private static final Pattern PATTERN = Pattern.compile(
		ConversionHelper.buildCriteriaKeyRegex("resultcontent"),
		Pattern.CASE_INSENSITIVE
	);

	@Override
	public void convert(String key, String value, JsonNode connector, PreConnector preConnector) {
		createCriterionTextNode(key, extractResultContent(key, value), connector, "resultContent");
	}

	@Override
	protected Matcher getMatcher(String key) {
		return PATTERN.matcher(key);
	}
}
