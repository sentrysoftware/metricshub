package com.sentrysoftware.matrix.connector.parser.state.compute.translate;

import com.sentrysoftware.matrix.connector.model.Connector;
import com.sentrysoftware.matrix.connector.parser.state.IConnectorStateParser;

public class ConnectorTranslateComputeParser implements IConnectorStateParser {

	@Override
	public boolean detect(String key, String value, Connector connector) {

		return ConnectorTranslateProperty
				.getConnectorProperties()
				.stream()
				.anyMatch(connectorTranslateProperty -> connectorTranslateProperty.detect(key, value, connector));
	}

	@Override
	public void parse(String key, String value, Connector connector) {

		ConnectorTranslateProperty
				.getConnectorProperties()
				.stream()
				.filter(connectorTranslateProperty -> connectorTranslateProperty.detect(key, value, connector))
				.forEach(connectorTranslateProperty -> connectorTranslateProperty.parse(key, value, connector));
	}
}
