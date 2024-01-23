package org.sentrysoftware.metricshub.cli.service.converter;

import static org.junit.jupiter.api.Assertions.assertEquals;

import org.junit.jupiter.api.Test;
import org.sentrysoftware.metricshub.engine.configuration.SnmpConfiguration;

class SnmpVersionConverterTest {

	@Test
	void testConvert() {
		final SnmpVersionConverter snmpVersionConverter = new SnmpVersionConverter();

		// Check Snmp version conversion
		assertEquals(SnmpConfiguration.SnmpVersion.V3_MD5, snmpVersionConverter.convert("v3 md5"));
		assertEquals(SnmpConfiguration.SnmpVersion.V3_NO_AUTH, snmpVersionConverter.convert("v3 no auth"));
		assertEquals(SnmpConfiguration.SnmpVersion.V2C, snmpVersionConverter.convert("v2"));
		assertEquals(SnmpConfiguration.SnmpVersion.V1, snmpVersionConverter.convert("v1"));
	}
}
