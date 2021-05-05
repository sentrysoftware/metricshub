package com.sentrysoftware.matrix.connector.parser.state.compute.leftconcat;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertTrue;

class ConnectorLeftConcatPropertyTest {

	@Test
	void testGetConnectorStateProcessor() {

		assertTrue(ConnectorLeftConcatProperty.TYPE.getConnectorStateProcessor() instanceof TypeProcessor);
	}
}