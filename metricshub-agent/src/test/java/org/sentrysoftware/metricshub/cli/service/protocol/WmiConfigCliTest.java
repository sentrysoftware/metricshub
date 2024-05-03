package org.sentrysoftware.metricshub.cli.service.protocol;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.mockito.Mockito.mockStatic;

import java.util.List;
import org.junit.jupiter.api.Test;
import org.mockito.MockedStatic;
import org.sentrysoftware.metricshub.agent.extension.WmiTestConfiguration;
import org.sentrysoftware.metricshub.agent.extension.WmiTestExtension;
import org.sentrysoftware.metricshub.cli.service.CliExtensionManager;
import org.sentrysoftware.metricshub.engine.extension.ExtensionManager;

class WmiConfigCliTest {

	@Test
	void testToProtocol() throws Exception {
		// Create a WmiConfigCli instance and set its data
		final WmiConfigCli wmiConfigCli = new WmiConfigCli();
		final String username = "username";
		wmiConfigCli.setUsername(username);
		final char[] password = "pass".toCharArray();
		wmiConfigCli.setPassword(password);
		final String namespace = "namespace";
		wmiConfigCli.setNamespace(namespace);
		final String timeout = "120";
		wmiConfigCli.setTimeout(timeout);

		try (MockedStatic<CliExtensionManager> cliExtensionManagerMock = mockStatic(CliExtensionManager.class)) {
			// Initialize the extension manager required by the agent context
			final ExtensionManager extensionManager = ExtensionManager
				.builder()
				.withProtocolExtensions(List.of(new WmiTestExtension()))
				.build();

			cliExtensionManagerMock
				.when(() -> CliExtensionManager.getExtensionManagerSingleton())
				.thenReturn(extensionManager);

			// Create an WmiTestConfiguration and call method toProtocol
			WmiTestConfiguration wmiConfiguration = (WmiTestConfiguration) wmiConfigCli.toProtocol(null, null);

			final WmiTestConfiguration expected = WmiTestConfiguration
				.builder()
				.username(username)
				.password(password)
				.timeout(Long.valueOf(timeout))
				.namespace(namespace)
				.build();
			// Check the resulting WMI configuration
			assertEquals(expected, wmiConfiguration);

			// Check null password and null username
			wmiConfigCli.setPassword(null);
			wmiConfigCli.setUsername(null);
			wmiConfiguration = (WmiTestConfiguration) wmiConfigCli.toProtocol(null, null);

			assertNull(wmiConfiguration.getPassword());
			assertNull(wmiConfiguration.getUsername());
		}
	}
}
