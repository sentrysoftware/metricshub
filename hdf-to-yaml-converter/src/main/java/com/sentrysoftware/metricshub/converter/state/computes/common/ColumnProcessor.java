package com.sentrysoftware.metricshub.converter.state.computes.common;

import com.fasterxml.jackson.databind.JsonNode;
import com.sentrysoftware.metricshub.converter.PreConnector;
import com.sentrysoftware.metricshub.converter.state.AbstractStateConverter;
import com.sentrysoftware.metricshub.converter.state.ConversionHelper;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class ColumnProcessor extends AbstractStateConverter {

	private static final Pattern PATTERN = Pattern.compile(
		ConversionHelper.buildComputeKeyRegex("column"),
		Pattern.CASE_INSENSITIVE
	);

	@Override
	public void convert(String key, String value, JsonNode connector, PreConnector preConnector) {
		createComputeIntegerNode(key, value, connector, "column");
	}

	@Override
	protected Matcher getMatcher(String key) {
		return PATTERN.matcher(key);
	}
}
