package com.sentrysoftware.matrix.converter.state.detection.service;

import java.util.Set;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import com.sentrysoftware.matrix.converter.state.IConnectorStateConverter;
import com.sentrysoftware.matrix.converter.state.detection.common.ForceSerializationProcessor;
import com.sentrysoftware.matrix.converter.state.detection.common.TypeProcessor;

import lombok.AccessLevel;
import lombok.NoArgsConstructor;

@NoArgsConstructor(access = AccessLevel.PRIVATE)
public class ConnectorServiceProperty {

	private static final String SERVICE_HDF_TYPE_VALUE = "Service";
	private static final String SERVICE_YAML_TYPE_VALUE = "service";

	public static Set<IConnectorStateConverter> getConnectorProperties() {

		return Stream.of(
			new TypeProcessor(SERVICE_HDF_TYPE_VALUE, SERVICE_YAML_TYPE_VALUE),
			new ForceSerializationProcessor(),
			new ServiceNameProcessor()
		)
		.collect(Collectors.toSet());
	}
}