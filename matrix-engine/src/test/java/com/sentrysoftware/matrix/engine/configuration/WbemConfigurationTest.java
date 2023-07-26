package com.sentrysoftware.matrix.engine.configuration;

import org.junit.jupiter.api.Test;

import static com.sentrysoftware.matrix.constants.Constants.PASSWORD;
import static com.sentrysoftware.matrix.constants.Constants.USERNAME;
import static com.sentrysoftware.matrix.constants.Constants.WBEM_CONFIGURATION_TO_STRING;
import static com.sentrysoftware.matrix.constants.Constants.WBEM_NAMESPACE;
import static com.sentrysoftware.matrix.constants.Constants.WBEM_VCENTER;
import static org.junit.jupiter.api.Assertions.assertEquals;

public class WbemConfigurationTest {

	@Test
	public void testToString() {
		final WbemConfiguration wbemConfiguration = new WbemConfiguration();
		wbemConfiguration.setUsername(USERNAME);
		wbemConfiguration.setPassword(PASSWORD.toCharArray());
		wbemConfiguration.setNamespace(WBEM_NAMESPACE);
		wbemConfiguration.setVCenter(WBEM_VCENTER);
		assertEquals(WBEM_CONFIGURATION_TO_STRING, wbemConfiguration.toString());
	}
}
