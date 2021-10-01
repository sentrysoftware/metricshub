package com.sentrysoftware.matrix.connector.parser.state.compute.xml2csv;

import static org.junit.jupiter.api.Assertions.assertEquals;

import java.util.stream.Collectors;
import java.util.stream.Stream;

import org.junit.jupiter.api.Test;

import com.sentrysoftware.matrix.connector.parser.state.IConnectorStateParser;
import com.sentrysoftware.matrix.connector.parser.state.compute.common.TypeProcessor;

class ConnectorXml2CsvPropertyTest {

	@Test
	void testGetConnectorProperties() {
		assertEquals(
				Stream.of(TypeProcessor.class,RecordtTagProcessor.class, PropertiesProcessor.class).collect(Collectors.toSet()),
				ConnectorXml2CsvProperty.getConnectorProperties().stream()
				.map(IConnectorStateParser::getClass)
				.collect(Collectors.toSet()));
	}
}
