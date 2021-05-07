package com.sentrysoftware.matrix.connector.parser.state.detection.snmp;

import static org.junit.jupiter.api.Assertions.assertEquals;

import java.util.stream.Collectors;
import java.util.stream.Stream;

import org.junit.jupiter.api.Test;

class ConnectorSnmpPropertyTest {

	@Test
	void testGetConnectorProperties() {

		assertEquals(Stream.of(OidProcessor.class, ExpectedResultProcessor.class, ForceSerializationProcessor.class).collect(Collectors.toSet()),
				ConnectorSnmpProperty.getConnectorProperties().stream().map(obj -> obj.getClass()).collect(Collectors.toSet()));
	}
}