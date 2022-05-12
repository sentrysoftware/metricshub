package com.sentrysoftware.matrix.connector.parser.state.source.snmpget;

import static org.junit.jupiter.api.Assertions.assertEquals;

import org.junit.jupiter.api.Test;

import com.sentrysoftware.matrix.connector.model.monitor.job.source.type.snmp.SNMPGetSource;

class SnmpGetProcessorTest {
	@Test
	void testGetType() {
		assertEquals(SNMPGetSource.class, new SnmpGetOidProcessor().getType());
	}

	@Test
	void testGetTypeValue() {
		assertEquals(SnmpGetProcessor.SNMP_GET_TYPE_VALUE, new SnmpGetOidProcessor().getTypeValue());
	}
}
