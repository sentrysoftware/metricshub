package com.sentrysoftware.matrix.converter.state.source.common;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.node.ObjectNode;
import com.sentrysoftware.matrix.converter.PreConnector;
import com.sentrysoftware.matrix.converter.state.AbstractStateConverter;
import com.sentrysoftware.matrix.converter.state.ConversionHelper;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import lombok.AllArgsConstructor;
import lombok.Getter;

@AllArgsConstructor
public class TypeProcessor extends AbstractStateConverter {

	@Getter
	private final String hdfType;

	@Getter
	private final String yamlType;

	private static final Pattern PATTERN = Pattern.compile(
		ConversionHelper.buildSourceKeyRegex("type"),
		Pattern.CASE_INSENSITIVE
	);

	@Override
	public Matcher getMatcher(String key) {
		return PATTERN.matcher(key);
	}

	@Override
	public void convert(String key, String value, JsonNode connector, PreConnector preConnector) {
		final ObjectNode source = createSource(key, connector);

		appendComment(key, preConnector, source);

		createTextNode("type", yamlType, source);
	}
}
