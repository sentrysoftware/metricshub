package com.sentrysoftware.matrix.connector.parser.state.detection.service;

import static org.junit.jupiter.api.Assertions.assertEquals;

import java.util.stream.Collectors;
import java.util.stream.Stream;

import org.junit.jupiter.api.Test;

import com.sentrysoftware.matrix.connector.parser.state.IConnectorStateParser;
import com.sentrysoftware.matrix.connector.parser.state.detection.common.ForceSerializationProcessor;
import com.sentrysoftware.matrix.connector.parser.state.detection.common.TypeProcessor;

class ConnectorServicePropertyTest {

	@Test
	void testGetConnectorProperties() {
		assertEquals(
				Stream.of(TypeProcessor.class,ForceSerializationProcessor.class).collect(Collectors.toSet()),
				ConnectorServiceProperty.getConnectorProperties().stream()
				.map(IConnectorStateParser::getClass)
				.collect(Collectors.toSet()));
	}
}
