package com.sentrysoftware.matrix.connector.parser.state.source.snmptable;

import com.sentrysoftware.matrix.connector.model.monitor.job.source.type.snmp.SNMPGetTableSource;
import com.sentrysoftware.matrix.connector.parser.state.IConnectorStateParser;
import com.sentrysoftware.matrix.connector.parser.state.source.common.ForceSerializationProcessor;
import com.sentrysoftware.matrix.connector.parser.state.source.common.TypeProcessor;

import java.util.Set;
import java.util.stream.Collectors;
import java.util.stream.Stream;

public class ConnectorSnmpTableProperty {

	private ConnectorSnmpTableProperty() {
	}

	public static Set<IConnectorStateParser> getConnectorProperties() {

		return Stream
			.of(
				new TypeProcessor(SNMPGetTableSource.class, SnmpTableProcessor.SNMP_TABLE_TYPE_VALUE),
				new ForceSerializationProcessor(SNMPGetTableSource.class, SnmpTableProcessor.SNMP_TABLE_TYPE_VALUE),
				new SnmpTableOidProcessor(),
				new SnmpTableSelectColumnsProcessor())
			.collect(Collectors.toSet());
	}
}
