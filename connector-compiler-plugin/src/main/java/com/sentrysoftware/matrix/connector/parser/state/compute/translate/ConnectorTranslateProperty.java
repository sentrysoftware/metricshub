package com.sentrysoftware.matrix.connector.parser.state.compute.translate;

import com.sentrysoftware.matrix.connector.model.Connector;
import com.sentrysoftware.matrix.connector.parser.state.IConnectorStateParser;
import lombok.AllArgsConstructor;
import lombok.Getter;

import java.util.Arrays;
import java.util.Set;
import java.util.stream.Collectors;

@Getter
@AllArgsConstructor
public enum ConnectorTranslateProperty implements IConnectorStateParser {

	TYPE(new TypeProcessor()),
	COLUMN(new ColumnProcessor()),
	TRANSLATION_TABLE(new TranslationTableProcessor());

	private final IConnectorStateParser connectorStateProcessor;

	public boolean detect(final String key, final String value, final Connector connector) {

		return connectorStateProcessor.detect(key, value, connector);
	}

	public void parse(final String key, final String value, final Connector connector) {

		connectorStateProcessor.parse(key, value, connector);
	}

	public static Set<ConnectorTranslateProperty> getConnectorProperties() {

		return Arrays.stream(ConnectorTranslateProperty.values()).collect(Collectors.toSet());
	}
}
