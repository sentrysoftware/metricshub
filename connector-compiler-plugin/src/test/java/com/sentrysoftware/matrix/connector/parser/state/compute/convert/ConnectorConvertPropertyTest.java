package com.sentrysoftware.matrix.connector.parser.state.compute.convert;

import static org.junit.jupiter.api.Assertions.assertEquals;

import java.util.stream.Collectors;
import java.util.stream.Stream;

import org.junit.jupiter.api.Test;

import com.sentrysoftware.matrix.connector.parser.state.IConnectorStateParser;
import com.sentrysoftware.matrix.connector.parser.state.compute.common.ColumnProcessor;
import com.sentrysoftware.matrix.connector.parser.state.compute.common.TypeProcessor;

class ConnectorConvertPropertyTest {

	@Test
	void testGetConnectorProperties() {
		assertEquals(Stream.of(TypeProcessor.class, ColumnProcessor.class, ConversionTypeProcessor.class).collect(Collectors.toSet()),
				ConnectorConvertProperty.getConnectorProperties().stream().map(IConnectorStateParser::getClass).collect(Collectors.toSet()));
	}
}
