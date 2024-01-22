package org.sentrysoftware.metricshub.engine.configuration;

import static org.junit.jupiter.api.Assertions.assertEquals;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.sentrysoftware.metricshub.engine.constants.Constants;

/**
 * Test of {@link HttpConfiguration}
 */
class HttpConfigurationTest {

	@Test
	void testToString() {
		final HttpConfiguration httpsConfiguration = new HttpConfiguration();

		// When the userName is NOT null, it's appended to the result
		httpsConfiguration.setUsername(Constants.USERNAME);
		httpsConfiguration.setPassword(Constants.PASSWORD.toCharArray());
		Assertions.assertEquals(Constants.HTTP_CONFIGURATION_TO_STRING, httpsConfiguration.toString());

		// When the userName is null, it's not appended to the result
		httpsConfiguration.setUsername(null);
		Assertions.assertEquals(Constants.HTTPS_WITH_PORT, httpsConfiguration.toString());
	}
}
