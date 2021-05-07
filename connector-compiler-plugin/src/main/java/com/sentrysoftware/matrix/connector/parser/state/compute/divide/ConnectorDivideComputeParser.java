package com.sentrysoftware.matrix.connector.parser.state.compute.divide;

import com.sentrysoftware.matrix.connector.model.Connector;
import com.sentrysoftware.matrix.connector.parser.state.IConnectorStateParser;

public class ConnectorDivideComputeParser implements IConnectorStateParser {

	@Override
	public boolean detect(String key, String value, Connector connector) {

		return ConnectorDivideProperty
			.getConnectorProperties()
			.stream()
			.anyMatch(connectorDivideProperty -> connectorDivideProperty.detect(key, value, connector));
	}

	@Override
	public void parse(String key, String value, Connector connector) {

		ConnectorDivideProperty
			.getConnectorProperties()
			.stream()
			.filter(connectorDivideProperty -> connectorDivideProperty.detect(key, value, connector))
			.forEach(connectorDivideProperty -> connectorDivideProperty.parse(key, value, connector));
	}
}
