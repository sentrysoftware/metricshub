package com.sentrysoftware.matrix.connector.parser.state.detection.snmp;

import com.sentrysoftware.matrix.connector.model.detection.criteria.snmp.SNMP;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;

class SnmpProcessorTest {

	@Test
	void testGetType() {

		assertEquals(SNMP.class, new OidProcessor().getType());
	}

	@Test
	void testGetTypeValue() {

		assertEquals(SnmpProcessor.SNMP_TYPE_VALUE, new OidProcessor().getTypeValue());
	}
}