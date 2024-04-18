package org.sentrysoftware.metricshub.cli.service.protocol;

import org.junit.jupiter.api.Test;
import org.mockito.Mock;
import org.mockito.MockedStatic;
import org.sentrysoftware.metricshub.agent.extension.IpmiTestConfiguration;
import org.sentrysoftware.metricshub.agent.extension.IpmiTestExtension;
import org.sentrysoftware.metricshub.cli.service.CliExtensionManager;
import org.sentrysoftware.metricshub.engine.common.exception.InvalidConfigurationException;
import org.sentrysoftware.metricshub.engine.extension.ExtensionManager;

import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.mockito.Mockito.mockStatic;

class IpmiConfigCliTest {

	@Test
	void testToProtocol() throws InvalidConfigurationException {
		IpmiConfigCli ipmiConfigCli = new IpmiConfigCli();
		final char[] password = "value".toCharArray();
		final String username = "username";
		final String timeout = "120";

		ipmiConfigCli.setPassword(password);
		ipmiConfigCli.setUsername(username);
		ipmiConfigCli.setTimeout(timeout);

		try (MockedStatic<CliExtensionManager> cliExtensionManagerMock = mockStatic(CliExtensionManager.class)) {
			// Initialize the extension manager required by the agent context
			final ExtensionManager extensionManager = ExtensionManager
				.builder()
				.withProtocolExtensions(List.of(new IpmiTestExtension()))
				.build();

			cliExtensionManagerMock
				.when(() -> CliExtensionManager.getExtensionManagerSingleton())
				.thenReturn(extensionManager);

			// Create an IpmiTestConfiguration and call method toProtocol
			final IpmiTestConfiguration ipmiConfiguration = (IpmiTestConfiguration) ipmiConfigCli.toProtocol(
				username,
				password
			);

			assertNotNull(ipmiConfiguration);

			final IpmiTestConfiguration ipmiConfigurationExpected = IpmiTestConfiguration
				.builder()
				.username(username)
				.password(password)
				.build();

			assertEquals(ipmiConfigurationExpected, ipmiConfigCli.toProtocol(username, password));
		}
	}
}
