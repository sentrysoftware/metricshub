package org.sentrysoftware.metricshub.engine.configuration;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.sentrysoftware.metricshub.engine.constants.Constants.BMC_KEY;
import static org.sentrysoftware.metricshub.engine.constants.Constants.IMPI_CONFIGURATION_TO_STRING;
import static org.sentrysoftware.metricshub.engine.constants.Constants.IPMI;
import static org.sentrysoftware.metricshub.engine.constants.Constants.PASSWORD;
import static org.sentrysoftware.metricshub.engine.constants.Constants.USERNAME;

import org.junit.jupiter.api.Test;

/**
 * Test of {@link IpmiConfiguration}
 */
class IpmiConfigurationTest {

	@Test
	void testToString() {
		final IpmiConfiguration ipmiConfiguration = new IpmiConfiguration();

		// When the userName is NOT null, it's appended to the result
		ipmiConfiguration.setUsername(USERNAME);
		ipmiConfiguration.setPassword(PASSWORD.toCharArray());
		ipmiConfiguration.setBmcKey(BMC_KEY);
		ipmiConfiguration.setSkipAuth(false);
		assertEquals(IMPI_CONFIGURATION_TO_STRING, ipmiConfiguration.toString());

		// When the userName is null, it's not appended to the result
		ipmiConfiguration.setUsername(null);
		assertEquals(IPMI, ipmiConfiguration.toString());
	}
}
