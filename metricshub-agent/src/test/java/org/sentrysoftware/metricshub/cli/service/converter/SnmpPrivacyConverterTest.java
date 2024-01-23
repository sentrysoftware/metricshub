package org.sentrysoftware.metricshub.cli.service.converter;

import static org.junit.jupiter.api.Assertions.assertEquals;

import org.junit.jupiter.api.Test;
import org.sentrysoftware.metricshub.engine.configuration.SnmpConfiguration;

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
