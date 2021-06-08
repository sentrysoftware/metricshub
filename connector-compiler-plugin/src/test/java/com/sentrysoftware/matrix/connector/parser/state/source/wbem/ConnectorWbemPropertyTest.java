package com.sentrysoftware.matrix.connector.parser.state.source.wbem;

import static org.junit.jupiter.api.Assertions.assertEquals;

import java.util.stream.Collectors;
import java.util.stream.Stream;

import org.junit.jupiter.api.Test;

import com.sentrysoftware.matrix.connector.parser.state.IConnectorStateParser;
import com.sentrysoftware.matrix.connector.parser.state.source.common.ForceSerializationProcessor;
import com.sentrysoftware.matrix.connector.parser.state.source.common.TypeProcessor;

class ConnectorWbemPropertyTest {

	@Test
	void testGetConnectorProperties() {
		assertEquals(Stream.of(TypeProcessor.class,
				ForceSerializationProcessor.class,
				WbemQueryProcessor.class,
				WbemNameSpaceProcessor.class).collect(Collectors.toSet()),
				ConnectorWbemProperty.getConnectorProperties().stream().map(IConnectorStateParser::getClass).collect(Collectors.toSet()));
	}
}
