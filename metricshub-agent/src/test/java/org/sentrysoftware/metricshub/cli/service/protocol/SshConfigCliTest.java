package org.sentrysoftware.metricshub.cli.service.protocol;

import static org.junit.jupiter.api.Assertions.assertEquals;

import java.io.File;
import java.util.Set;
import org.junit.jupiter.api.Test;
import org.sentrysoftware.metricshub.engine.configuration.SshConfiguration;

class SshConfigCliTest {

	@Test
	void testToProtocol() {
		SshConfigCli sshConfigCli = new SshConfigCli();
		final char[] password = "value".toCharArray();
		final String username = "username";
		final int timeout = 120;
		final File privateKey = new File("privateKey");
		final String sudoCommand = "sudoCommand";
		final Set<String> useSudoCommands = Set.of("command1", "command2");

		final SshConfiguration sshConfiguration = SshConfiguration
			.sshConfigurationBuilder()
			.username(username)
			.password(password)
			.timeout(120L)
			.privateKey(privateKey)
			.sudoCommand(sudoCommand)
			.useSudoCommands(useSudoCommands)
			.useSudo(true)
			.build();

		sshConfigCli.setPassword(password);
		sshConfigCli.setUsername(username);
		sshConfigCli.setTimeout(timeout);
		sshConfigCli.setPrivateKey(privateKey);
		sshConfigCli.setSudoCommand(sudoCommand);
		sshConfigCli.setUseSudoCommands(useSudoCommands);

		assertEquals(sshConfiguration, sshConfigCli.toProtocol(null, null));

		sshConfigCli = new SshConfigCli();
		sshConfigCli.setTimeout(timeout);
		sshConfigCli.setPrivateKey(privateKey);
		sshConfigCli.setSudoCommand(sudoCommand);
		sshConfigCli.setUseSudoCommands(useSudoCommands);

		assertEquals(sshConfiguration, sshConfigCli.toProtocol(username, password));
	}
}
