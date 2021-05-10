package com.sentrysoftware.matrix.connector.parser.state.compute.divide;

import static org.junit.jupiter.api.Assertions.assertEquals;

import java.util.stream.Collectors;
import java.util.stream.Stream;

import org.junit.jupiter.api.Test;

class ConnectorDividePropertyTest {

	@Test
	void testGetConnectorProperties() {

		assertEquals(Stream.of(TypeProcessor.class, ColumnProcessor.class, DivideByProcessor.class).collect(Collectors.toSet()),
				ConnectorDivideProperty.getConnectorProperties().stream().map(obj -> obj.getClass()).collect(Collectors.toSet()));
	}
}