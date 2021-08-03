package com.sentrysoftware.matrix.connector.parser.state.detection.os;

import static org.junit.jupiter.api.Assertions.assertEquals;

import java.util.stream.Collectors;
import java.util.stream.Stream;

import org.junit.jupiter.api.Test;

import com.sentrysoftware.matrix.connector.parser.state.IConnectorStateParser;
import com.sentrysoftware.matrix.connector.parser.state.detection.common.ForceSerializationProcessor;
import com.sentrysoftware.matrix.connector.parser.state.detection.common.TypeProcessor;

public class ConnectorOsPropertyTest {

	@Test
	void testGetConnectorProperties() {

		assertEquals(
				Stream.of(
						TypeProcessor.class,
						ForceSerializationProcessor.class,
						KeepOnlyProcessor.class,
						ExcludeProcessor.class)
				.collect(Collectors.toSet()),
				ConnectorOsProperty
				.getConnectorProperties()
				.stream()
				.map(IConnectorStateParser::getClass)
				.collect(Collectors.toSet()));
	}
}
