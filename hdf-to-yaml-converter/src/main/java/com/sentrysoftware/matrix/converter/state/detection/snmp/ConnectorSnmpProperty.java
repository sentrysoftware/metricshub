package com.sentrysoftware.matrix.converter.state.detection.snmp;

import java.util.Set;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import com.sentrysoftware.matrix.converter.state.IConnectorStateConverter;
import com.sentrysoftware.matrix.converter.state.detection.common.ExpectedResultProcessor;
import com.sentrysoftware.matrix.converter.state.detection.common.ForceSerializationProcessor;
import com.sentrysoftware.matrix.converter.state.detection.common.TypeProcessor;

public class ConnectorSnmpProperty {

	private ConnectorSnmpProperty() {}

	private static final String SNMP_HDF_TYPE_VALUE = "SNMP";
	private static final String SNMP_YAML_TYPE_VALUE = "snmp";

	public static Set<IConnectorStateConverter> getConnectorProperties() {

		return Stream.of(
			new TypeProcessor(SNMP_HDF_TYPE_VALUE, SNMP_YAML_TYPE_VALUE),
			new OidProcessor(),
			new ExpectedResultProcessor(),
			new ForceSerializationProcessor()
		)
		.collect(Collectors.toSet());
	}
}
