package com.sentrysoftware.matrix.connector.parser.state.compute.excludematchinglines;

import static org.junit.jupiter.api.Assertions.assertEquals;

import java.util.stream.Collectors;
import java.util.stream.Stream;

import org.junit.jupiter.api.Test;

import com.sentrysoftware.matrix.connector.parser.state.IConnectorStateParser;

class ConnectorExcludeMatchingLinesPropertyTest {

	@Test
	void testGetConnectorProperties() {

		assertEquals(
				Stream.of(TypeProcessor.class, ColumnProcessor.class, ValueListProcessor.class, RegexpProcessor.class).collect(Collectors.toSet()),
				ConnectorExcludeMatchingLinesProperty.getConnectorProperties().stream().map(IConnectorStateParser::getClass).collect(Collectors.toSet()));
	}
}