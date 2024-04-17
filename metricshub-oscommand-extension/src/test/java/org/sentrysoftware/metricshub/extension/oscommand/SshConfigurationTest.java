package org.sentrysoftware.metricshub.extension.oscommand;

import static org.junit.jupiter.api.Assertions.assertEquals;

import org.junit.jupiter.api.Test;

/**
 * Test of {@link SshConfiguration}
 */
class SshConfigurationTest {

	public static final String USERNAME = "testUser";
	public static final String PASSWORD = "testPassword";
	public static final String SSH_SUDO_COMMAND = "sudo pwd";
	public static final String SSH_CONFIGURATION_TO_STRING = "SSH as testUser";
	public static final String SSH = "SSH";
	public static final Long SSH_CONFIGURATION_TIMEOUT = 50L;

	@Test
	void testBuilder() {
		final SshConfiguration sshConfiguration = SshConfiguration
			.builder()
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
	void testToString() {
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
