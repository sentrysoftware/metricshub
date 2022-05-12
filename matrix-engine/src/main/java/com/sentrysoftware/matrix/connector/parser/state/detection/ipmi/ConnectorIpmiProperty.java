package com.sentrysoftware.matrix.connector.parser.state.detection.ipmi;

import java.util.Set;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import com.sentrysoftware.matrix.connector.model.detection.criteria.ipmi.Ipmi;
import com.sentrysoftware.matrix.connector.parser.state.IConnectorStateParser;
import com.sentrysoftware.matrix.connector.parser.state.detection.common.ForceSerializationProcessor;
import com.sentrysoftware.matrix.connector.parser.state.detection.common.TypeProcessor;

public class ConnectorIpmiProperty {
	private ConnectorIpmiProperty() {}

	public static Set<IConnectorStateParser> getConnectorProperties() {

		return Stream
				.of(
						new TypeProcessor(Ipmi.class, IpmiProcessor.IPMI_TYPE_VALUE),
						new ForceSerializationProcessor(Ipmi.class, IpmiProcessor.IPMI_TYPE_VALUE))
				.collect(Collectors.toSet());
	}
}
