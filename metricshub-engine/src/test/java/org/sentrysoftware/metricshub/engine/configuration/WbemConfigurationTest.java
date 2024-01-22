package org.sentrysoftware.metricshub.engine.configuration;

import static org.junit.jupiter.api.Assertions.assertEquals;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.sentrysoftware.metricshub.engine.constants.Constants;

/**
 * Test of {@link WbemConfiguration}
 */
class WbemConfigurationTest {

	@Test
	void testToString() {
		final WbemConfiguration wbemConfiguration = new WbemConfiguration();

		// When the userName is NOT null, it's appended to the result
		wbemConfiguration.setUsername(Constants.USERNAME);
		wbemConfiguration.setPassword(Constants.PASSWORD.toCharArray());
		wbemConfiguration.setNamespace(Constants.WBEM_NAMESPACE);
		wbemConfiguration.setVCenter(Constants.WBEM_VCENTER);
		Assertions.assertEquals(Constants.WBEM_CONFIGURATION_TO_STRING, wbemConfiguration.toString());

		// When the userName is null, it's not appended to the result
		wbemConfiguration.setUsername(null);
		Assertions.assertEquals(Constants.WBEM_HTTPS, wbemConfiguration.toString());
	}
}
