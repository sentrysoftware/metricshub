package com.sentrysoftware.matrix.connector.parser.state.compute.duplicatecolumn;

import java.util.Set;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import com.sentrysoftware.matrix.connector.parser.state.IConnectorStateParser;

public class ConnectorDuplicateColumnProperty {

	private ConnectorDuplicateColumnProperty() {}

	public static Set<IConnectorStateParser> getConnectorProperties() {

		return Stream
				.of(new TypeProcessor(), new ColumnProcessor())
				.collect(Collectors.toSet());
	}
}
