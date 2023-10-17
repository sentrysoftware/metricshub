package com.sentrysoftware.metricshub.converter.state.detection.service;

import com.sentrysoftware.metricshub.converter.state.IConnectorStateConverter;
import com.sentrysoftware.metricshub.converter.state.detection.common.ForceSerializationProcessor;
import com.sentrysoftware.metricshub.converter.state.detection.common.CriterionTypeProcessor;
import java.util.Set;
import java.util.stream.Collectors;
import java.util.stream.Stream;
import lombok.AccessLevel;
import lombok.NoArgsConstructor;

@NoArgsConstructor(access = AccessLevel.PRIVATE)
public class ConnectorServiceProperty {

	private static final String SERVICE_HDF_TYPE_VALUE = "Service";
	private static final String SERVICE_YAML_TYPE_VALUE = "service";

	public static Set<IConnectorStateConverter> getConnectorProperties() {
		return Stream
			.of(
				new CriterionTypeProcessor(SERVICE_HDF_TYPE_VALUE, SERVICE_YAML_TYPE_VALUE),
				new ForceSerializationProcessor(),
				new ServiceNameProcessor()
			)
			.collect(Collectors.toSet());
	}
}
