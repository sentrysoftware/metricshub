package com.sentrysoftware.matrix.engine.configuration;

import org.junit.Test;

import static com.sentrysoftware.matrix.constants.Constants.INVALID_PRIVACY_VALUE_EXCEPTION_MESSAGE;
import static com.sentrysoftware.matrix.constants.Constants.INVALID_SNMP_VERSION;
import static com.sentrysoftware.matrix.constants.Constants.PASSWORD;
import static com.sentrysoftware.matrix.constants.Constants.SNMP_CONFIGURATION_TO_STRING;
import static com.sentrysoftware.matrix.constants.Constants.USERNAME;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

public class SnmpConfigurationTest {

	@Test
	public void testToString() {
		final SnmpConfiguration snmpConfiguration = new SnmpConfiguration();
		snmpConfiguration.setUsername(USERNAME);
		snmpConfiguration.setPassword(PASSWORD.toCharArray());
		snmpConfiguration.setPrivacy(SnmpConfiguration.Privacy.AES);
		snmpConfiguration.setPrivacyPassword(PASSWORD.toCharArray());
		assertEquals(SNMP_CONFIGURATION_TO_STRING, snmpConfiguration.toString());
	}

	@Test
	public void testSnmpVersionInterpretValueOf() {
		assertEquals(SnmpConfiguration.SnmpVersion.V1, SnmpConfiguration.SnmpVersion.interpretValueOf("1"));
		assertEquals(SnmpConfiguration.SnmpVersion.V2C, SnmpConfiguration.SnmpVersion.interpretValueOf("2"));
		assertEquals(SnmpConfiguration.SnmpVersion.V3_MD5, SnmpConfiguration.SnmpVersion.interpretValueOf("3 md5"));
		assertEquals(SnmpConfiguration.SnmpVersion.V3_NO_AUTH, SnmpConfiguration.SnmpVersion.interpretValueOf("v3_no_auth"));
		assertEquals(SnmpConfiguration.SnmpVersion.V3_SHA, SnmpConfiguration.SnmpVersion.interpretValueOf("v3 with sha auth"));
		final Exception exception = assertThrows(IllegalArgumentException.class, () -> {
			SnmpConfiguration.SnmpVersion.interpretValueOf("4");
		});
		final String actualMessage = exception.getMessage();
		assertTrue(actualMessage.contains(INVALID_SNMP_VERSION));
	}

	@Test
	public void testPrivacyInterpretValueOf() {
		assertEquals(SnmpConfiguration.Privacy.DES, SnmpConfiguration.Privacy.interpretValueOf("des"));
		assertEquals(SnmpConfiguration.Privacy.AES, SnmpConfiguration.Privacy.interpretValueOf("aes"));
		assertEquals(SnmpConfiguration.Privacy.NO_ENCRYPTION, SnmpConfiguration.Privacy.interpretValueOf("no"));
		assertEquals(SnmpConfiguration.Privacy.NO_ENCRYPTION, SnmpConfiguration.Privacy.interpretValueOf("none"));
		final Exception exception = assertThrows(IllegalArgumentException.class, () -> {
			SnmpConfiguration.Privacy.interpretValueOf("sha-256");
		});
		final String actualMessage = exception.getMessage();
		assertTrue(actualMessage.contains(INVALID_PRIVACY_VALUE_EXCEPTION_MESSAGE));
	}
}
