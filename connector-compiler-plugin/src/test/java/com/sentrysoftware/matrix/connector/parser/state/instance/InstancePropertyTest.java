package com.sentrysoftware.matrix.connector.parser.state.instance;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertTrue;

class InstancePropertyTest {

	@Test
	void testGetConnectorStateProcessor() {

		assertTrue(InstanceProperty.INSTANCE.getConnectorStateProcessor() instanceof InstanceProcessor);
	}
}