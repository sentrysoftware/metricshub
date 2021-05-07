package com.sentrysoftware.matrix.connector.parser.state.compute.duplicatecolumn;

import com.sentrysoftware.matrix.connector.model.Connector;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

class ConnectorDuplicateColumnComputeParserTest {

	private Connector connector;

	private static final String DUPLICATE_COLUMN_COLLECT_TYPE_KEY = "enclosure.collect.source(1).compute(1).type";
	private static final String DUPLICATE_COLUMN_TYPE_VALUE = "DuplicateColumn";

	@BeforeEach
	void setUp() {

		connector = new Connector();
	}

	@Test
	void testParse() {

		ConnectorDuplicateColumnComputeParser connectorDuplicateColumnComputeParser = new ConnectorDuplicateColumnComputeParser();
		connectorDuplicateColumnComputeParser.parse(DUPLICATE_COLUMN_COLLECT_TYPE_KEY, DUPLICATE_COLUMN_TYPE_VALUE, connector);

		assertNotNull(connector.getHardwareMonitors());
		assertTrue(connector.getHardwareMonitors().isEmpty());
	}
}