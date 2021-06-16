package com.sentrysoftware.matrix.connector.parser.state.source.reference;

import java.util.Set;

import com.sentrysoftware.matrix.connector.parser.state.IConnectorStateParser;

public class ConnectorReferenceProperty {

	private ConnectorReferenceProperty() {
	}

	public static Set<IConnectorStateParser> getConnectorProperties() {

		return Set.of(new ReferenceProcessor());
	}
}