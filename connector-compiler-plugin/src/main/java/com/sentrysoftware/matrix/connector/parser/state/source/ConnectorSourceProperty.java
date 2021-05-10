package com.sentrysoftware.matrix.connector.parser.state.source;

import java.util.Set;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import com.sentrysoftware.matrix.connector.parser.state.IConnectorStateParser;

public class ConnectorSourceProperty {

	private ConnectorSourceProperty() {}

	public static Set<IConnectorStateParser> getConnectorProperties() {

		return Stream
				.of(new SnmpTableProcessor(), new TableJoinProcessor())
				.collect(Collectors.toSet());
	}
}