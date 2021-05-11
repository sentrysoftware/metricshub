package com.sentrysoftware.matrix.connector.parser.state.compute.add;

import com.sentrysoftware.matrix.connector.parser.state.IConnectorStateParser;

import java.util.Set;
import java.util.stream.Collectors;
import java.util.stream.Stream;

public class ConnectorAddProperty {

	private ConnectorAddProperty() {}

	public static Set<IConnectorStateParser> getConnectorProperties() {

		return Stream
				.of(new TypeProcessor(), new ColumnProcessor(), new AddPropertyProcessor())
				.collect(Collectors.toSet());
	}
}
