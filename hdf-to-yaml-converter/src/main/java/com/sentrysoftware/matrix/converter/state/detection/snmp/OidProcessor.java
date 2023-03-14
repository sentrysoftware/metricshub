package com.sentrysoftware.matrix.converter.state.detection.snmp;

import java.util.regex.Pattern;

import com.fasterxml.jackson.databind.JsonNode;
import com.sentrysoftware.matrix.converter.PreConnector;

public class OidProcessor extends SnmpProcessor {

	private static final String SNMP_GET_OID_KEY = ".snmpget";

	private static final Pattern OID_KEY_PATTERN = Pattern.compile(
		"^\\s*detection\\.criteria\\(([1-9]\\d*)\\)\\.snmpget(next)?\\s*$",
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
