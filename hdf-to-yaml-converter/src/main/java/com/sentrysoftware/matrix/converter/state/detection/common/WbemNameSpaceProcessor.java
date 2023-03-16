package com.sentrysoftware.matrix.converter.state.detection.common;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

import com.fasterxml.jackson.databind.JsonNode;
import com.sentrysoftware.matrix.converter.PreConnector;
import com.sentrysoftware.matrix.converter.state.AbstractStateConverter;

import lombok.AllArgsConstructor;

@AllArgsConstructor
public class WbemNameSpaceProcessor extends AbstractStateConverter {

	private static final Pattern WBEM_NAMESPACE_KEY_PATTERN = Pattern.compile(
		"^\\s*detection\\.criteria\\(([1-9]\\d*)\\)\\.wbemnamespace\\s*$",
		Pattern.CASE_INSENSITIVE
	);


	@Override
	public void convert(String key, String value, JsonNode connector, PreConnector preConnector) {
		createCriterionTextNode(key, value, connector, "namespace");
	}


	@Override
	protected Matcher getMatcher(String key) {
		return WBEM_NAMESPACE_KEY_PATTERN.matcher(key);
	}
}
