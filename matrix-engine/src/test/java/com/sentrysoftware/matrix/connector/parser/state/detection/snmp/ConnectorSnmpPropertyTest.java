package com.sentrysoftware.matrix.connector.parser.state.detection.snmp;

import com.sentrysoftware.matrix.connector.parser.state.IConnectorStateParser;
import com.sentrysoftware.matrix.connector.parser.state.detection.common.ExpectedResultProcessor;
import com.sentrysoftware.matrix.connector.parser.state.detection.common.ForceSerializationProcessor;
import org.junit.jupiter.api.Test;

import java.util.stream.Collectors;
import java.util.stream.Stream;

import static org.junit.jupiter.api.Assertions.assertEquals;

class ConnectorSnmpPropertyTest {

	@Test
	void testGetConnectorProperties() {

		assertEquals(
			Stream.of(
				OidProcessor.class,
				ExpectedResultProcessor.class,
				ForceSerializationProcessor.class)
				.collect(Collectors.toSet()),
			ConnectorSnmpProperty
				.getConnectorProperties()
				.stream()
				.map(IConnectorStateParser::getClass)
				.collect(Collectors.toSet()));
	}
}