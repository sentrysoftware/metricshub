package com.sentrysoftware.matrix.connector.parser.state.compute.divide;

import com.sentrysoftware.matrix.connector.model.Connector;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

class ConnectorDivideComputeParserTest {

	private Connector connector;

	private static final String DIVIDE_COLLECT_TYPE_KEY = "enclosure.collect.source(1).compute(1).type";
	private static final String DIVIDE_TYPE_VALUE = "Divide";

	@BeforeEach
	void setUp() {

		connector = new Connector();
	}

	@Test
	void testParse() {

		ConnectorDivideComputeParser connectorDivideComputeParser = new ConnectorDivideComputeParser();
		connectorDivideComputeParser.parse(DIVIDE_COLLECT_TYPE_KEY, DIVIDE_TYPE_VALUE, connector);

		assertNotNull(connector.getHardwareMonitors());
		assertTrue(connector.getHardwareMonitors().isEmpty());
	}
}