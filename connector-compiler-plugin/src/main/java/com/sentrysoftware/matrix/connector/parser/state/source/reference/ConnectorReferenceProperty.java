package com.sentrysoftware.matrix.connector.parser.state.source.reference;

import java.util.Set;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import com.sentrysoftware.matrix.connector.parser.state.IConnectorStateParser;

public class ConnectorReferenceProperty {

	private ConnectorReferenceProperty() {
	}

	public static Set<IConnectorStateParser> getConnectorProperties() {

		return Stream
				.of(
						new ReferenceProcessor())
				.collect(Collectors.toSet());
	}
}
