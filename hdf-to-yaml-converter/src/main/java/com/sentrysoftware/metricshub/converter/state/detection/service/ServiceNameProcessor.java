package com.sentrysoftware.metricshub.converter.state.detection.service;

import com.fasterxml.jackson.databind.JsonNode;
import com.sentrysoftware.metricshub.converter.PreConnector;
import com.sentrysoftware.metricshub.converter.state.AbstractStateConverter;
import com.sentrysoftware.metricshub.converter.state.ConversionHelper;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class ServiceNameProcessor extends AbstractStateConverter {

	private static final Pattern PATTERN = Pattern.compile(
		ConversionHelper.buildCriteriaKeyRegex("servicename"),
		Pattern.CASE_INSENSITIVE
	);

	@Override
	public void convert(String key, String value, JsonNode connector, PreConnector preConnector) {
		createCriterionTextNode(key, value, connector, "name");
	}

	@Override
	protected Matcher getMatcher(String key) {
		return PATTERN.matcher(key);
	}
}
