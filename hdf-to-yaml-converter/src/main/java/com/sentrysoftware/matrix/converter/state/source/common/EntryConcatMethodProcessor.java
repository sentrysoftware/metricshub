package com.sentrysoftware.matrix.converter.state.source.common;

import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.node.ObjectNode;
import com.fasterxml.jackson.databind.node.TextNode;
import com.sentrysoftware.matrix.connector.model.common.EntryConcatMethod;
import com.sentrysoftware.matrix.converter.PreConnector;
import com.sentrysoftware.matrix.converter.state.ConversionHelper;

public class EntryConcatMethodProcessor extends AbstractExecuteForEach {

	private static final Pattern PATTERN = Pattern.compile(
		ConversionHelper.buildSourceKeyRegex("entryconcatmethod"),
		Pattern.CASE_INSENSITIVE
	);

	private static final Map<String, String> CONVERSIONS = EntryConcatMethod
		.ENUM_VALUES
		.stream()
		.collect(Collectors.toMap(k -> k.name().toLowerCase(), EntryConcatMethod::getName));

	@Override
	public Matcher getMatcher(String key) {
		return PATTERN.matcher(key);
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
