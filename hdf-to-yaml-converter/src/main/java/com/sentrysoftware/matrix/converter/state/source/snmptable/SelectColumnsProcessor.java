package com.sentrysoftware.matrix.converter.state.source.snmptable;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

import com.fasterxml.jackson.databind.JsonNode;
import com.sentrysoftware.matrix.converter.PreConnector;
import com.sentrysoftware.matrix.converter.state.AbstractStateConverter;
import com.sentrysoftware.matrix.converter.state.ConversionHelper;

public class SelectColumnsProcessor extends AbstractStateConverter  {

	private static final Pattern SELECT_COLUMNS_KEY_PATTERN = Pattern.compile(
		ConversionHelper.buildSourceKeyRegex("snmptableselectcolumns"),
		Pattern.CASE_INSENSITIVE
	);

	@Override
	public void convert(String key, String value, JsonNode connector, PreConnector preConnector) {
		createSourceTextNode(key, value, connector, "selectColumns");
	}

	@Override
	protected Matcher getMatcher(String key) {
		return SELECT_COLUMNS_KEY_PATTERN.matcher(key);
	}

}
