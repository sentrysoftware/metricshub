package org.sentrysoftware.metricshub.engine.configuration;

import static org.junit.jupiter.api.Assertions.assertEquals;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.sentrysoftware.metricshub.engine.constants.Constants;

/**
 * Test of {@link OsCommandConfiguration}
 */
class OsCommandConfigurationTest {

	@Test
	void testToString() {
		final OsCommandConfiguration osCommandConfiguration = new OsCommandConfiguration();
		Assertions.assertEquals(Constants.OS_COMMAND_CONFIGURATION_TO_STRING, osCommandConfiguration.toString());
	}
}
