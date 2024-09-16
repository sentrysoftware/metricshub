package org.sentrysoftware.metricshub.extension.ping;

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

import org.junit.jupiter.api.Test;
import org.sentrysoftware.metricshub.engine.common.exception.InvalidConfigurationException;

/**
 * Test of {@link PingConfiguration}
 */
class PingConfigurationTest {

	private static final String RESOURCE_KEY = "resource-test-key";

	@Test
	void testValidateConfiguration() {
		assertThrows(
			InvalidConfigurationException.class,
			() -> PingConfiguration.builder().timeout(-60L).build().validateConfiguration(RESOURCE_KEY)
		);
		assertThrows(
			InvalidConfigurationException.class,
			() -> PingConfiguration.builder().timeout(null).build().validateConfiguration(RESOURCE_KEY)
		);
		assertDoesNotThrow(() -> PingConfiguration.builder().timeout(60L).build().validateConfiguration(RESOURCE_KEY));
	}

	@Test
	void testCopy() {
		final PingConfiguration pingConfiguration = PingConfiguration.builder().timeout(100L).build();

		assertEquals(pingConfiguration, pingConfiguration.copy());
	}
}
