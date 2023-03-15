package com.sentrysoftware.matrix.converter.state.detection.oscommand;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

import com.fasterxml.jackson.databind.JsonNode;
import com.sentrysoftware.matrix.converter.PreConnector;
import com.sentrysoftware.matrix.converter.state.AbstractStateConverter;

public class CommandLineProcessor extends AbstractStateConverter {

	private static final Pattern COMMANDLINE_KEY_PATTERN = Pattern.compile(
		"^\\s*detection\\.criteria\\(([1-9]\\d*)\\)\\.commandline\\s*$",
		Pattern.CASE_INSENSITIVE
	);

	@Override
	public void convert(String key, String value, JsonNode connector, PreConnector preConnector) {
		// TODO Implement
	}

	@Override
	protected Matcher getMatcher(String key) {
		return COMMANDLINE_KEY_PATTERN.matcher(key);
	}

}
