package com.sentrysoftware.matrix.engine.configuration;

import org.junit.Test;

import static com.sentrysoftware.matrix.constants.Constants.PASSWORD;
import static com.sentrysoftware.matrix.constants.Constants.USERNAME;
import static com.sentrysoftware.matrix.constants.Constants.WMI_CONFIGURATION_TO_STRING;
import static com.sentrysoftware.matrix.constants.Constants.WMI_NAMESPACE;
import static org.junit.jupiter.api.Assertions.assertEquals;

public class WmiConfigurationTest {

	@Test
	public void testToString() {
		final WmiConfiguration wmiConfiguration = new WmiConfiguration();
		wmiConfiguration.setUsername(USERNAME);
		wmiConfiguration.setPassword(PASSWORD.toCharArray());
		wmiConfiguration.setNamespace(WMI_NAMESPACE);
		assertEquals(WMI_CONFIGURATION_TO_STRING, wmiConfiguration.toString());
	}
}
