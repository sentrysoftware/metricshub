package com.sentrysoftware.matrix.connector.parser.state.compute.translate;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertTrue;

class ConnectorTranslatePropertyTest {

	@Test
	void testGetConnectorStateProcessor() {

		assertTrue(ConnectorTranslateProperty.TYPE.getConnectorStateProcessor() instanceof TypeProcessor);
	}
}