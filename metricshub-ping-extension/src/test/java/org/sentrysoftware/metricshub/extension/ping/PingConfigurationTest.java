package org.sentrysoftware.metricshub.extension.ping;

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
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
			() -> PingConfiguration.builder().timeout(-60L).maxAttempts(1234).build().validateConfiguration(RESOURCE_KEY)
		);
		assertThrows(
			InvalidConfigurationException.class,
			() -> PingConfiguration.builder().timeout(null).maxAttempts(1234).build().validateConfiguration(RESOURCE_KEY)
		);
		assertThrows(
			InvalidConfigurationException.class,
			() -> PingConfiguration.builder().timeout(60L).maxAttempts(-1).build().validateConfiguration(RESOURCE_KEY)
		);
		assertThrows(
			InvalidConfigurationException.class,
			() -> PingConfiguration.builder().timeout(60L).maxAttempts(null).build().validateConfiguration(RESOURCE_KEY)
		);
		assertDoesNotThrow(() ->
			PingConfiguration.builder().timeout(60L).maxAttempts(1234).build().validateConfiguration(RESOURCE_KEY)
		);
	}
}
