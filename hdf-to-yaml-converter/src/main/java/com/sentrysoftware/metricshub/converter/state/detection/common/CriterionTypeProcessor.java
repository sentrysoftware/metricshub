package com.sentrysoftware.metricshub.converter.state.detection.common;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.node.ArrayNode;
import com.fasterxml.jackson.databind.node.JsonNodeFactory;
import com.fasterxml.jackson.databind.node.ObjectNode;
import com.sentrysoftware.metricshub.converter.PreConnector;
import com.sentrysoftware.metricshub.converter.state.AbstractStateConverter;
import com.sentrysoftware.metricshub.converter.state.ConversionHelper;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import lombok.AllArgsConstructor;
import lombok.Getter;

@AllArgsConstructor
public class CriterionTypeProcessor extends AbstractStateConverter {

	@Getter
	private final String hdfType;

	private final String yamlType;

	private static final Pattern PATTERN = Pattern.compile(
		ConversionHelper.buildCriteriaKeyRegex("type"),
		Pattern.CASE_INSENSITIVE
	);

	@Override
	public void convert(String key, String value, JsonNode connector, PreConnector preConnector) {
		final ArrayNode criteria = getOrCreateCriteria(connector);

		final ObjectNode criterion = JsonNodeFactory.instance.objectNode();

		appendComment(key, preConnector, criterion);

		createTextNode("type", yamlType, criterion);

		criteria.add(criterion);
	}

	@Override
	public Matcher getMatcher(String key) {
		return PATTERN.matcher(key);
	}
}
