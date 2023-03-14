package com.sentrysoftware.matrix.converter.state.detection.ipmi;

import java.util.Set;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import com.sentrysoftware.matrix.converter.state.IConnectorStateConverter;
import com.sentrysoftware.matrix.converter.state.detection.common.ForceSerializationProcessor;
import com.sentrysoftware.matrix.converter.state.detection.common.TypeProcessor;

public class ConnectorIpmiProperty {
	private ConnectorIpmiProperty() {}

	public static Set<IConnectorStateConverter> getConnectorProperties() {

		return Stream
				.of(
						new TypeProcessor(IpmiProcessor.IPMI_TYPE_VALUE),
						new ForceSerializationProcessor(IpmiProcessor.IPMI_TYPE_VALUE))
				.collect(Collectors.toSet());
	}
}
