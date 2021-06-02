package com.sentrysoftware.matrix.connector.parser.state.compute.extract;

import com.sentrysoftware.matrix.connector.parser.state.IConnectorStateParser;
import com.sentrysoftware.matrix.connector.parser.state.compute.common.ColumnProcessor;
import com.sentrysoftware.matrix.connector.parser.state.compute.common.TypeProcessor;
import org.junit.jupiter.api.Test;

import java.util.stream.Collectors;
import java.util.stream.Stream;

import static org.junit.jupiter.api.Assertions.assertEquals;

class ConnectorExtractPropertyTest {

	@Test
	void testGetConnectorProperties() {

		assertEquals(
			Stream
				.of(TypeProcessor.class, ColumnProcessor.class, SubColumnProcessor.class, SubSeparatorsProcessor.class)
				.collect(Collectors.toSet()),
			ConnectorExtractProperty
				.getConnectorProperties()
				.stream()
				.map(IConnectorStateParser::getClass)
				.collect(Collectors.toSet()));
	}
}