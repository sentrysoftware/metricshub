package com.sentrysoftware.matrix.connector.parser.state.detection.snmp;

import com.sentrysoftware.matrix.connector.model.detection.criteria.snmp.SNMP;
import com.sentrysoftware.matrix.connector.parser.state.AbstractStateParser;

public abstract class SnmpProcessor extends AbstractStateParser {

	protected static final String SNMP_TYPE_VALUE = "SNMP";

	@Override
	public Class<SNMP> getType() {
		return SNMP.class;
	}

	@Override
	public String getTypeValue() {
		return SNMP_TYPE_VALUE;
	}
}
