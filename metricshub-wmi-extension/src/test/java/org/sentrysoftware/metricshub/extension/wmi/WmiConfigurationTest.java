package org.sentrysoftware.metricshub.extension.wmi;

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

import org.junit.jupiter.api.Test;
import org.sentrysoftware.metricshub.engine.common.exception.InvalidConfigurationException;

class WmiConfigurationTest {

	@Test
	void testValidateConfiguration() throws InvalidConfigurationException {
		assertDoesNotThrow(() ->
			WmiConfiguration
				.builder()
				.username("user")
				.password("pass".toCharArray())
				.timeout(15L)
				.build()
				.validateConfiguration("resourceKey")
		);
		assertThrows(
			InvalidConfigurationException.class,
			() ->
				WmiConfiguration
					.builder()
					.username("user")
					.password("pass".toCharArray())
					.timeout(-15L) // Bad timeout
					.build()
					.validateConfiguration("resourceKey")
		);
	}

	@Test
	void testToString() {
		assertEquals(
			"WMI as Administrator",
			WmiConfiguration
				.builder()
				.username("Administrator")
				.password("passwd".toCharArray())
				.timeout(15L)
				.build()
				.toString()
		);
	}

	@Test
	void testCopy() {
		final WmiConfiguration wmiConfiguration = WmiConfiguration
			.builder()
			.namespace("namespace")
			.password("password".toCharArray())
			.timeout(100L)
			.username("username")
			.build();
		assertEquals(wmiConfiguration, wmiConfiguration.copy());
	}
}
