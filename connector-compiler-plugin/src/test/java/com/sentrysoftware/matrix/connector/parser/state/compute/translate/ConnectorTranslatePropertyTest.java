package com.sentrysoftware.matrix.connector.parser.state.compute.translate;

import static org.junit.jupiter.api.Assertions.assertEquals;

import java.util.stream.Collectors;
import java.util.stream.Stream;

import com.sentrysoftware.matrix.connector.parser.state.IConnectorStateParser;
import org.junit.jupiter.api.Test;

class ConnectorTranslatePropertyTest {

	@Test
	void testGetConnectorProperties() {

		assertEquals(Stream.of(TypeProcessor.class, ColumnProcessor.class, TranslationTableProcessor.class).collect(Collectors.toSet()),
				ConnectorTranslateProperty.getConnectorProperties().stream().map(IConnectorStateParser::getClass).collect(Collectors.toSet()));

	}
}