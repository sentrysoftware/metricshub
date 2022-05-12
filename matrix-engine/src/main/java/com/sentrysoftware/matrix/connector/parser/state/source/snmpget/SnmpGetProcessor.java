package com.sentrysoftware.matrix.connector.parser.state.source.snmpget;


import com.sentrysoftware.matrix.connector.model.monitor.job.source.type.snmp.SNMPGetSource;
import com.sentrysoftware.matrix.connector.parser.state.AbstractStateParser;

public abstract class SnmpGetProcessor extends AbstractStateParser {

	protected static final String SNMP_GET_TYPE_VALUE = "snmpget";

	@Override
	public Class<SNMPGetSource> getType() {
		return SNMPGetSource.class;
	}

	@Override
	public String getTypeValue() {
		return SNMP_GET_TYPE_VALUE;
	}
}
