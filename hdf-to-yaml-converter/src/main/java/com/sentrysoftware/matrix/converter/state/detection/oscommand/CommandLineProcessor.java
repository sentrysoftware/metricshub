package com.sentrysoftware.matrix.converter.state.detection.oscommand;

import java.util.regex.Pattern;

import com.fasterxml.jackson.databind.JsonNode;
import com.sentrysoftware.matrix.converter.PreConnector;

public class CommandLineProcessor extends OSCommandProcessor {

	private static final Pattern COMMANDLINE_KEY_PATTERN = Pattern.compile(
			"^\\s*detection\\.criteria\\(([1-9]\\d*)\\)\\.commandline\\s*$",
			Pattern.CASE_INSENSITIVE);

	@Override
	public boolean detect(String key, String value, JsonNode connector) {
		// TODO Auto-generated method stub
		return false;
	}

	@Override
	public void convert(String key, String value, JsonNode connector, PreConnector preConnector) {
		// TODO Auto-generated method stub
		
	}

}
