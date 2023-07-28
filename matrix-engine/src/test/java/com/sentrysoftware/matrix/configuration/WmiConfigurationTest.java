package com.sentrysoftware.matrix.configuration;

import static com.sentrysoftware.matrix.constants.Constants.PASSWORD;
import static com.sentrysoftware.matrix.constants.Constants.USERNAME;
import static com.sentrysoftware.matrix.constants.Constants.WMI;
import static com.sentrysoftware.matrix.constants.Constants.WMI_CONFIGURATION_TO_STRING;
import static com.sentrysoftware.matrix.constants.Constants.WMI_NAMESPACE;
import static org.junit.jupiter.api.Assertions.assertEquals;

import org.junit.jupiter.api.Test;

/**
 * Test of {@link WmiConfiguration}
 */
class WmiConfigurationTest {

	@Test
	void testToString() {
		final WmiConfiguration wmiConfiguration = new WmiConfiguration();

		// When the userName is NOT null, it's appended to the result
		wmiConfiguration.setUsername(USERNAME);
		wmiConfiguration.setPassword(PASSWORD.toCharArray());
		wmiConfiguration.setNamespace(WMI_NAMESPACE);
		assertEquals(WMI_CONFIGURATION_TO_STRING, wmiConfiguration.toString());

		// When the userName is null, it's not appended to the result
		wmiConfiguration.setUsername(null);
		assertEquals(WMI, wmiConfiguration.toString());
	}
}
