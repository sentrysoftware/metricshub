package org.sentrysoftware.metricshub.engine.configuration;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.sentrysoftware.metricshub.engine.constants.Constants.INVALID_SNMP_VERSION;
import static org.sentrysoftware.metricshub.engine.constants.Constants.NO;
import static org.sentrysoftware.metricshub.engine.constants.Constants.SNMP_CONFIGURATION_V1_TO_STRING;
import static org.sentrysoftware.metricshub.engine.constants.Constants.SNMP_CONFIGURATION_V2C_TO_STRING;

import org.junit.jupiter.api.Test;

/**
 * Test of {@link SnmpConfiguration}
 */
class SnmpConfigurationTest {

	@Test
	void testToString() {
		// Privacy is NULL, version is V1 and username is NULL
		SnmpConfiguration snmpConfiguration = SnmpConfiguration.builder().version(SnmpConfiguration.SnmpVersion.V1).build();
		assertEquals(SNMP_CONFIGURATION_V1_TO_STRING, snmpConfiguration.toString());

		// Privacy is NULL, version is V2C and username is NULL
		snmpConfiguration = SnmpConfiguration.builder().version(SnmpConfiguration.SnmpVersion.V2C).build();
		assertEquals(SNMP_CONFIGURATION_V2C_TO_STRING, snmpConfiguration.toString());
	}

	@Test
	void testSnmpVersionInterpretValueOf() {
		// Version 1
		assertEquals(SnmpConfiguration.SnmpVersion.V1, SnmpConfiguration.SnmpVersion.interpretValueOf("1"));
		assertEquals(SnmpConfiguration.SnmpVersion.V1, SnmpConfiguration.SnmpVersion.interpretValueOf("v1"));

		// Version 2
		assertEquals(SnmpConfiguration.SnmpVersion.V2C, SnmpConfiguration.SnmpVersion.interpretValueOf("2"));
		assertEquals(SnmpConfiguration.SnmpVersion.V2C, SnmpConfiguration.SnmpVersion.interpretValueOf("v2"));
		assertEquals(SnmpConfiguration.SnmpVersion.V2C, SnmpConfiguration.SnmpVersion.interpretValueOf("v2c"));

		// Invalid version, exception is thrown
		Exception exception = assertThrows(
			IllegalArgumentException.class,
			() -> {
				SnmpConfiguration.SnmpVersion.interpretValueOf("4");
			}
		);
		String actualMessage = exception.getMessage();
		assertTrue(actualMessage.contains(INVALID_SNMP_VERSION));

		exception =
			assertThrows(
				IllegalArgumentException.class,
				() -> {
					SnmpConfiguration.SnmpVersion.interpretValueOf(NO);
				}
			);
		actualMessage = exception.getMessage();
		assertTrue(actualMessage.contains(INVALID_SNMP_VERSION));
	}
}
