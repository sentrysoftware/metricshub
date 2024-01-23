package org.sentrysoftware.metricshub.engine.configuration;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.sentrysoftware.metricshub.engine.constants.Constants.PASSWORD;
import static org.sentrysoftware.metricshub.engine.constants.Constants.USERNAME;
import static org.sentrysoftware.metricshub.engine.constants.Constants.WBEM_CONFIGURATION_TO_STRING;
import static org.sentrysoftware.metricshub.engine.constants.Constants.WBEM_HTTPS;
import static org.sentrysoftware.metricshub.engine.constants.Constants.WBEM_NAMESPACE;
import static org.sentrysoftware.metricshub.engine.constants.Constants.WBEM_VCENTER;

import org.junit.jupiter.api.Test;

/**
 * Test of {@link WbemConfiguration}
 */
class WbemConfigurationTest {

	@Test
	void testToString() {
		final WbemConfiguration wbemConfiguration = new WbemConfiguration();

		// When the userName is NOT null, it's appended to the result
		wbemConfiguration.setUsername(USERNAME);
		wbemConfiguration.setPassword(PASSWORD.toCharArray());
		wbemConfiguration.setNamespace(WBEM_NAMESPACE);
		wbemConfiguration.setVCenter(WBEM_VCENTER);
		assertEquals(WBEM_CONFIGURATION_TO_STRING, wbemConfiguration.toString());

		// When the userName is null, it's not appended to the result
		wbemConfiguration.setUsername(null);
		assertEquals(WBEM_HTTPS, wbemConfiguration.toString());
	}
}
