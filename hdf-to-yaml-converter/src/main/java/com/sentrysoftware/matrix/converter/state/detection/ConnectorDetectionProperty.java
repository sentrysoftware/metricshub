package com.sentrysoftware.matrix.converter.state.detection;

import java.util.Set;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import com.sentrysoftware.matrix.converter.state.IConnectorStateConverter;
import com.sentrysoftware.matrix.converter.state.detection.devicetype.ConnectorDeviceTypeProperty;
import com.sentrysoftware.matrix.converter.state.detection.http.ConnectorHttpProperty;
import com.sentrysoftware.matrix.converter.state.detection.ipmi.ConnectorIpmiProperty;
import com.sentrysoftware.matrix.converter.state.detection.oscommand.ConnectorOSCommandProperty;
import com.sentrysoftware.matrix.converter.state.detection.process.ConnectorProcessProperty;
import com.sentrysoftware.matrix.converter.state.detection.productrequirements.ConnectorProductRequirementsProperty;
import com.sentrysoftware.matrix.converter.state.detection.service.ConnectorServiceProperty;
import com.sentrysoftware.matrix.converter.state.detection.snmp.ConnectorSnmpProperty;
import com.sentrysoftware.matrix.converter.state.detection.ucs.ConnectorUcsProperty;
import com.sentrysoftware.matrix.converter.state.detection.wbem.ConnectorWbemProperty;
import com.sentrysoftware.matrix.converter.state.detection.wmi.ConnectorWmiProperty;

import lombok.AccessLevel;
import lombok.NoArgsConstructor;

@NoArgsConstructor(access = AccessLevel.PRIVATE)
public class ConnectorDetectionProperty {

	public static Set<IConnectorStateConverter> getConnectorProperties() {

		return Stream.of(
			ConnectorHttpProperty.getConnectorProperties(),
			ConnectorSnmpProperty.getConnectorProperties(),
			ConnectorIpmiProperty.getConnectorProperties(),
			ConnectorWbemProperty.getConnectorProperties(),
			ConnectorWmiProperty.getConnectorProperties(),
			ConnectorOSCommandProperty.getConnectorProperties(),
			ConnectorDeviceTypeProperty.getConnectorProperties(),
			ConnectorProcessProperty.getConnectorProperties(),
			ConnectorProductRequirementsProperty.getConnectorProperties(),
			ConnectorServiceProperty.getConnectorProperties(),
			ConnectorUcsProperty.getConnectorProperties()
		)
		.flatMap(Set::stream)
		.collect(Collectors.toSet());
	}
}
