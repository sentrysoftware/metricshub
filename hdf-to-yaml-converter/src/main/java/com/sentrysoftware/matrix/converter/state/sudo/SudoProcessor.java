package com.sentrysoftware.matrix.converter.state.sudo;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.node.ArrayNode;
import com.fasterxml.jackson.databind.node.JsonNodeFactory;
import com.fasterxml.jackson.databind.node.ObjectNode;
import com.sentrysoftware.matrix.converter.PreConnector;
import com.sentrysoftware.matrix.converter.state.AbstractStateConverter;
import com.sentrysoftware.matrix.converter.state.ConversionHelper;

public class SudoProcessor extends AbstractStateConverter {

	private static final String SUDO_COMMANDS = "sudoCommands";
	
	private static final Pattern PATTERN = Pattern.compile(
		"^\\s*sudo\\(([1-9]\\d*)\\).command\\s*$",
		Pattern.CASE_INSENSITIVE
	);

	@Override
	public boolean detect(String key, String value, JsonNode connector) {
		return value != null
				&& key != null
				&& PATTERN.matcher(key).matches();
	}

	@Override
	public void convert(String key, String value, JsonNode connector, PreConnector preConnector) {
		if(connector.has(SUDO_COMMANDS)) {
			((ArrayNode) connector.get(SUDO_COMMANDS)).add(ConversionHelper.performValueConversions(value));
		} else {
			((ObjectNode)connector).set(SUDO_COMMANDS, JsonNodeFactory.instance.arrayNode().add(ConversionHelper.performValueConversions(value)));
		}
	}

	@Override
	protected Matcher getMatcher(String key) {
		return PATTERN.matcher(key);
	}
}