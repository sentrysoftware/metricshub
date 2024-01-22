package org.sentrysoftware.metricshub.engine.configuration;

import static org.junit.jupiter.api.Assertions.assertEquals;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.sentrysoftware.metricshub.engine.constants.Constants;

/**
 * Test of {@link SshConfiguration}
 */
class SshConfigurationTest {

	@Test
	void testBuilder() {
		final SshConfiguration sshConfiguration = SshConfiguration
			.sshConfigurationBuilder()
			.username(Constants.USERNAME)
			.password(Constants.PASSWORD.toCharArray())
			.sudoCommand(Constants.SSH_SUDO_COMMAND)
			.timeout(Constants.SSH_CONFIGURATION_TIMEOUT)
			.useSudo(true)
			.build();
		Assertions.assertEquals(Constants.USERNAME, sshConfiguration.getUsername());
		Assertions.assertEquals(Constants.PASSWORD, new String(sshConfiguration.getPassword()));
		Assertions.assertEquals(Constants.SSH_SUDO_COMMAND, sshConfiguration.getSudoCommand());
		Assertions.assertEquals(Constants.SSH_CONFIGURATION_TIMEOUT, sshConfiguration.getTimeout());
		assertEquals(true, sshConfiguration.isUseSudo());
	}

	@Test
	void testToString() {
		final SshConfiguration sshConfiguration = new SshConfiguration();

		// When the userName is NOT null, it's appended to the result
		sshConfiguration.setUsername(Constants.USERNAME);
		sshConfiguration.setPassword(Constants.PASSWORD.toCharArray());
		sshConfiguration.setTimeout(Constants.SSH_CONFIGURATION_TIMEOUT);
		sshConfiguration.setPrivateKey(null);
		sshConfiguration.setSudoCommand("");
		Assertions.assertEquals(Constants.SSH_CONFIGURATION_TO_STRING, sshConfiguration.toString());

		// When the userName is null, it's not appended to the result
		sshConfiguration.setUsername(null);
		Assertions.assertEquals(Constants.SSH, sshConfiguration.toString());
	}
}
