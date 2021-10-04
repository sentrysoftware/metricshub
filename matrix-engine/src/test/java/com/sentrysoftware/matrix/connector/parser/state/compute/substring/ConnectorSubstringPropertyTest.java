package com.sentrysoftware.matrix.connector.parser.state.compute.substring;

import static org.junit.jupiter.api.Assertions.assertEquals;

import java.util.stream.Collectors;
import java.util.stream.Stream;

import org.junit.jupiter.api.Test;

import com.sentrysoftware.matrix.connector.parser.state.IConnectorStateParser;
import com.sentrysoftware.matrix.connector.parser.state.compute.common.ColumnProcessor;
import com.sentrysoftware.matrix.connector.parser.state.compute.common.TypeProcessor;

class ConnectorSubstringPropertyTest {

	@Test
	void testGetConnectorProperties() {
		assertEquals(Stream.of(TypeProcessor.class, ColumnProcessor.class, StartProcessor.class, LengthProcessor.class).collect(Collectors.toSet()),
				ConnectorSubstringProperty.getConnectorProperties().stream().map(IConnectorStateParser::getClass).collect(Collectors.toSet()));
	}

}
