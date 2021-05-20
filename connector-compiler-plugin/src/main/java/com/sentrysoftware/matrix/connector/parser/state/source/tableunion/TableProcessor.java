package com.sentrysoftware.matrix.connector.parser.state.source.tableunion;

import com.sentrysoftware.matrix.connector.model.Connector;
import com.sentrysoftware.matrix.connector.model.monitor.job.source.type.tableunion.TableUnionSource;
import com.sentrysoftware.matrix.connector.parser.ConnectorParserConstants;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class TableProcessor extends TableUnionProcessor {

	private static final Pattern TABLE_KEY_PATTERN = Pattern.compile(
		"^\\s*((.*)\\.(discovery|collect)\\.source\\(([1-9]\\d*)\\))\\.table([1-9]\\d*)\\s*$",
		Pattern.CASE_INSENSITIVE);

	@Override
	public Matcher getMatcher(String key) {
		return TABLE_KEY_PATTERN.matcher(key);
	}

	@Override
	public void parse(final String key, final String value, final Connector connector) {

		super.parse(key, value, connector);

		((TableUnionSource) getSource(key, connector))
			.getTables()
			.add(value.replaceAll(ConnectorParserConstants.SOURCE_REFERENCE_REGEX_REPLACEMENT, "$1"));
	}
}
