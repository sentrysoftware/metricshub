package com.sentrysoftware.matrix.converter.state.source.snmp;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

import com.fasterxml.jackson.databind.JsonNode;
import com.sentrysoftware.matrix.converter.PreConnector;
import com.sentrysoftware.matrix.converter.state.AbstractStateConverter;
import com.sentrysoftware.matrix.converter.state.ConversionHelper;

public class SnmpOidProcessor extends AbstractStateConverter  {

	private static final Pattern SNMP_OID_KEY_PATTERN = Pattern.compile(
		ConversionHelper.buildSourceKeyRegex("snmpoid"),
		Pattern.CASE_INSENSITIVE
	);

	@Override
	public void convert(String key, String value, JsonNode connector, PreConnector preConnector) {
		createSourceTextNode(key, value, connector, "oid");
	}

	@Override
	protected Matcher getMatcher(String key) {
		return SNMP_OID_KEY_PATTERN.matcher(key);
	}

}
