package com.sentrysoftware.metricshub.converter.state.instance;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.node.JsonNodeFactory;
import com.fasterxml.jackson.databind.node.ObjectNode;
import com.sentrysoftware.metricshub.converter.PreConnector;
import com.sentrysoftware.metricshub.converter.state.AbstractStateConverter;
import com.sentrysoftware.metricshub.converter.state.ConversionHelper;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class InstanceProcessor extends AbstractStateConverter {

	private static final Pattern PATTERN = Pattern.compile(
		"^\\s*(([a-z]+)\\.discovery\\.instance\\.(parameteractivation\\.[a-z0-9]+|[a-z0-9]+))\\s*$",
		Pattern.CASE_INSENSITIVE
	);

	@Override
	public boolean detect(String key, String value, JsonNode connector) {
		return value != null && key != null && PATTERN.matcher(key).matches();
	}

	@Override
	public void convert(String key, String value, JsonNode connector, PreConnector preConnector) {
		final ObjectNode attributes = getOrCreateAttributes(key, connector);

		final String attribute = getMappingAttribute(key);

		attributes.set(attribute, JsonNodeFactory.instance.textNode(ConversionHelper.performValueConversions(value)));
	}

	@Override
	protected Matcher getMatcher(String key) {
		return PATTERN.matcher(key);
	}
}
