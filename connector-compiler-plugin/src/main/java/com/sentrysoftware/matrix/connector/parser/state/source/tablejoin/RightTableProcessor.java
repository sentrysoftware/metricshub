package com.sentrysoftware.matrix.connector.parser.state.source.tablejoin;

import com.sentrysoftware.matrix.connector.model.Connector;
import com.sentrysoftware.matrix.connector.model.monitor.job.source.type.tablejoin.TableJoinSource;
import com.sentrysoftware.matrix.connector.parser.ConnectorParserConstants;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class RightTableProcessor extends TableJoinProcessor {

	private static final Pattern RIGHT_TABLE_KEY_PATTERN = Pattern.compile(
		"^\\s*((.*)\\.(discovery|collect)\\.source\\(([1-9]\\d*)\\))\\.righttable\\s*$",
		Pattern.CASE_INSENSITIVE);

	@Override
	public Matcher getMatcher(String key) {
		return RIGHT_TABLE_KEY_PATTERN.matcher(key);
	}

	@Override
	public void parse(final String key, final String value, final Connector connector) {

		super.parse(key, value, connector);

		((TableJoinSource) getSource(key, connector)).setRightTable(
			value.replaceAll(ConnectorParserConstants.SOURCE_REFERENCE_REGEX_REPLACEMENT, "$1"));
	}
}
