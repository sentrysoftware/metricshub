package com.sentrysoftware.matrix.connector.parser.state.compute.arraytranslate;

import com.sentrysoftware.matrix.connector.parser.state.IConnectorStateParser;
import com.sentrysoftware.matrix.connector.parser.state.compute.common.ColumnProcessor;
import com.sentrysoftware.matrix.connector.parser.state.compute.common.TypeProcessor;
import org.junit.jupiter.api.Test;

import java.util.stream.Collectors;
import java.util.stream.Stream;

import static org.junit.jupiter.api.Assertions.*;

class ConnectorArrayTranslatePropertyTest {

	@Test
	void testGetConnectorProperties() {

		assertEquals(
			Stream.of(
				TypeProcessor.class,
				ColumnProcessor.class,
				TranslationTableProcessor.class,
				ArraySeparatorProcessor.class,
				ResultSeparatorProcessor.class)
				.collect(Collectors.toSet()),
			ConnectorArrayTranslateProperty
				.getConnectorProperties()
				.stream()
				.map(IConnectorStateParser::getClass)
				.collect(Collectors.toSet()));
	}
}