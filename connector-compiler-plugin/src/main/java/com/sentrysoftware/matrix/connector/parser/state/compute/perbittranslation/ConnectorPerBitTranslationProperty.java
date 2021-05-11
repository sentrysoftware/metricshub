package com.sentrysoftware.matrix.connector.parser.state.compute.perbittranslation;

import com.sentrysoftware.matrix.connector.parser.state.IConnectorStateParser;

import java.util.Set;
import java.util.stream.Collectors;
import java.util.stream.Stream;

public class ConnectorPerBitTranslationProperty {

	private ConnectorPerBitTranslationProperty() {}

	public static Set<IConnectorStateParser> getConnectorProperties() {

		return Stream
				.of(new TypeProcessor(), new ColumnProcessor(), new BitListProcessor(), new BitTranslationTableProcessor())
				.collect(Collectors.toSet());
	}
}
