package com.sentrysoftware.matrix.converter.state.source.ucs;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

import com.fasterxml.jackson.databind.JsonNode;
import com.sentrysoftware.matrix.converter.PreConnector;
import com.sentrysoftware.matrix.converter.state.AbstractStateConverter;
import com.sentrysoftware.matrix.converter.state.ConversionHelper;

public class ExcludeRegExpProcessor extends AbstractStateConverter {

	private static final Pattern PATTERN = Pattern.compile(
			ConversionHelper.buildSourceKeyRegex("excluderegexp"),
			Pattern.CASE_INSENSITIVE);

	@Override
	protected Matcher getMatcher(String key) {
		return PATTERN.matcher(key);
	}

	@Override
	public void convert(String key, String value, JsonNode connector, PreConnector preConnector) {
		createSourceTextNode(key, value, connector, "exclude");
	}
}