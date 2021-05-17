package com.sentrysoftware.matrix.connector.parser.state.source.snmptable;

import com.sentrysoftware.matrix.connector.model.Connector;
import com.sentrysoftware.matrix.connector.model.monitor.job.source.type.snmp.SNMPGetTableSource;
import com.sentrysoftware.matrix.connector.parser.ConnectorParserConstants;

import java.util.Arrays;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class SnmpTableSelectColumnsProcessor extends SnmpTableProcessor {

	private static final Pattern SNMP_TABLE_SELECT_COLUMNS_KEY_PATTERN = Pattern.compile(
		"^\\s*((.*)\\.(discovery|collect)\\.source\\(([1-9]\\d*)\\))\\.snmptableselectcolumns\\s*$",
		Pattern.CASE_INSENSITIVE);

	@Override
	public Matcher getMatcher(String key) {
		return SNMP_TABLE_SELECT_COLUMNS_KEY_PATTERN.matcher(key);
	}

	@Override
	public void parse(final String key, final String value, final Connector connector) {

		super.parse(key, value, connector);

		((SNMPGetTableSource) getSource(key, connector)).setSnmpTableSelectColumns(
			Arrays.asList(value.split(ConnectorParserConstants.COMA)));
	}
}
