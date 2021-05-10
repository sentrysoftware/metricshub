package com.sentrysoftware.matrix.connector.parser.state.compute.keeponlymatchinglines;

import java.util.Set;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import com.sentrysoftware.matrix.connector.parser.state.IConnectorStateParser;

public class ConnectorKeepOnlyMatchingLinesProperty {

	private ConnectorKeepOnlyMatchingLinesProperty() {}

	public static Set<IConnectorStateParser> getConnectorProperties() {

		return Stream
				.of(new TypeProcessor(), new ColumnProcessor(), new ValueListProcessor(), new RegexpProcessor())
				.collect(Collectors.toSet());
	}
}