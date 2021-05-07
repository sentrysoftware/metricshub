package com.sentrysoftware.matrix.connector.parser.state.compute.divide;

import java.util.Set;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import com.sentrysoftware.matrix.connector.parser.state.IConnectorStateParser;

public class ConnectorDivideProperty {

	private ConnectorDivideProperty() {}

	public static Set<IConnectorStateParser> getConnectorProperties() {

		return Stream
				.of(new TypeProcessor(), new ColumnProcessor(), new DivideByProcessor())
				.collect(Collectors.toSet());
	}
}
