package com.sentrysoftware.matrix.converter.state.detection.oscommand;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

import com.fasterxml.jackson.databind.JsonNode;
import com.sentrysoftware.matrix.converter.PreConnector;
import com.sentrysoftware.matrix.converter.state.AbstractStateConverter;

public class ExecuteLocallyProcessor extends AbstractStateConverter {

	private static final Pattern EXECUTELOCALLY_KEY_PATTERN = Pattern.compile(
		"^\\s*detection\\.criteria\\(([1-9]\\d*)\\)\\.executelocally\\s*$",
		Pattern.CASE_INSENSITIVE
	);

	@Override
	public void convert(String key, String value, JsonNode connector, PreConnector preConnector) {
		createCriterionBooleanNode(key, value, connector, "executeLocally");
	}

	@Override
	protected Matcher getMatcher(String key) {
		return EXECUTELOCALLY_KEY_PATTERN.matcher(key);
	}
}
