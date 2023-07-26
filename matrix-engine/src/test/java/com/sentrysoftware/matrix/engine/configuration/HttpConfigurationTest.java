package com.sentrysoftware.matrix.engine.configuration;

import org.junit.Test;

import static com.sentrysoftware.matrix.constants.Constants.HTTP_CONFIGURATION_TO_STRING;
import static com.sentrysoftware.matrix.constants.Constants.PASSWORD;
import static com.sentrysoftware.matrix.constants.Constants.USERNAME;
import static org.junit.jupiter.api.Assertions.assertEquals;

public class HttpConfigurationTest {

	@Test
	public void testToString() {
		final HttpConfiguration httpConfiguration = new HttpConfiguration();
		httpConfiguration.setUsername(USERNAME);
		httpConfiguration.setPassword(PASSWORD.toCharArray());
		assertEquals(HTTP_CONFIGURATION_TO_STRING, httpConfiguration.toString());
	}
}
