package com.sentrysoftware.matrix.engine.configuration;

import org.junit.Test;

import static com.sentrysoftware.matrix.constants.Constants.PASSWORD;
import static com.sentrysoftware.matrix.constants.Constants.SNMP_CONFIGURATION_TO_STRING;
import static com.sentrysoftware.matrix.constants.Constants.USERNAME;
import static org.junit.jupiter.api.Assertions.assertEquals;

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
}
