package com.sentrysoftware.matrix.connector.parser.state.value.table;

import com.sentrysoftware.matrix.connector.model.Connector;
import com.sentrysoftware.matrix.connector.parser.state.IConnectorStateParser;

public class ConnectorValueTableParser implements IConnectorStateParser {
	ValueTableProcessor valueTableProcessor = new ValueTableProcessor();

	@Override
	public boolean detect(String key, String value, Connector connector) {
		return valueTableProcessor.detect(key, value, connector);
	}

	@Override
	public void parse(String key, String value, Connector connector) {
		if (valueTableProcessor.detect(key, value, connector)) {
			valueTableProcessor.parse(key, value, connector);
		}
	}
}
