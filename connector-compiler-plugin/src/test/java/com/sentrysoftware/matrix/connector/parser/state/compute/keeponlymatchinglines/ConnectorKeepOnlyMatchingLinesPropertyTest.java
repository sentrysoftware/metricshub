package com.sentrysoftware.matrix.connector.parser.state.compute.keeponlymatchinglines;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertTrue;

class ConnectorKeepOnlyMatchingLinesPropertyTest {

	@Test
	void testGetConnectorStateProcessor() {

		assertTrue(ConnectorKeepOnlyMatchingLinesProperty.TYPE.getConnectorStateProcessor() instanceof TypeProcessor);
	}
}