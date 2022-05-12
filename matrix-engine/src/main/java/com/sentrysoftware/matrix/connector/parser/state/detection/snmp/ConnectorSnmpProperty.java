package com.sentrysoftware.matrix.connector.parser.state.detection.snmp;

import java.util.Set;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import com.sentrysoftware.matrix.connector.model.detection.criteria.snmp.Snmp;
import com.sentrysoftware.matrix.connector.parser.state.IConnectorStateParser;
import com.sentrysoftware.matrix.connector.parser.state.detection.common.ExpectedResultProcessor;
import com.sentrysoftware.matrix.connector.parser.state.detection.common.ForceSerializationProcessor;

public class ConnectorSnmpProperty {

	private ConnectorSnmpProperty() {}

	public static Set<IConnectorStateParser> getConnectorProperties() {

		return Stream
				.of(
					new OidProcessor(),
					new ExpectedResultProcessor(Snmp.class, SnmpProcessor.SNMP_TYPE_VALUE),
					new ForceSerializationProcessor(Snmp.class, SnmpProcessor.SNMP_TYPE_VALUE))
				.collect(Collectors.toSet());
	}
}
