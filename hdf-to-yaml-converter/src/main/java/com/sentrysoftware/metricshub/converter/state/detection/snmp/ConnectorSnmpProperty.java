package com.sentrysoftware.metricshub.converter.state.detection.snmp;

import com.sentrysoftware.metricshub.converter.state.IConnectorStateConverter;
import com.sentrysoftware.metricshub.converter.state.detection.common.ExpectedResultProcessor;
import com.sentrysoftware.metricshub.converter.state.detection.common.ForceSerializationProcessor;
import java.util.Set;
import java.util.stream.Collectors;
import java.util.stream.Stream;

public class ConnectorSnmpProperty {

	private ConnectorSnmpProperty() {}

	public static Set<IConnectorStateConverter> getConnectorProperties() {
		return Stream
			.of(new OidProcessor(), new ExpectedResultProcessor(), new ForceSerializationProcessor())
			.collect(Collectors.toSet());
	}
}
