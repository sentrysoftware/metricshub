package com.sentrysoftware.metricshub.cli.service.converter;

import static org.junit.jupiter.api.Assertions.assertEquals;

import com.sentrysoftware.metricshub.engine.configuration.SnmpConfiguration;
import org.junit.jupiter.api.Test;

class SnmpPrivacyConverterTest {

	@Test
	void testConvert() {
		final SnmpPrivacyConverter snmpPrivacyConverter = new SnmpPrivacyConverter();

		// Check Snmp privacy conversion
		assertEquals(SnmpConfiguration.Privacy.DES, snmpPrivacyConverter.convert("des"));
		assertEquals(SnmpConfiguration.Privacy.AES, snmpPrivacyConverter.convert("aes"));
		assertEquals(SnmpConfiguration.Privacy.NO_ENCRYPTION, snmpPrivacyConverter.convert("none"));
		assertEquals(SnmpConfiguration.Privacy.NO_ENCRYPTION, snmpPrivacyConverter.convert("no"));
	}
}
