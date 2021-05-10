package com.sentrysoftware.matrix.connector.parser.state.instance;

import java.util.Set;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import com.sentrysoftware.matrix.connector.parser.state.IConnectorStateParser;

public class ConnectorInstanceProperty {

	private ConnectorInstanceProperty() {}

	public static Set<IConnectorStateParser> getConnectorProperties() {

		return Stream
				.of(new InstanceTableProcessor(), new InstanceProcessor())
				.collect(Collectors.toSet());
	}
}
