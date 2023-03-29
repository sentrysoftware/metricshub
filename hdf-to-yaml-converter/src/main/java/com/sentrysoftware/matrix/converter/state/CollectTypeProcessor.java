package com.sentrysoftware.matrix.converter.state;

import java.util.regex.Pattern;

import com.fasterxml.jackson.databind.JsonNode;
import com.sentrysoftware.matrix.converter.PreConnector;

public class CollectTypeProcessor implements IConnectorStateConverter {

	private static final Pattern PATTERN = Pattern.compile("^\\s*(([a-z]+)\\.(collect)\\.(type))\\s*$", Pattern.CASE_INSENSITIVE);

	@Override
	public boolean detect(String key, String value, JsonNode connector) {
		return value != null
				&& key != null
				&& PATTERN.matcher(key).matches();
	}

	@Override
	public void convert(String key, String value, JsonNode connector, PreConnector preConnector) {
		// TODO
	}

}
