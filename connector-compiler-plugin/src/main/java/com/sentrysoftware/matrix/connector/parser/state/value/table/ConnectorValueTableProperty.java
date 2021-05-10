package com.sentrysoftware.matrix.connector.parser.state.value.table;

import java.util.Set;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import com.sentrysoftware.matrix.connector.parser.state.IConnectorStateParser;

public class ConnectorValueTableProperty {

	private ConnectorValueTableProperty() {}

	public static Set<IConnectorStateParser> getConnectorProperties() {

		return Stream
				.of(new CollectTypeProcessor(), new ValueTableProcessor(), new CollectParameterProcessor())
				.collect(Collectors.toSet());
	}
}