package com.sentrysoftware.matrix.converter.state.detection.ipmi;

import java.util.Set;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import com.sentrysoftware.matrix.converter.state.IConnectorStateConverter;
import com.sentrysoftware.matrix.converter.state.detection.common.ForceSerializationProcessor;
import com.sentrysoftware.matrix.converter.state.detection.common.TypeProcessor;

import lombok.AccessLevel;
import lombok.AllArgsConstructor;

@AllArgsConstructor(access = AccessLevel.PRIVATE)
public class ConnectorIpmiProperty {

	public static final String IPMI_HDF_TYPE_VALUE = "IPMI";
	public static final String IPMI_YAML_TYPE_VALUE = "ipmi";

	public static Set<IConnectorStateConverter> getConnectorProperties() {

		return Stream.of(
			new TypeProcessor(IPMI_HDF_TYPE_VALUE, IPMI_YAML_TYPE_VALUE),
			new ForceSerializationProcessor()
		)
		.collect(Collectors.toSet());
	}
}
