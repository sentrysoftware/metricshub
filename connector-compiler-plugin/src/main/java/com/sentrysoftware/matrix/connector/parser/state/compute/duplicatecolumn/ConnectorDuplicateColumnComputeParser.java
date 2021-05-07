package com.sentrysoftware.matrix.connector.parser.state.compute.duplicatecolumn;

import com.sentrysoftware.matrix.connector.model.Connector;
import com.sentrysoftware.matrix.connector.parser.state.IConnectorStateParser;

public class ConnectorDuplicateColumnComputeParser implements IConnectorStateParser {

	@Override
	public boolean detect(String key, String value, Connector connector) {

		return ConnectorDuplicateColumnProperty
			.getConnectorProperties()
			.stream()
			.anyMatch(connectorDuplicateColumnProperty -> connectorDuplicateColumnProperty.detect(key, value, connector));
	}

	@Override
	public void parse(String key, String value, Connector connector) {

		ConnectorDuplicateColumnProperty
			.getConnectorProperties()
			.stream()
			.filter(connectorDuplicateColumnProperty -> connectorDuplicateColumnProperty.detect(key, value, connector))
			.forEach(connectorDuplicateColumnProperty -> connectorDuplicateColumnProperty.parse(key, value, connector));
	}
}
