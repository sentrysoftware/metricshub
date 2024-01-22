package org.sentrysoftware.metricshub.engine.configuration;

import static org.junit.jupiter.api.Assertions.assertEquals;

import java.util.ArrayList;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.sentrysoftware.metricshub.engine.constants.Constants;

/**
 * Test of {@link WinRmConfiguration}
 */
class WinRmConfigurationTest {

	@Test
	void testToString() {
		final WinRmConfiguration winRmConfiguration = new WinRmConfiguration();

		// When the userName is NOT null, it's appended to the result
		winRmConfiguration.setUsername(Constants.USERNAME);
		winRmConfiguration.setPassword(Constants.PASSWORD.toCharArray());
		winRmConfiguration.setAuthentications(new ArrayList<>());
		winRmConfiguration.setNamespace(Constants.WINRM_NAMESPACE);
		Assertions.assertEquals(Constants.WINRM_CONFIGURATION_TO_STRING, winRmConfiguration.toString());

		// When the userName is null, it's not appended to the result
		winRmConfiguration.setUsername(null);
		Assertions.assertEquals(Constants.WINRM, winRmConfiguration.toString());
	}
}
