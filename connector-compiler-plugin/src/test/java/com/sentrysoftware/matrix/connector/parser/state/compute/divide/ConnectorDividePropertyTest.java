package com.sentrysoftware.matrix.connector.parser.state.compute.divide;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class ConnectorDividePropertyTest {

	@Test
	void testGetConnectorStateProcessor() {

		assertTrue(ConnectorDivideProperty.TYPE.getConnectorStateProcessor() instanceof TypeProcessor);
	}
}