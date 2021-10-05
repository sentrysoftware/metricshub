package com.sentrysoftware.matrix.connector.parser.state.compute.extractpropertyfromwbempath;

import static org.junit.jupiter.api.Assertions.assertEquals;

import java.util.stream.Collectors;
import java.util.stream.Stream;

import org.junit.jupiter.api.Test;

import com.sentrysoftware.matrix.connector.parser.state.IConnectorStateParser;
import com.sentrysoftware.matrix.connector.parser.state.compute.common.ColumnProcessor;
import com.sentrysoftware.matrix.connector.parser.state.compute.common.TypeProcessor;

public class ConnectorExtractPropertyFromWbemPathPropertyTest {
	
	@Test
	void testGetConnectorProperties() {

		assertEquals(Stream.of(TypeProcessor.class, ColumnProcessor.class, PropertyNameProcessor.class).collect(Collectors.toSet()),
				ConnectorExtractPropertyFromWbemPathProperty.getConnectorProperties().stream().map(IConnectorStateParser::getClass).collect(Collectors.toSet()));
	}
}
