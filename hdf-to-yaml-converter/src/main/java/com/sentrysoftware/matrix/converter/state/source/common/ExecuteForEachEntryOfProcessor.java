package com.sentrysoftware.matrix.converter.state.source.common;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.node.ObjectNode;
import com.sentrysoftware.matrix.converter.PreConnector;
import com.sentrysoftware.matrix.converter.state.ConversionHelper;

public class ExecuteForEachEntryOfProcessor extends AbstractExecuteForEach {

	private static final Pattern PATTERN = Pattern.compile(
		ConversionHelper.buildSourceKeyRegex("executeforeachentryof"),
		Pattern.CASE_INSENSITIVE
	);

	@Override
	public Matcher getMatcher(String key) {
		return PATTERN.matcher(key);
	}

	@Override
	public void convert(String key, String value, JsonNode connector, PreConnector preConnector) {
		final ObjectNode executeForEachEntryOf = getOrCreateExecuteForEachEntryOf(key, connector);
		createTextNode("source", value, executeForEachEntryOf);
	}

}