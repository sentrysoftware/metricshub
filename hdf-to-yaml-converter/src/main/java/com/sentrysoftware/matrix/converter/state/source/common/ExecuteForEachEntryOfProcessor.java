package com.sentrysoftware.matrix.converter.state.source.common;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.node.ObjectNode;
import com.sentrysoftware.matrix.converter.PreConnector;

public class ExecuteForEachEntryOfProcessor extends AbstractExecuteForEach {

	private static final Pattern EXECUTE_FOR_EACH_ENTRY_OF_KEY_PATTERN = Pattern.compile(
		"^\\s*((.*)\\.(discovery|collect)\\.source\\(([1-9]\\d*)\\))\\.executeforeachentryof\\s*$",
		Pattern.CASE_INSENSITIVE
	);

	@Override
	public Matcher getMatcher(String key) {
		return EXECUTE_FOR_EACH_ENTRY_OF_KEY_PATTERN.matcher(key);
	}

	@Override
	public void convert(String key, String value, JsonNode connector, PreConnector preConnector) {
		final ObjectNode executeForEachEntryOf = getOrCreateExecuteForEachEntryOf(key, connector);
		createTextNode("source", value, executeForEachEntryOf);
	}

}
