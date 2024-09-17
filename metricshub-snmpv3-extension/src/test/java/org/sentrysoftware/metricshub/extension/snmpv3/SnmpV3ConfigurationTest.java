package org.sentrysoftware.metricshub.extension.snmpv3;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

import org.junit.jupiter.api.Test;
import org.sentrysoftware.metricshub.engine.common.exception.InvalidConfigurationException;
import org.sentrysoftware.metricshub.engine.configuration.IConfiguration;
import org.sentrysoftware.metricshub.extension.snmpv3.SnmpV3Configuration.AuthType;
import org.sentrysoftware.metricshub.extension.snmpv3.SnmpV3Configuration.Privacy;

class SnmpV3ConfigurationTest {

	@Test
	void testValidateConfiguration() {
		final String resourceKey = "resourceKey";

		// Test when port is negative
		{
			final SnmpV3Configuration snmpV3Config = SnmpV3Configuration
				.builder()
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
		assertEquals(Privacy.NO_ENCRYPTION, Privacy.interpretValueOf("no"));
		assertEquals(Privacy.AES, Privacy.interpretValueOf("AES"));
		assertEquals(Privacy.DES, Privacy.interpretValueOf("DES"));
	}

	@Test
	void testPrivacyInterpretValueOf_InvalidValue() {
		assertThrows(IllegalArgumentException.class, () -> Privacy.interpretValueOf("INVALID_PRIVACY"));
	}

	@Test
	void testCopy() {
		final SnmpV3Configuration snmpV3Configuration = SnmpV3Configuration
			.builder()
			.authType(AuthType.MD5)
			.contextName("context")
			.password("password".toCharArray())
			.port(100)
			.privacy(Privacy.AES)
			.privacyPassword("privacyPassword".toCharArray())
			.retryIntervals(new int[] { 10, 10 })
			.timeout(100L)
			.username("username")
			.build();

		final IConfiguration snmpV3ConfigurationCopy = snmpV3Configuration.copy();

		// Verify that the copied configuration has the same values as the original configuration
		assertEquals(snmpV3Configuration, snmpV3ConfigurationCopy);

		// Ensure that the copied configuration is a distinct object
		assert (snmpV3Configuration != snmpV3ConfigurationCopy);
	}
}
