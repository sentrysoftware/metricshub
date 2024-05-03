package org.sentrysoftware.metricshub.cli.service.protocol;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.mockito.Mockito.mockStatic;

import java.util.List;
import java.util.Set;
import org.junit.jupiter.api.Test;
import org.mockito.MockedStatic;
import org.sentrysoftware.metricshub.agent.extension.OsCommandTestExtension;
import org.sentrysoftware.metricshub.agent.extension.SshTestConfiguration;
import org.sentrysoftware.metricshub.cli.service.CliExtensionManager;
import org.sentrysoftware.metricshub.engine.extension.ExtensionManager;

class SshConfigCliTest {

	@Test
	void testToProtocol() throws Exception {
		SshConfigCli sshConfigCli = new SshConfigCli();
		final char[] password = "value".toCharArray();
		final String username = "username";

		sshConfigCli.setPassword(password);
		sshConfigCli.setUsername(username);
		sshConfigCli.setTimeout("120");
		sshConfigCli.setPrivateKey("privateKey");
		sshConfigCli.setSudoCommand("sudoCommand");
		sshConfigCli.setUseSudoCommands(Set.of("command1", "command2"));

		try (MockedStatic<CliExtensionManager> CliExtensionManagerMock = mockStatic(CliExtensionManager.class)) {
			// Initialize the extension manager required by the agent context
			final ExtensionManager extensionManager = ExtensionManager
				.builder()
				.withProtocolExtensions(List.of(new OsCommandTestExtension()))
				.build();

			CliExtensionManagerMock
				.when(() -> CliExtensionManager.getExtensionManagerSingleton())
				.thenReturn(extensionManager);

			// Create an SshTestConfiguration and call method toProtocol
			final SshTestConfiguration sshConfiguration = (SshTestConfiguration) sshConfigCli.toProtocol(username, password);

			// Check the resulting ssh configuration
			assertNotNull(sshConfiguration);
			assertEquals(username, sshConfiguration.getUsername());
			assertEquals(String.valueOf(password), String.valueOf(sshConfiguration.getPassword()));
		}
	}
}
