package org.sentrysoftware.metricshub.engine.configuration;

import static org.junit.jupiter.api.Assertions.assertEquals;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.sentrysoftware.metricshub.engine.constants.Constants;

/**
 * Test of {@link WmiConfiguration}
 */
class WmiConfigurationTest {

	@Test
	void testToString() {
		final WmiConfiguration wmiConfiguration = new WmiConfiguration();

		// When the userName is NOT null, it's appended to the result
		wmiConfiguration.setUsername(Constants.USERNAME);
		wmiConfiguration.setPassword(Constants.PASSWORD.toCharArray());
		wmiConfiguration.setNamespace(Constants.WMI_NAMESPACE);
		Assertions.assertEquals(Constants.WMI_CONFIGURATION_TO_STRING, wmiConfiguration.toString());

		// When the userName is null, it's not appended to the result
		wmiConfiguration.setUsername(null);
		Assertions.assertEquals(Constants.WMI, wmiConfiguration.toString());
	}
}
