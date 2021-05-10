package com.sentrysoftware.matrix.connector.parser.state.detection.snmp;

import java.util.Set;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import com.sentrysoftware.matrix.connector.parser.state.IConnectorStateParser;

public class ConnectorSnmpProperty {

	private ConnectorSnmpProperty() {}

	public static Set<IConnectorStateParser> getConnectorProperties() {

		return Stream
				.of(new OidProcessor(), new ExpectedResultProcessor(), new ForceSerializationProcessor())
				.collect(Collectors.toSet());
	}
}
