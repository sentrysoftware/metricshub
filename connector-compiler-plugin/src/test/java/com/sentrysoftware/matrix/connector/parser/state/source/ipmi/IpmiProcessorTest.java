package com.sentrysoftware.matrix.connector.parser.state.source.ipmi;

import static org.junit.jupiter.api.Assertions.assertEquals;

import java.util.stream.Collectors;
import java.util.stream.Stream;

import org.junit.jupiter.api.Test;

import com.sentrysoftware.matrix.connector.parser.state.IConnectorStateParser;
import com.sentrysoftware.matrix.connector.parser.state.source.common.ForceSerializationProcessor;

public class IpmiProcessorTest {

	@Test
	void testGetConnectorProperties() {
		assertEquals(Stream.of(IpmiTypeProcessor.class,
				ForceSerializationProcessor.class).collect(Collectors.toSet()),
				ConnectorIpmiProperty.getConnectorProperties().stream().map(IConnectorStateParser::getClass).collect(Collectors.toSet()));
	}
}
