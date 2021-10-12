package com.sentrysoftware.matrix.connector.parser.state.detection.ipmi;

import static org.junit.jupiter.api.Assertions.assertEquals;

import java.util.stream.Collectors;
import java.util.stream.Stream;

import org.junit.jupiter.api.Test;

import com.sentrysoftware.matrix.connector.parser.state.IConnectorStateParser;
import com.sentrysoftware.matrix.connector.parser.state.detection.common.ForceSerializationProcessor;
import com.sentrysoftware.matrix.connector.parser.state.detection.common.TypeProcessor;

public class IpmiProcessorTest {

	@Test
	void testGetConnectorProperties() {

		assertEquals(
				Stream.of(TypeProcessor.class,
						ForceSerializationProcessor.class)
				.collect(Collectors.toSet()),
				ConnectorIpmiProperty
				.getConnectorProperties()
				.stream()
				.map(IConnectorStateParser::getClass)
				.collect(Collectors.toSet()));
	}
}
