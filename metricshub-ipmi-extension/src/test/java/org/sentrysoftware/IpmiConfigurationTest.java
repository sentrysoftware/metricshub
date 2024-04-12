package org.sentrysoftware;

import static org.junit.jupiter.api.Assertions.assertEquals;

import org.junit.jupiter.api.Test;
import org.sentrysoftware.metricshub.extension.ipmi.IpmiConfiguration;

/**
 * Test of {@link IpmiConfiguration}
 */
class IpmiConfigurationTest {

	private static final byte[] BMC_KEY = new byte[] { 0x06, 0x66 };
	private static final String USERNAME = "testUser";
	private static final String PASSWORD = "testPassword";
	public static final String IPMI = "IPMI";
	public static final String IPMI_CONFIGURATION_TO_STRING = "IPMI as testUser";

	@Test
	void testToString() {
		final IpmiConfiguration ipmiConfiguration = new IpmiConfiguration();

		// When the userName is NOT null, it's appended to the result
		ipmiConfiguration.setUsername(USERNAME);
		ipmiConfiguration.setPassword(PASSWORD.toCharArray());
		ipmiConfiguration.setBmcKey(BMC_KEY);
		ipmiConfiguration.setSkipAuth(false);
		assertEquals(IPMI_CONFIGURATION_TO_STRING, ipmiConfiguration.toString());

		// When the userName is null, it's not appended to the result
		ipmiConfiguration.setUsername(null);
		assertEquals(IPMI, ipmiConfiguration.toString());
	}
}
