package com.sentrysoftware.matrix.connector.parser.state.compute.keeponlymatchinglines;

import com.sentrysoftware.matrix.connector.model.Connector;
import com.sentrysoftware.matrix.connector.model.monitor.job.source.compute.KeepOnlyMatchingLines;

import java.util.Arrays;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import static com.sentrysoftware.matrix.connector.parser.ConnectorParserConstants.COMMA;

public class ValueListProcessor extends KeepOnlyMatchingLinesProcessor {

	private static final Pattern VALUE_LIST_KEY_PATTERN = Pattern.compile(
		"^\\s*((.*)\\.(discovery|collect)\\.source\\(([1-9]\\d*)\\))\\.compute\\(([1-9]\\d*)\\)\\.valuelist\\s*$",
		Pattern.CASE_INSENSITIVE);

	@Override
	public Matcher getMatcher(String key) {
		return VALUE_LIST_KEY_PATTERN.matcher(key);
	}

	@Override
	public void parse(final String key, final String value, final Connector connector) {

		super.parse(key, value, connector);

		((KeepOnlyMatchingLines) getCompute(key, connector)).setValueList(Arrays.asList(value.split(COMMA)));
	}
}
