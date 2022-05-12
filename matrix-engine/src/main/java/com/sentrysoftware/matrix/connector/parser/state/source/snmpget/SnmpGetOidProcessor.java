package com.sentrysoftware.matrix.connector.parser.state.source.snmpget;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

import com.sentrysoftware.matrix.connector.model.Connector;
import com.sentrysoftware.matrix.connector.model.monitor.job.source.type.snmp.SNMPGetSource;

public class SnmpGetOidProcessor extends SnmpGetProcessor {

	private static final Pattern SNMP_GET_OID_KEY_PATTERN = Pattern.compile(
		"^\\s*((.*)\\.(discovery|collect)\\.source\\(([1-9]\\d*)\\))\\.snmpoid\\s*$",
		Pattern.CASE_INSENSITIVE);

	@Override
	public Matcher getMatcher(String key) {
		return SNMP_GET_OID_KEY_PATTERN.matcher(key);
	}

	@Override
	public void parse(final String key, final String value, final Connector connector) {

		super.parse(key, value, connector);

		((SNMPGetSource) getSource(key, connector)).setOid(value);
	}
}
