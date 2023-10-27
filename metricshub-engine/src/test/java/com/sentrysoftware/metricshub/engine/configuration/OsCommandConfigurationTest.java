package com.sentrysoftware.metricshub.engine.configuration;

import static com.sentrysoftware.metricshub.engine.constants.Constants.OS_COMMAND_CONFIGURATION_TO_STRING;
import static org.junit.jupiter.api.Assertions.assertEquals;

import org.junit.jupiter.api.Test;

/**
 * Test of {@link OsCommandConfiguration}
 */
class OsCommandConfigurationTest {

	@Test
	void testToString() {
		final OsCommandConfiguration osCommandConfiguration = new OsCommandConfiguration();
		assertEquals(OS_COMMAND_CONFIGURATION_TO_STRING, osCommandConfiguration.toString());
	}
}