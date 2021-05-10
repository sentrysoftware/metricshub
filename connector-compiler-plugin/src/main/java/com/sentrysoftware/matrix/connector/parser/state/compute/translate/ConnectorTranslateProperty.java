package com.sentrysoftware.matrix.connector.parser.state.compute.translate;

import java.util.Set;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import com.sentrysoftware.matrix.connector.parser.state.IConnectorStateParser;

public class ConnectorTranslateProperty {

	private ConnectorTranslateProperty() {}

	public static Set<IConnectorStateParser> getConnectorProperties() {

		return Stream
				.of(new TypeProcessor(), new ColumnProcessor(), new TranslationTableProcessor())
				.collect(Collectors.toSet());
	}
}
