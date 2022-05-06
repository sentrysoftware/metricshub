package com.sentrysoftware.matrix.connector.parser.state.source.snmptable;

import com.sentrysoftware.matrix.connector.model.monitor.job.source.type.snmp.SnmpGetTableSource;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;

class SnmpTableProcessorTest {

	@Test
	void testGetType() {

		assertEquals(SnmpGetTableSource.class, new SnmpTableOidProcessor().getType());
	}

	@Test
	void testGetTypeValue() {

		assertEquals(SnmpTableProcessor.SNMP_TABLE_TYPE_VALUE, new SnmpTableOidProcessor().getTypeValue());
	}
}