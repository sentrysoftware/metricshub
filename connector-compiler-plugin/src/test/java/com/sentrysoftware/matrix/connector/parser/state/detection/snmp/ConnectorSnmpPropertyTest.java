package com.sentrysoftware.matrix.connector.parser.state.detection.snmp;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertTrue;

class ConnectorSnmpPropertyTest {

	@Test
	void testGetConnectorStateProcessor() {

		assertTrue(ConnectorSnmpProperty.OID.getConnectorStateProcessor() instanceof OidProcessor);
	}
}