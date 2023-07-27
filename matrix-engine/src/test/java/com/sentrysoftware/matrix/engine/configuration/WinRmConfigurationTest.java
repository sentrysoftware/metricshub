package com.sentrysoftware.matrix.engine.configuration;

import org.junit.Test;

import java.util.ArrayList;

import static com.sentrysoftware.matrix.constants.Constants.PASSWORD;
import static com.sentrysoftware.matrix.constants.Constants.USERNAME;
import static com.sentrysoftware.matrix.constants.Constants.WINRM;
import static com.sentrysoftware.matrix.constants.Constants.WINRM_CONFIGURATION_TO_STRING;
import static com.sentrysoftware.matrix.constants.Constants.WINRM_NAMESPACE;
import static org.junit.jupiter.api.Assertions.assertEquals;

/**
 * Test of {@link WinRmConfiguration}
 */
public class WinRmConfigurationTest {

	@Test
	public void testToString() {
		final WinRmConfiguration winRmConfiguration = new WinRmConfiguration();

		// When the userName is NOT null, it's appended to the result
		winRmConfiguration.setUsername(USERNAME);
		winRmConfiguration.setPassword(PASSWORD.toCharArray());
		winRmConfiguration.setAuthentications(new ArrayList<>());
		winRmConfiguration.setNamespace(WINRM_NAMESPACE);
		assertEquals(WINRM_CONFIGURATION_TO_STRING, winRmConfiguration.toString());

		// When the userName is null, it's not appended to the result
		winRmConfiguration.setUsername(null);
		assertEquals(WINRM, winRmConfiguration.toString());
	}
}
