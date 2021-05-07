package com.sentrysoftware.matrix.connector.parser.state.compute.leftconcat;

import com.sentrysoftware.matrix.connector.model.Connector;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class ConnectorLeftConcatComputeParserTest {

	private Connector connector;

	private static final String LEFT_CONCAT_DISCOVERY_TYPE_KEY = "enclosure.discovery.source(1).compute(1).type";
	private static final String LEFT_CONCAT_TYPE_VALUE = "LeftConcat";

	@BeforeEach
	void setUp() {

		connector = new Connector();
	}

	@Test
	void testParse() {

		ConnectorLeftConcatComputeParser connectorLeftConcatComputeParser = new ConnectorLeftConcatComputeParser();
		connectorLeftConcatComputeParser.parse(LEFT_CONCAT_DISCOVERY_TYPE_KEY, LEFT_CONCAT_TYPE_VALUE, connector);

		assertNotNull(connector.getHardwareMonitors());
		assertTrue(connector.getHardwareMonitors().isEmpty());
	}
}