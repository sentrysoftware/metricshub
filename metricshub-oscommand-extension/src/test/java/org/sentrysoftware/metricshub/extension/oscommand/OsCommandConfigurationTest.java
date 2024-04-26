package org.sentrysoftware.metricshub.extension.oscommand;

import static org.junit.jupiter.api.Assertions.assertEquals;

import org.junit.jupiter.api.Test;

/**
 * Test of {@link OsCommandConfiguration}
 */
class OsCommandConfigurationTest {

	public static final String OS_COMMAND_CONFIGURATION_TO_STRING = "Local Commands";

	@Test
	void testToString() {
		final OsCommandConfiguration osCommandConfiguration = new OsCommandConfiguration();
		assertEquals(OS_COMMAND_CONFIGURATION_TO_STRING, osCommandConfiguration.toString());
	}
}
