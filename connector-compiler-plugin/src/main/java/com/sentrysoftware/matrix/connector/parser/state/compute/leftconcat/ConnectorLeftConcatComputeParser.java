package com.sentrysoftware.matrix.connector.parser.state.compute.leftconcat;

import com.sentrysoftware.matrix.connector.model.Connector;
import com.sentrysoftware.matrix.connector.parser.state.IConnectorStateParser;

public class ConnectorLeftConcatComputeParser implements IConnectorStateParser {

	@Override
	public boolean detect(String key, String value, Connector connector) {

		return ConnectorLeftConcatProperty
			.getConnectorProperties()
			.stream()
			.anyMatch(connectorLeftConcatProperty -> connectorLeftConcatProperty.detect(key, value, connector));
	}

	@Override
	public void parse(String key, String value, Connector connector) {

		ConnectorLeftConcatProperty
			.getConnectorProperties()
			.stream()
			.filter(connectorLeftConcatProperty -> connectorLeftConcatProperty.detect(key, value, connector))
			.forEach(connectorLeftConcatProperty -> connectorLeftConcatProperty.parse(key, value, connector));
	}
}
