package com.sentrysoftware.matrix.connector.parser.state.source.wmi;

import static org.junit.jupiter.api.Assertions.assertEquals;

import java.util.stream.Collectors;
import java.util.stream.Stream;

import org.junit.jupiter.api.Test;

import com.sentrysoftware.matrix.connector.parser.state.IConnectorStateParser;
import com.sentrysoftware.matrix.connector.parser.state.source.common.ForceSerializationProcessor;
import com.sentrysoftware.matrix.connector.parser.state.source.common.TypeProcessor;
import com.sentrysoftware.matrix.connector.parser.state.source.common.WbemNamespaceProcessor;
import com.sentrysoftware.matrix.connector.parser.state.source.common.WbemQueryProcessor;

class ConnectorWmiPropertyTest {

	@Test
	void testGetConnectorProperties() {
		assertEquals(Stream.of(TypeProcessor.class,
				ForceSerializationProcessor.class,
				WbemQueryProcessor.class,
				WbemNamespaceProcessor.class).collect(Collectors.toSet()),
				ConnectorWmiProperty.getConnectorProperties().stream().map(IConnectorStateParser::getClass).collect(Collectors.toSet()));
	}

}
