package org.sentrysoftware.metricshub.engine.configuration;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.sentrysoftware.metricshub.engine.constants.Constants.AES;
import static org.sentrysoftware.metricshub.engine.constants.Constants.DES;
import static org.sentrysoftware.metricshub.engine.constants.Constants.INVALID_PRIVACY_VALUE;
import static org.sentrysoftware.metricshub.engine.constants.Constants.INVALID_PRIVACY_VALUE_EXCEPTION_MESSAGE;
import static org.sentrysoftware.metricshub.engine.constants.Constants.INVALID_SNMP_VERSION;
import static org.sentrysoftware.metricshub.engine.constants.Constants.NO;
import static org.sentrysoftware.metricshub.engine.constants.Constants.NONE;
import static org.sentrysoftware.metricshub.engine.constants.Constants.SNMP_CONFIGURATION_ENCRYPTED_TO_STRING;
import static org.sentrysoftware.metricshub.engine.constants.Constants.SNMP_CONFIGURATION_NO_PRIVACY_TO_STRING;
import static org.sentrysoftware.metricshub.engine.constants.Constants.SNMP_CONFIGURATION_NO_PRIVACY_WITH_USERNAME_TO_STRING;
import static org.sentrysoftware.metricshub.engine.constants.Constants.SNMP_CONFIGURATION_V1_TO_STRING;
import static org.sentrysoftware.metricshub.engine.constants.Constants.SNMP_CONFIGURATION_V2C_TO_STRING;
import static org.sentrysoftware.metricshub.engine.constants.Constants.USERNAME;

import java.util.Map;
import org.junit.jupiter.api.Test;
import org.sentrysoftware.metricshub.engine.telemetry.TelemetryManager;

/**
 * Test of {@link SnmpConfiguration}
 */
class SnmpConfigurationTest {

	@Test
	void testToString() {
		// Privacy is not NULL, version is V3_MD5 and username is NULL
		SnmpConfiguration snmpConfiguration = SnmpConfiguration
			.builder()
			.version(SnmpConfiguration.SnmpVersion.V3_MD5)
			.privacy(SnmpConfiguration.Privacy.AES)
			.build();
		assertEquals(SNMP_CONFIGURATION_ENCRYPTED_TO_STRING, snmpConfiguration.toString());

		// Privacy is NULL, version is V3_MD5 and username is NULL
		snmpConfiguration = SnmpConfiguration.builder().privacy(null).version(SnmpConfiguration.SnmpVersion.V3_MD5).build();
		assertEquals(SNMP_CONFIGURATION_NO_PRIVACY_TO_STRING, snmpConfiguration.toString());

		// Privacy is NULL, version is V3_MD5 and username is not NULL
		snmpConfiguration =
			SnmpConfiguration.builder().username(USERNAME).version(SnmpConfiguration.SnmpVersion.V3_MD5).build();
		assertEquals(SNMP_CONFIGURATION_NO_PRIVACY_WITH_USERNAME_TO_STRING, snmpConfiguration.toString());

		// Privacy is NULL, version is V1 and username is NULL
		snmpConfiguration = SnmpConfiguration.builder().version(SnmpConfiguration.SnmpVersion.V1).build();
		assertEquals(SNMP_CONFIGURATION_V1_TO_STRING, snmpConfiguration.toString());

		// Privacy is NULL, version is V2C and username is NULL
		snmpConfiguration = SnmpConfiguration.builder().version(SnmpConfiguration.SnmpVersion.V2C).build();
		assertEquals(SNMP_CONFIGURATION_V2C_TO_STRING, snmpConfiguration.toString());

		// Privacy is not NULL, version is V3_MD5 and username is not NULL
		snmpConfiguration =
			SnmpConfiguration
				.builder()
				.version(SnmpConfiguration.SnmpVersion.V3_MD5)
				.privacy(SnmpConfiguration.Privacy.NO_ENCRYPTION)
				.username(USERNAME)
				.build();
		assertEquals(SNMP_CONFIGURATION_NO_PRIVACY_WITH_USERNAME_TO_STRING, snmpConfiguration.toString());
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

		// Version 3
		assertEquals(SnmpConfiguration.SnmpVersion.V3_MD5, SnmpConfiguration.SnmpVersion.interpretValueOf("3 md5"));
		assertEquals(
			SnmpConfiguration.SnmpVersion.V3_NO_AUTH,
			SnmpConfiguration.SnmpVersion.interpretValueOf("v3_no_auth")
		);
		assertEquals(
			SnmpConfiguration.SnmpVersion.V3_SHA,
			SnmpConfiguration.SnmpVersion.interpretValueOf("v3 with sha auth")
		);

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

	@Test
	void testPrivacyInterpretValueOf() {
		// Encryption is defined: privacy is not equal to NO and not equal to NONE
		assertEquals(SnmpConfiguration.Privacy.DES, SnmpConfiguration.Privacy.interpretValueOf(DES));
		assertEquals(SnmpConfiguration.Privacy.AES, SnmpConfiguration.Privacy.interpretValueOf(AES));

		// No encryption: privacy is equal to NO or equal to NONE
		assertEquals(SnmpConfiguration.Privacy.NO_ENCRYPTION, SnmpConfiguration.Privacy.interpretValueOf(NO));
		assertEquals(SnmpConfiguration.Privacy.NO_ENCRYPTION, SnmpConfiguration.Privacy.interpretValueOf(NONE));

		// Invalid privacy value
		final Exception exception = assertThrows(
			IllegalArgumentException.class,
			() -> {
				SnmpConfiguration.Privacy.interpretValueOf(INVALID_PRIVACY_VALUE);
			}
		);
		final String actualMessage = exception.getMessage();
		assertTrue(actualMessage.contains(INVALID_PRIVACY_VALUE_EXCEPTION_MESSAGE));
	}

	@Test
	void testContextNameTransfer() {
		final TelemetryManager telemetryManager = TelemetryManager
			.builder()
			.hostConfiguration(
				HostConfiguration
					.builder()
					.configurations(Map.of(SnmpConfiguration.class, SnmpConfiguration.builder().contextName("context-1").build()))
					.build()
			)
			.build();
		final SnmpConfiguration snmpConfiguration = (SnmpConfiguration) telemetryManager
			.getHostConfiguration()
			.getConfigurations()
			.get(SnmpConfiguration.class);
		assertEquals("context-1", snmpConfiguration.getContextName());
	}
}
