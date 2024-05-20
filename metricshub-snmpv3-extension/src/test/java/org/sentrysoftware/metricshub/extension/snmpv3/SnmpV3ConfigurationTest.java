package org.sentrysoftware.metricshub.extension.snmpv3;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

import org.junit.jupiter.api.Test;
import org.sentrysoftware.metricshub.engine.common.exception.InvalidConfigurationException;
import org.sentrysoftware.metricshub.extension.snmpv3.SnmpV3Configuration.*;

class SnmpV3ConfigurationTest {

	@Test
	void testValidateConfiguration() {
		final String resourceKey = "resourceKey";

		final char[] community = "public".toCharArray();
		final char[] emptyCommunity = new char[] {};

		// Test when community is empty
		{
			final SnmpV3Configuration snmpV3Config = SnmpV3Configuration
				.builder()
				.community(emptyCommunity)
				.port(1234)
				.timeout(60L)
				.authType(AuthType.NO_AUTH)
				.privacy(Privacy.NO_ENCRYPTION)
				.build();

			assertThrows(InvalidConfigurationException.class, () -> snmpV3Config.validateConfiguration(resourceKey));
		}

		// Test when community is null
		{
			final SnmpV3Configuration snmpV3Config = SnmpV3Configuration
				.builder()
				.community(null)
				.port(1234)
				.timeout(60L)
				.authType(AuthType.NO_AUTH)
				.privacy(Privacy.NO_ENCRYPTION)
				.build();

			assertThrows(InvalidConfigurationException.class, () -> snmpV3Config.validateConfiguration(resourceKey));
		}

		// Test when port is negative
		{
			final SnmpV3Configuration snmpV3Config = SnmpV3Configuration
				.builder()
				.community(community)
				.port(-1)
				.timeout(60L)
				.authType(AuthType.NO_AUTH)
				.privacy(Privacy.NO_ENCRYPTION)
				.build();

			assertThrows(InvalidConfigurationException.class, () -> snmpV3Config.validateConfiguration(resourceKey));
		}

		// Test when port is greater than maximum allowed value
		{
			final SnmpV3Configuration snmpConfig = SnmpV3Configuration
				.builder()
				.community(community)
				.port(66666)
				.timeout(60L)
				.authType(AuthType.NO_AUTH)
				.privacy(Privacy.NO_ENCRYPTION)
				.build();

			assertThrows(InvalidConfigurationException.class, () -> snmpConfig.validateConfiguration(resourceKey));
		}

		// Test when port is null
		{
			final SnmpV3Configuration snmpV3Config = SnmpV3Configuration
				.builder()
				.community(community)
				.port(null)
				.timeout(60L)
				.authType(AuthType.NO_AUTH)
				.privacy(Privacy.NO_ENCRYPTION)
				.build();

			assertThrows(InvalidConfigurationException.class, () -> snmpV3Config.validateConfiguration(resourceKey));
		}

		// Test when timeout is negative
		{
			final SnmpV3Configuration snmpV3Config = SnmpV3Configuration
				.builder()
				.community(community)
				.port(1234)
				.timeout(-60L)
				.authType(AuthType.NO_AUTH)
				.privacy(Privacy.NO_ENCRYPTION)
				.build();

			assertThrows(InvalidConfigurationException.class, () -> snmpV3Config.validateConfiguration(resourceKey));
		}

		// Test when timeout is null
		{
			final SnmpV3Configuration snmpV3Config = SnmpV3Configuration
				.builder()
				.community(community)
				.port(1234)
				.timeout(null)
				.authType(AuthType.NO_AUTH)
				.privacy(Privacy.NO_ENCRYPTION)
				.build();

			assertThrows(InvalidConfigurationException.class, () -> snmpV3Config.validateConfiguration(resourceKey));
		}

		// Test when username is null
		{
			final SnmpV3Configuration snmpV3Config = SnmpV3Configuration
				.builder()
				.community(community)
				.port(1234)
				.timeout(60L)
				.authType(AuthType.NO_AUTH)
				.privacy(Privacy.AES)
				.username(null)
				.build();

			assertThrows(InvalidConfigurationException.class, () -> snmpV3Config.validateConfiguration(resourceKey));
		}

		// Test when timeout is zero
		{
			final SnmpV3Configuration snmpV3Config = SnmpV3Configuration
				.builder()
				.community(community)
				.port(1234)
				.timeout(0L)
				.authType(AuthType.NO_AUTH)
				.privacy(Privacy.NO_ENCRYPTION)
				.build();

			assertThrows(InvalidConfigurationException.class, () -> snmpV3Config.validateConfiguration(resourceKey));
		}

		// Test when username is empty
		{
			final SnmpV3Configuration snmpV3Config = SnmpV3Configuration
				.builder()
				.community(community)
				.port(1234)
				.timeout(60L)
				.authType(AuthType.SHA)
				.privacy(Privacy.AES)
				.username("")
				.build();

			assertThrows(InvalidConfigurationException.class, () -> snmpV3Config.validateConfiguration(resourceKey));
		}

		// Test when authType is null
		{
			final SnmpV3Configuration snmpV3Config = SnmpV3Configuration
				.builder()
				.community(community)
				.port(1234)
				.timeout(60L)
				.authType(null)
				.privacy(Privacy.AES)
				.username("user")
				.build();

			assertThrows(InvalidConfigurationException.class, () -> snmpV3Config.validateConfiguration(resourceKey));
		}
	}

	@Test
	void testPrivacyInterpretValueOf_ValidValues() {
		assertEquals(Privacy.NO_ENCRYPTION, Privacy.interpretValueOf("NO_ENCRYPTION"));
		assertEquals(Privacy.AES, Privacy.interpretValueOf("AES"));
		assertEquals(Privacy.DES, Privacy.interpretValueOf("DES"));
	}

	@Test
	void testPrivacyInterpretValueOf_InvalidValue() {
		assertThrows(IllegalArgumentException.class, () -> Privacy.interpretValueOf("INVALID_PRIVACY"));
	}
}
