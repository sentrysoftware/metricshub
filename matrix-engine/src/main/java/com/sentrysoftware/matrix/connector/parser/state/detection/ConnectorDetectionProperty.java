package com.sentrysoftware.matrix.connector.parser.state.detection;

import com.sentrysoftware.matrix.connector.parser.state.IConnectorStateParser;
import com.sentrysoftware.matrix.connector.parser.state.detection.http.ConnectorHttpProperty;
import com.sentrysoftware.matrix.connector.parser.state.detection.ipmi.ConnectorIpmiProperty;
import com.sentrysoftware.matrix.connector.parser.state.detection.os.ConnectorOsProperty;
import com.sentrysoftware.matrix.connector.parser.state.detection.oscommand.ConnectorOSCommandProperty;
import com.sentrysoftware.matrix.connector.parser.state.detection.process.ConnectorProcessProperty;
import com.sentrysoftware.matrix.connector.parser.state.detection.service.ConnectorServiceProperty;
import com.sentrysoftware.matrix.connector.parser.state.detection.snmp.ConnectorSnmpProperty;
import com.sentrysoftware.matrix.connector.parser.state.detection.sshinteractive.ConnectorSshInteractiveProperty;
import com.sentrysoftware.matrix.connector.parser.state.detection.wbem.ConnectorWbemProperty;
import com.sentrysoftware.matrix.connector.parser.state.detection.wmi.ConnectorWmiProperty;

import java.util.Set;
import java.util.stream.Collectors;
import java.util.stream.Stream;

public class ConnectorDetectionProperty {

	private ConnectorDetectionProperty() {}

	public static Set<IConnectorStateParser> getConnectorProperties() {

		return Stream
				.of(
						ConnectorSnmpProperty.getConnectorProperties(),
						ConnectorWbemProperty.getConnectorProperties(),
						ConnectorHttpProperty.getConnectorProperties(),
						ConnectorWmiProperty.getConnectorProperties(),
						ConnectorIpmiProperty.getConnectorProperties(),
						ConnectorOsProperty.getConnectorProperties(),
						ConnectorProcessProperty.getConnectorProperties(),
						ConnectorServiceProperty.getConnectorProperties(),
						ConnectorOSCommandProperty.getConnectorProperties(),
						ConnectorSshInteractiveProperty.getConnectorProperties())
				.flatMap(Set::stream)
				.collect(Collectors.toSet());
	}
}