package org.sentrysoftware.metricshub.engine.configuration;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.sentrysoftware.metricshub.engine.constants.Constants.OS_COMMAND_CONFIGURATION_TO_STRING;

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
