package com.sentrysoftware.matrix.engine.configuration;

import org.junit.Test;

import static com.sentrysoftware.matrix.constants.Constants.PASSWORD;
import static com.sentrysoftware.matrix.constants.Constants.SSH;
import static com.sentrysoftware.matrix.constants.Constants.SSH_CONFIGURATION_TIMEOUT;
import static com.sentrysoftware.matrix.constants.Constants.SSH_CONFIGURATION_TO_STRING;
import static com.sentrysoftware.matrix.constants.Constants.SSH_SUDO_COMMAND;
import static com.sentrysoftware.matrix.constants.Constants.USERNAME;
import static org.junit.jupiter.api.Assertions.assertEquals;

/**
 * Test of {@link SshConfiguration}
 */
public class SshConfigurationTest {

	@Test
	public void testBuilder() {
		final SshConfiguration sshConfiguration = SshConfiguration
			.sshConfigurationBuilder()
			.username(USERNAME)
			.password(PASSWORD.toCharArray())
			.sudoCommand(SSH_SUDO_COMMAND)
			.timeout(SSH_CONFIGURATION_TIMEOUT)
			.useSudo(true)
			.build();
		assertEquals(USERNAME, sshConfiguration.getUsername());
		assertEquals(PASSWORD, new String(sshConfiguration.getPassword()));
		assertEquals(SSH_SUDO_COMMAND, sshConfiguration.getSudoCommand());
		assertEquals(SSH_CONFIGURATION_TIMEOUT, sshConfiguration.getTimeout());
		assertEquals(true, sshConfiguration.isUseSudo());
	}

	@Test
	public void testToString() {
		final SshConfiguration sshConfiguration = new SshConfiguration();

		// When the userName is NOT null, it's appended to the result
		sshConfiguration.setUsername(USERNAME);
		sshConfiguration.setPassword(PASSWORD.toCharArray());
		sshConfiguration.setTimeout(SSH_CONFIGURATION_TIMEOUT);
		sshConfiguration.setPrivateKey(null);
		sshConfiguration.setSudoCommand("");
		assertEquals(SSH_CONFIGURATION_TO_STRING, sshConfiguration.toString());

		// When the userName is null, it's not appended to the result
		sshConfiguration.setUsername(null);
		assertEquals(SSH, sshConfiguration.toString());
	}
}
