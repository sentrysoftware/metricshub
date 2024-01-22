package com.sentrysoftware.metricshub.engine.configuration;

import static com.sentrysoftware.metricshub.engine.constants.Constants.HTTPS_WITH_PORT;
import static com.sentrysoftware.metricshub.engine.constants.Constants.HTTP_CONFIGURATION_TO_STRING;
import static com.sentrysoftware.metricshub.engine.constants.Constants.PASSWORD;
import static com.sentrysoftware.metricshub.engine.constants.Constants.USERNAME;
import static org.junit.jupiter.api.Assertions.assertEquals;

import org.junit.jupiter.api.Test;

/**
 * Test of {@link HttpConfiguration}
 */
class HttpConfigurationTest {

	@Test
	void testToString() {
		final HttpConfiguration httpsConfiguration = new HttpConfiguration();

		// When the userName is NOT null, it's appended to the result
		httpsConfiguration.setUsername(USERNAME);
		httpsConfiguration.setPassword(PASSWORD.toCharArray());
		assertEquals(HTTP_CONFIGURATION_TO_STRING, httpsConfiguration.toString());

		// When the userName is null, it's not appended to the result
		httpsConfiguration.setUsername(null);
		assertEquals(HTTPS_WITH_PORT, httpsConfiguration.toString());
	}
}
