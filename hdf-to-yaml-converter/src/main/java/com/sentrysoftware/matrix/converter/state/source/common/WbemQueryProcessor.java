package com.sentrysoftware.matrix.converter.state.source.common;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

import com.fasterxml.jackson.databind.JsonNode;
import com.sentrysoftware.matrix.converter.PreConnector;
import com.sentrysoftware.matrix.converter.state.AbstractStateConverter;

import lombok.AllArgsConstructor;

@AllArgsConstructor
public class WbemQueryProcessor extends AbstractStateConverter {

	private static final Pattern WBEM_NAMESPACE_KEY_PATTERN = Pattern.compile(
		"^\\s*((.*)\\.(discovery|collect)\\.source\\(([1-9]\\d*)\\))\\.wbemquery\\s*$",
		Pattern.CASE_INSENSITIVE
	);

	@Override
	protected Matcher getMatcher(String key) {
		return WBEM_NAMESPACE_KEY_PATTERN.matcher(key);
	}

	@Override
	public void convert(String key, String value, JsonNode connector, PreConnector preConnector) {
		createSourceTextNode(key, value, connector, "query");
	}
}