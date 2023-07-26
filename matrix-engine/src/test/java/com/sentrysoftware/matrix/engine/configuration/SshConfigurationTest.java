package com.sentrysoftware.matrix.engine.configuration;

import org.junit.Test;

import static com.sentrysoftware.matrix.constants.Constants.PASSWORD;
import static com.sentrysoftware.matrix.constants.Constants.SSH_CONFIGURATION_TIMEOUT;
import static com.sentrysoftware.matrix.constants.Constants.SSH_CONFIGURATION_TO_STRING;
import static com.sentrysoftware.matrix.constants.Constants.USERNAME;
import static org.junit.jupiter.api.Assertions.assertEquals;

public class SshConfigurationTest {

	@Test
	public void testToString() {
		final SshConfiguration sshConfiguration = new SshConfiguration();
		sshConfiguration.setUsername(USERNAME);
		sshConfiguration.setPassword(PASSWORD.toCharArray());
		sshConfiguration.setTimeout(SSH_CONFIGURATION_TIMEOUT);
		sshConfiguration.setPrivateKey(null);
		sshConfiguration.setSudoCommand("");
		assertEquals(SSH_CONFIGURATION_TO_STRING, sshConfiguration.toString());
	}
}
