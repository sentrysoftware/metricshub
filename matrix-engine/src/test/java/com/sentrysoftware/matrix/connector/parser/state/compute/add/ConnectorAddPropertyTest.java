package com.sentrysoftware.matrix.connector.parser.state.compute.add;

import com.sentrysoftware.matrix.connector.parser.state.IConnectorStateParser;
import com.sentrysoftware.matrix.connector.parser.state.compute.common.ColumnProcessor;
import com.sentrysoftware.matrix.connector.parser.state.compute.common.TypeProcessor;
import org.junit.jupiter.api.Test;

import java.util.stream.Collectors;
import java.util.stream.Stream;

import static org.junit.jupiter.api.Assertions.assertEquals;

class ConnectorAddPropertyTest {

	@Test
	void testGetConnectorProperties() {

		assertEquals(Stream.of(TypeProcessor.class, ColumnProcessor.class, AddPropertyProcessor.class).collect(Collectors.toSet()),
				ConnectorAddProperty.getConnectorProperties().stream().map(IConnectorStateParser::getClass).collect(Collectors.toSet()));
	}
}