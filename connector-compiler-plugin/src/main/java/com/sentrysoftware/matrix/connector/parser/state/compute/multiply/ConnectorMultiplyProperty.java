package com.sentrysoftware.matrix.connector.parser.state.compute.multiply;

import com.sentrysoftware.matrix.connector.parser.state.IConnectorStateParser;

import java.util.Set;
import java.util.stream.Collectors;
import java.util.stream.Stream;

public class ConnectorMultiplyProperty {

	private ConnectorMultiplyProperty() {}

	public static Set<IConnectorStateParser> getConnectorProperties() {

		return Stream
				.of(new TypeProcessor(), new ColumnProcessor(), new MultiplyByProcessor())
				.collect(Collectors.toSet());
	}
}
