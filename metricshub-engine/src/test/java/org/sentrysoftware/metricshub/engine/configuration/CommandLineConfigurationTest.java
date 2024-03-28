package org.sentrysoftware.metricshub.engine.configuration;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.sentrysoftware.metricshub.engine.constants.Constants.OS_COMMAND_CONFIGURATION_TO_STRING;

import org.junit.jupiter.api.Test;

/**
 * Test of {@link CommandLineConfiguration}
 */
class CommandLineConfigurationTest {

	@Test
	void testToString() {
		final CommandLineConfiguration commandLineConfiguration = new CommandLineConfiguration();
		assertEquals(OS_COMMAND_CONFIGURATION_TO_STRING, commandLineConfiguration.toString());
	}
}
