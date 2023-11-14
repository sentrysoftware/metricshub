package com.sentrysoftware.metricshub.cli.service;

import com.sentrysoftware.metricshub.cli.service.converter.SnmpPrivacyConverter;
import com.sentrysoftware.metricshub.cli.service.converter.SnmpVersionConverter;
import com.sentrysoftware.metricshub.cli.service.protocol.SnmpConfigCli;
import com.sentrysoftware.metricshub.engine.configuration.SnmpConfiguration;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;

class MetricsHubCliServiceTest {

	@Test
	void testSnmpVersionConverter(){

		final SnmpVersionConverter snmpVersionConverter = new SnmpVersionConverter();

		// Check Snmp version conversion
		assertEquals(SnmpConfiguration.SnmpVersion.V3_MD5, snmpVersionConverter.convert("v3 md5"));
		assertEquals(SnmpConfiguration.SnmpVersion.V3_NO_AUTH, snmpVersionConverter.convert("v3 no auth"));
		assertEquals(SnmpConfiguration.SnmpVersion.V2C, snmpVersionConverter.convert("v2"));
		assertEquals(SnmpConfiguration.SnmpVersion.V1, snmpVersionConverter.convert("v1"));
	}

	@Test
	void testSnmpPrivacyConverter(){

		final SnmpPrivacyConverter snmpPrivacyConverter = new SnmpPrivacyConverter();

		// Check Snmp privacy conversion
		assertEquals(SnmpConfiguration.Privacy.DES, snmpPrivacyConverter.convert("des"));
		assertEquals(SnmpConfiguration.Privacy.AES, snmpPrivacyConverter.convert("aes"));
		assertEquals(SnmpConfiguration.Privacy.NO_ENCRYPTION, snmpPrivacyConverter.convert("none"));
		assertEquals(SnmpConfiguration.Privacy.NO_ENCRYPTION, snmpPrivacyConverter.convert("no"));
	}

	@Test
	void testSnmpConfigCliToProtocol(){

		// Create a SnmpConfigCli instance and set its data
		final SnmpConfigCli snmpConfigCli = new SnmpConfigCli();
		snmpConfigCli.setSnmpVersion(SnmpConfiguration.SnmpVersion.V1);
		snmpConfigCli.setPort(80);
		snmpConfigCli.setCommunity("community");
		snmpConfigCli.setPrivacy(SnmpConfiguration.Privacy.AES);
		snmpConfigCli.setTimeout(120L);
		snmpConfigCli.setPrivacyPassword("def".toCharArray());

		// Create an SnmpConfiguration and call method toProtocol

		final SnmpConfiguration snmpConfiguration = snmpConfigCli.toProtocol("user1", "abc".toCharArray());

		// Check the resulting snmp configuration
		assertNotNull(snmpConfiguration);
		assertEquals(SnmpConfiguration.SnmpVersion.V1, snmpConfiguration.getVersion());
		assertEquals("SNMP v1 (community)", snmpConfiguration.toString());
	}
}
