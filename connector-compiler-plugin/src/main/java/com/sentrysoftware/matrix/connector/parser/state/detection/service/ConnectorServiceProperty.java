package com.sentrysoftware.matrix.connector.parser.state.detection.service;

import java.util.Set;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import com.sentrysoftware.matrix.connector.model.detection.criteria.service.Service;
import com.sentrysoftware.matrix.connector.parser.state.IConnectorStateParser;
import com.sentrysoftware.matrix.connector.parser.state.detection.common.ForceSerializationProcessor;
import com.sentrysoftware.matrix.connector.parser.state.detection.common.TypeProcessor;

public class ConnectorServiceProperty {

	private ConnectorServiceProperty() { }

	public static Set<IConnectorStateParser> getConnectorProperties() {
		return Stream.of(
				new TypeProcessor(Service.class, ServiceNameProcessor.SERVICE_TYPE_VALUE),
				new ForceSerializationProcessor(Service.class, ServiceNameProcessor.SERVICE_TYPE_VALUE),
				new ServiceNameProcessor())
				.collect(Collectors.toSet());
	}

}
