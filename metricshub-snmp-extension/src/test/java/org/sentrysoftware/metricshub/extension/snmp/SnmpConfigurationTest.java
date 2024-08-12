package org.sentrysoftware.metricshub.extension.snmp;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

import org.junit.jupiter.api.Test;
import org.sentrysoftware.metricshub.engine.common.exception.InvalidConfigurationException;
import org.sentrysoftware.metricshub.extension.snmp.SnmpConfiguration.SnmpVersion;

class SnmpConfigurationTest {

	@Test
	void testValidateConfiguration() {
		final String resourceKey = "resourceKey";

		final char[] community = "public".toCharArray();
		final char[] emptyCommunity = new char[] {};

		{
			final SnmpConfiguration snmpConfig = SnmpConfiguration
				.builder()
				.community(emptyCommunity)
				.port(1234)
				.timeout(60L)
				.version(SnmpVersion.V1)
				.build();

			assertThrows(InvalidConfigurationException.class, () -> snmpConfig.validateConfiguration(resourceKey));
		}

		{
			final SnmpConfiguration snmpConfig = SnmpConfiguration
				.builder()
				.community(null)
				.port(1234)
				.timeout(60L)
				.version(SnmpVersion.V1)
				.build();

			assertThrows(InvalidConfigurationException.class, () -> snmpConfig.validateConfiguration(resourceKey));
		}

		{
			final SnmpConfiguration snmpConfig = SnmpConfiguration
				.builder()
				.community(community)
				.port(-1)
				.timeout(60L)
				.version(SnmpVersion.V1)
				.build();

			assertThrows(InvalidConfigurationException.class, () -> snmpConfig.validateConfiguration(resourceKey));
		}

		{
			final SnmpConfiguration snmpConfig = SnmpConfiguration
				.builder()
				.community(community)
				.port(66666)
				.timeout(60L)
				.version(SnmpVersion.V1)
				.build();

			assertThrows(InvalidConfigurationException.class, () -> snmpConfig.validateConfiguration(resourceKey));
		}

		{
			final SnmpConfiguration snmpConfig = SnmpConfiguration
				.builder()
				.community(community)
				.port(null)
				.timeout(60L)
				.version(SnmpVersion.V1)
				.build();

			assertThrows(InvalidConfigurationException.class, () -> snmpConfig.validateConfiguration(resourceKey));
		}

		{
			final SnmpConfiguration snmpConfig = SnmpConfiguration
				.builder()
				.community(community)
				.port(1234)
				.timeout(-60L)
				.version(SnmpVersion.V1)
				.build();

			assertThrows(InvalidConfigurationException.class, () -> snmpConfig.validateConfiguration(resourceKey));
		}

		{
			final SnmpConfiguration snmpConfig = SnmpConfiguration
				.builder()
				.community(community)
				.port(1234)
				.timeout(null)
				.version(SnmpVersion.V1)
				.build();

			assertThrows(InvalidConfigurationException.class, () -> snmpConfig.validateConfiguration(resourceKey));
		}
	}

	@Test
	void testSnmpVersionInterpretValueOf() {
		assertEquals(SnmpVersion.V1, SnmpVersion.interpretValueOf("1"));
		assertEquals(SnmpVersion.V1, SnmpVersion.interpretValueOf("v1"));
		assertEquals(SnmpVersion.V2C, SnmpVersion.interpretValueOf("2"));
		assertEquals(SnmpVersion.V2C, SnmpVersion.interpretValueOf("v2"));
		assertEquals(SnmpVersion.V2C, SnmpVersion.interpretValueOf("v2c"));
		assertEquals(SnmpVersion.V2C, SnmpVersion.interpretValueOf("2c"));
	}
}
