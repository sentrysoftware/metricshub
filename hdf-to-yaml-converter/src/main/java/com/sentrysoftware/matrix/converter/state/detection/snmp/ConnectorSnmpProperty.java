package com.sentrysoftware.matrix.converter.state.detection.snmp;

import java.util.Set;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import com.sentrysoftware.matrix.converter.state.IConnectorStateConverter;
import com.sentrysoftware.matrix.converter.state.detection.common.ExpectedResultProcessor;
import com.sentrysoftware.matrix.converter.state.detection.common.ForceSerializationProcessor;

public class ConnectorSnmpProperty {

	private ConnectorSnmpProperty() {}

	public static Set<IConnectorStateConverter> getConnectorProperties() {

		return Stream
				.of(
					new OidProcessor(),
					new ExpectedResultProcessor(SnmpProcessor.SNMP_TYPE_VALUE),
					new ForceSerializationProcessor(SnmpProcessor.SNMP_TYPE_VALUE))
				.collect(Collectors.toSet());
	}
}
