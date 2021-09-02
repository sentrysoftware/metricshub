package com.sentrysoftware.matrix.connector.parser.state.source.ipmi;

import java.util.Set;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import com.sentrysoftware.matrix.connector.model.monitor.job.source.type.ipmi.IPMI;
import com.sentrysoftware.matrix.connector.parser.state.IConnectorStateParser;
import com.sentrysoftware.matrix.connector.parser.state.source.common.ForceSerializationProcessor;

public class ConnectorIpmiProperty {

	private ConnectorIpmiProperty() {
	}

	public static Set<IConnectorStateParser> getConnectorProperties() {

		return Stream
				.of(
						new IpmiTypeProcessor(IPMI.class, IpmiTypeProcessor.IPMI_TYPE_VALUE),
						new ForceSerializationProcessor(IPMI.class, IpmiTypeProcessor.IPMI_TYPE_VALUE))
				.collect(Collectors.toSet());
	}
}
