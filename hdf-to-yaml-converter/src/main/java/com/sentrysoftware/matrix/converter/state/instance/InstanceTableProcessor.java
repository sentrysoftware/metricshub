package com.sentrysoftware.matrix.converter.state.instance;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.node.JsonNodeFactory;
import com.fasterxml.jackson.databind.node.ObjectNode;
import com.sentrysoftware.matrix.converter.PreConnector;
import com.sentrysoftware.matrix.converter.state.AbstractStateConverter;
import com.sentrysoftware.matrix.converter.state.ConversionHelper;

import static com.sentrysoftware.matrix.converter.ConverterConstants.SOURCE;

public class InstanceTableProcessor extends AbstractStateConverter {

	/**
	 * Pattern to detect discovery InstanceTable
	 */
	private static final Pattern INSTANCE_TABLE_PATTERN = Pattern.compile(
		"^\\s*(([a-z]+)\\.discovery\\.instancetable)\\s*$",
		Pattern.CASE_INSENSITIVE
	);

	@Override
	public boolean detect(String key, String value, JsonNode connector) {
		return value != null
			&& key != null
			&& getMatcher(key).matches();
	}

	@Override
	public void convert(String key, String value, JsonNode connector, PreConnector preConnector) {
		ObjectNode mapping = getOrCreateMapping(key, connector);

		appendComment(key, preConnector, mapping);

		mapping.set(SOURCE, JsonNodeFactory.instance.textNode(ConversionHelper.performValueConversions(value)));
	}

	@Override
	protected Matcher getMatcher(String key) {
		return INSTANCE_TABLE_PATTERN.matcher(key);
	}
}