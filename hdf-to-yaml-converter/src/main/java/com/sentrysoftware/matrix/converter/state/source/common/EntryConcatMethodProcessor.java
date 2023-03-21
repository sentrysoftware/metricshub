package com.sentrysoftware.matrix.converter.state.source.common;

import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.node.ObjectNode;
import com.fasterxml.jackson.databind.node.TextNode;
import com.sentrysoftware.matrix.converter.PreConnector;

public class EntryConcatMethodProcessor extends AbstractExecuteForEach {

	private static final Pattern ENTRY_CONCAT_METHOD_KEY_PATTERN = Pattern.compile(
		"^\\s*((.*)\\.(discovery|collect)\\.source\\(([1-9]\\d*)\\))\\.entryconcatmethod\\s*$",
		Pattern.CASE_INSENSITIVE
	);

	private static final Map<String, String> CONVERSIONS = Map.of(
		"list", "list",
		"jsonarray", "jsonArray",
		"jsonarrayextended", "jsonArrayExtended"
	);

	@Override
	public Matcher getMatcher(String key) {
		return ENTRY_CONCAT_METHOD_KEY_PATTERN.matcher(key);
	}

	@Override
	public void convert(String key, String value, JsonNode connector, PreConnector preConnector) {
		final ObjectNode executeForEachEntryOf = getOrCreateExecuteForEachEntryOf(key, connector);

		final String lowerCaseValue = value.toLowerCase().trim();
		final String concatMethodStr = CONVERSIONS.get(lowerCaseValue);
		if (concatMethodStr != null) {
			executeForEachEntryOf.set(CONCAT_METHOD, new TextNode(concatMethodStr));
		} else if ("custom".equals(lowerCaseValue)) {
			getOrCreateCustomConcatMethod(executeForEachEntryOf);
		} else {
			throw new IllegalStateException(String.format("Cannot determine the concatMethod identified with %s", value));
		}
	}

}
