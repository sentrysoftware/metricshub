package com.sentrysoftware.matrix.connector.parser.state.compute.leftconcat;

import java.util.Set;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import com.sentrysoftware.matrix.connector.parser.state.IConnectorStateParser;

public class ConnectorLeftConcatProperty {

	private ConnectorLeftConcatProperty() {}

	public static Set<IConnectorStateParser> getConnectorProperties() {

		return Stream
				.of(new TypeProcessor(), new ColumnProcessor(), new StringProcessor())
				.collect(Collectors.toSet());
	}
}
