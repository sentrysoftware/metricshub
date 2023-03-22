package com.sentrysoftware.matrix.converter.state.source.snmp;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

import com.fasterxml.jackson.databind.JsonNode;
import com.sentrysoftware.matrix.converter.PreConnector;
import com.sentrysoftware.matrix.converter.state.AbstractStateConverter;

public class SelectColumnsProcessor extends AbstractStateConverter  {

	private static final Pattern SELECT_COLUMNS_KEY_PATTERN = Pattern.compile(
			"^\\s*((.*)\\.(discovery|collect)\\.source\\(([1-9]\\d*)\\))\\.snmptableselectcolumns\\s*$",
			Pattern.CASE_INSENSITIVE
		);

	@Override
	public void convert(String key, String value, JsonNode connector, PreConnector preConnector) {
		createSourceTextNode(key, value, connector, "selectColumns");
	}

	@Override
	protected Matcher getMatcher(String key) {
		return SELECT_COLUMNS_KEY_PATTERN.matcher(key);
	}

}
