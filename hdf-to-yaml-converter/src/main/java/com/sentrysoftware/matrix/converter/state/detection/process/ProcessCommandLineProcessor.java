package com.sentrysoftware.matrix.converter.state.detection.process;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

import com.fasterxml.jackson.databind.JsonNode;
import com.sentrysoftware.matrix.converter.PreConnector;
import com.sentrysoftware.matrix.converter.state.AbstractStateConverter;

public class ProcessCommandLineProcessor extends AbstractStateConverter {

	private static final Pattern COMMANDLINE_KEY_PATTERN = Pattern.compile(
			"^\\s*detection\\.criteria\\(([1-9]\\d*)\\)\\.processcommandline\\s*$",
			Pattern.CASE_INSENSITIVE);

	@Override
	public void convert(String key, String value, JsonNode connector, PreConnector preConnector) {
		createCriterionTextNode(key, value, connector, "commandLine");
	}

	@Override
	protected Matcher getMatcher(String key) {
		return COMMANDLINE_KEY_PATTERN.matcher(key);
	}
}