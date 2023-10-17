package com.sentrysoftware.metricshub.converter.state.valuetable;

import static com.sentrysoftware.metricshub.converter.ConverterConstants.COLLECT;
import static com.sentrysoftware.metricshub.converter.ConverterConstants.SOURCE;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.node.JsonNodeFactory;
import com.fasterxml.jackson.databind.node.ObjectNode;
import com.sentrysoftware.metricshub.converter.PreConnector;
import com.sentrysoftware.metricshub.converter.state.AbstractStateConverter;
import com.sentrysoftware.metricshub.converter.state.ConversionHelper;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class ValueTableProcessor extends AbstractStateConverter {

	private static final Pattern VALUE_TABLE_KEY_PATTERN = Pattern.compile(
		"^\\s*(([a-z]+)\\.collect\\.valuetable)\\s*$",
		Pattern.CASE_INSENSITIVE
	);

	@Override
	public boolean detect(String key, String value, JsonNode connector) {
		return value != null && key != null && getMatcher(key).matches();
	}

	@Override
	public void convert(String key, String value, JsonNode connector, PreConnector preConnector) {
		final ObjectNode mapping = getOrCreateMapping(key, connector, COLLECT);

		appendComment(key, preConnector, mapping);

		mapping.set(SOURCE, JsonNodeFactory.instance.textNode(ConversionHelper.performValueConversions(value)));
	}

	@Override
	protected Matcher getMatcher(String key) {
		return VALUE_TABLE_KEY_PATTERN.matcher(key);
	}
}
