package com.sentrysoftware.matrix.connector.parser.state.detection.snmp;

import com.sentrysoftware.matrix.connector.model.detection.criteria.snmp.Snmp;
import com.sentrysoftware.matrix.connector.parser.state.AbstractStateParser;

public abstract class SnmpProcessor extends AbstractStateParser {

	protected static final String SNMP_TYPE_VALUE = "SNMP";

	@Override
	public Class<Snmp> getType() {
		return Snmp.class;
	}

	@Override
	public String getTypeValue() {
		return SNMP_TYPE_VALUE;
	}
}
