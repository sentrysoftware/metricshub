package com.sentrysoftware.metricshub.converter.state.computes.json2csv;

import com.fasterxml.jackson.databind.JsonNode;
import com.sentrysoftware.metricshub.converter.PreConnector;
import com.sentrysoftware.metricshub.converter.state.AbstractStateConverter;
import com.sentrysoftware.metricshub.converter.state.ConversionHelper;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class EntryKeyProcessor extends AbstractStateConverter {

	private static final Pattern PATTERN = Pattern.compile(
		ConversionHelper.buildComputeKeyRegex("entrykey"),
		Pattern.CASE_INSENSITIVE
	);

	@Override
	public void convert(String key, String value, JsonNode connector, PreConnector preConnector) {
		createComputeTextNode(key, value, connector, "entryKey");
	}

	@Override
	protected Matcher getMatcher(String key) {
		return PATTERN.matcher(key);
	}
}
