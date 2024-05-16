package org.sentrysoftware.metricshub.extension.winrm;

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

import org.junit.jupiter.api.Test;
import org.sentrysoftware.metricshub.engine.common.exception.InvalidConfigurationException;

class WinRmConfigurationTest {

	@Test
	void testValidateConfiguration() throws InvalidConfigurationException {
		assertDoesNotThrow(() ->
			WinRmConfiguration
				.builder()
				.username("user")
				.password("pass".toCharArray())
				.namespace("namespace")
				.port(443)
				.timeout(15L)
				.build()
				.validateConfiguration("resourceKey")
		);
		assertThrows(
			InvalidConfigurationException.class,
			() ->
				WinRmConfiguration
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
			"WinRm as Administrator",
			WinRmConfiguration
				.builder()
				.username("Administrator")
				.password("passwd".toCharArray())
				.timeout(15L)
				.build()
				.toString()
		);
	}
}
