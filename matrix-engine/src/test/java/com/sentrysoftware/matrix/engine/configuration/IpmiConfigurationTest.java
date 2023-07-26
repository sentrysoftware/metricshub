package com.sentrysoftware.matrix.engine.configuration;

import org.junit.Test;

import static com.sentrysoftware.matrix.constants.Constants.BMC_KEY;
import static com.sentrysoftware.matrix.constants.Constants.IMPI_CONFIGURATION_TO_STRING;
import static com.sentrysoftware.matrix.constants.Constants.PASSWORD;
import static com.sentrysoftware.matrix.constants.Constants.USERNAME;
import static org.junit.jupiter.api.Assertions.assertEquals;

public class IpmiConfigurationTest {

	@Test
	public void testToString() {
		final IpmiConfiguration ipmiConfiguration = new IpmiConfiguration();
		ipmiConfiguration.setUsername(USERNAME);
		ipmiConfiguration.setPassword(PASSWORD.toCharArray());
		ipmiConfiguration.setBmcKey(BMC_KEY);
		ipmiConfiguration.setSkipAuth(false);
		assertEquals(IMPI_CONFIGURATION_TO_STRING, ipmiConfiguration.toString());
	}
}
