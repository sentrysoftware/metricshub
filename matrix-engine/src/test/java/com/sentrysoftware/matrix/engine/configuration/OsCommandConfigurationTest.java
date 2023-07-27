package com.sentrysoftware.matrix.engine.configuration;

import org.junit.Test;

import static com.sentrysoftware.matrix.constants.Constants.OS_COMMAND_CONFIGURATION_TO_STRING;
import static org.junit.jupiter.api.Assertions.assertEquals;

/**
 * Test of {@link OsCommandConfiguration}
 */
public class OsCommandConfigurationTest {
	@Test
	public void testToString() {
		final OsCommandConfiguration osCommandConfiguration = new OsCommandConfiguration();
		assertEquals(OS_COMMAND_CONFIGURATION_TO_STRING, osCommandConfiguration.toString());
	}
}
