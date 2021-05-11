package com.sentrysoftware.matrix.connector.parser.state.compute.replace;

import com.sentrysoftware.matrix.connector.parser.state.IConnectorStateParser;

import java.util.Set;
import java.util.stream.Collectors;
import java.util.stream.Stream;

public class ConnectorReplaceProperty {

	private ConnectorReplaceProperty() {}

	public static Set<IConnectorStateParser> getConnectorProperties() {

		return Stream
				.of(new TypeProcessor(), new ColumnProcessor(), new ReplacePropertyProcessor(), new ReplaceByProcessor())
				.collect(Collectors.toSet());
	}
}
