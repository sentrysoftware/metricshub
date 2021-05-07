package com.sentrysoftware.matrix.connector.parser.state.compute.keeponlymatchinglines;

import com.sentrysoftware.matrix.connector.model.Connector;
import com.sentrysoftware.matrix.connector.parser.state.IConnectorStateParser;

public class ConnectorKeepOnlyMatchingLinesComputeParser implements IConnectorStateParser {

	@Override
	public boolean detect(String key, String value, Connector connector) {

		return ConnectorKeepOnlyMatchingLinesProperty
			.getConnectorProperties()
			.stream()
			.anyMatch(connectorKeepOnlyMatchingLinesProperty -> connectorKeepOnlyMatchingLinesProperty.detect(key, value, connector));
	}

	@Override
	public void parse(String key, String value, Connector connector) {

		ConnectorKeepOnlyMatchingLinesProperty
			.getConnectorProperties()
			.stream()
			.filter(connectorKeepOnlyMatchingLinesProperty -> connectorKeepOnlyMatchingLinesProperty.detect(key, value, connector))
			.forEach(connectorKeepOnlyMatchingLinesProperty -> connectorKeepOnlyMatchingLinesProperty.parse(key, value, connector));
	}
}
