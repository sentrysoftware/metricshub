package org.sentrysoftware.metricshub.engine.configuration;

import static org.junit.jupiter.api.Assertions.assertEquals;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.sentrysoftware.metricshub.engine.constants.Constants;

/**
 * Test of {@link IpmiConfiguration}
 */
class IpmiConfigurationTest {

	@Test
	void testToString() {
		final IpmiConfiguration ipmiConfiguration = new IpmiConfiguration();

		// When the userName is NOT null, it's appended to the result
		ipmiConfiguration.setUsername(Constants.USERNAME);
		ipmiConfiguration.setPassword(Constants.PASSWORD.toCharArray());
		ipmiConfiguration.setBmcKey(Constants.BMC_KEY);
		ipmiConfiguration.setSkipAuth(false);
		Assertions.assertEquals(Constants.IMPI_CONFIGURATION_TO_STRING, ipmiConfiguration.toString());

		// When the userName is null, it's not appended to the result
		ipmiConfiguration.setUsername(null);
		Assertions.assertEquals(Constants.IPMI, ipmiConfiguration.toString());
	}
}
