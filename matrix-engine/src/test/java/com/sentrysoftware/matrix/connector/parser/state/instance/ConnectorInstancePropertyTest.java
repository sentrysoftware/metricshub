package com.sentrysoftware.matrix.connector.parser.state.instance;

import static org.junit.jupiter.api.Assertions.assertEquals;

import java.util.stream.Collectors;
import java.util.stream.Stream;

import org.junit.jupiter.api.Test;

class ConnectorInstancePropertyTest {

	@Test
	void testGetConnectorProperties() {

		assertEquals(Stream.of(InstanceTableProcessor.class, InstanceProcessor.class).collect(Collectors.toSet()),
				ConnectorInstanceProperty.getConnectorProperties().stream().map(obj -> obj.getClass()).collect(Collectors.toSet()));
	}
}