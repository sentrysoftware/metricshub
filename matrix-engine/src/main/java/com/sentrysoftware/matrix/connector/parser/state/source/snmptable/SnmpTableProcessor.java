package com.sentrysoftware.matrix.connector.parser.state.source.snmptable;


import com.sentrysoftware.matrix.connector.model.monitor.job.source.type.snmp.SnmpGetTableSource;
import com.sentrysoftware.matrix.connector.parser.state.AbstractStateParser;

public abstract class SnmpTableProcessor extends AbstractStateParser {

	protected static final String SNMP_TABLE_TYPE_VALUE = "SnmpTable";

	@Override
	public Class<SnmpGetTableSource> getType() {
		return SnmpGetTableSource.class;
	}

	@Override
	public String getTypeValue() {
		return SNMP_TABLE_TYPE_VALUE;
	}
}
