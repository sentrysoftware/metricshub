package com.sentrysoftware.matrix.connector.parser.state.source.snmpget;

import java.util.Set;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import com.sentrysoftware.matrix.connector.model.monitor.job.source.type.snmp.SNMPGetSource;
import com.sentrysoftware.matrix.connector.parser.state.IConnectorStateParser;
import com.sentrysoftware.matrix.connector.parser.state.source.common.ForceSerializationProcessor;
import com.sentrysoftware.matrix.connector.parser.state.source.common.TypeProcessor;

public class ConnectorSnmpGetProperty {

	private ConnectorSnmpGetProperty() {
	}

	public static Set<IConnectorStateParser> getConnectorProperties() {

		return Stream
			.of(
				new TypeProcessor(SNMPGetSource.class, SnmpGetProcessor.SNMP_GET_TYPE_VALUE),
				new ForceSerializationProcessor(SNMPGetSource.class, SnmpGetProcessor.SNMP_GET_TYPE_VALUE),
				new SnmpGetOidProcessor())
			.collect(Collectors.toSet());
	}
}
