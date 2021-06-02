package com.sentrysoftware.matrix.connector.parser.state.compute.awk;

import static com.sentrysoftware.matrix.connector.parser.ConnectorParserConstants.COMMA;

import java.util.Arrays;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import com.sentrysoftware.matrix.connector.model.Connector;
import com.sentrysoftware.matrix.connector.model.monitor.job.source.compute.Awk;

public class SelectColumnsProcessor extends AwkProcessor {
	private static final Pattern SELECT_COLUMNS_KEY_PATTERN = Pattern.compile(
			"^\\s*((.*)\\.(discovery|collect)\\.source\\(([1-9]\\d*)\\))\\.compute\\(([1-9]\\d*)\\)\\.selectcolumns\\s*$",
			Pattern.CASE_INSENSITIVE);

	@Override
	public Matcher getMatcher(String key) {
		return SELECT_COLUMNS_KEY_PATTERN.matcher(key);
	}

	@Override
	public void parse(final String key, final String value, final Connector connector) {

		super.parse(key, value, connector);

		((Awk) getCompute(key, connector)).setSelectColumns(Arrays.asList(value.split(COMMA)));
	}
}
