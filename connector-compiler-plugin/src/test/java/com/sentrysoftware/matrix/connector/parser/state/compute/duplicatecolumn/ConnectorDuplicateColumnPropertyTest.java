package com.sentrysoftware.matrix.connector.parser.state.compute.duplicatecolumn;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertTrue;

class ConnectorDuplicateColumnPropertyTest {

	@Test
	void testGetConnectorStateProcessor() {

		assertTrue(ConnectorDuplicateColumnProperty.TYPE.getConnectorStateProcessor() instanceof TypeProcessor);
	}
}