package org.sentrysoftware.metricshub.cli.service.protocol;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.mockito.Mockito.mockStatic;

import java.util.List;
import org.junit.jupiter.api.Test;
import org.mockito.MockedStatic;
import org.sentrysoftware.metricshub.agent.extension.SnmpTestConfiguration;
import org.sentrysoftware.metricshub.agent.extension.SnmpTestExtension;
import org.sentrysoftware.metricshub.cli.service.CliExtensionManager;
import org.sentrysoftware.metricshub.engine.common.exception.InvalidConfigurationException;
import org.sentrysoftware.metricshub.engine.extension.ExtensionManager;

class SnmpConfigCliTest {

	@Test
	void testToProtocol() throws InvalidConfigurationException {
		final String password = "pwd";

		// Create a SnmpConfigCli instance and set its data
		final SnmpConfigCli snmpConfigCli = new SnmpConfigCli();
		snmpConfigCli.setSnmpVersion("v1");
		snmpConfigCli.setPort(80);
		snmpConfigCli.setCommunity("community");
		snmpConfigCli.setTimeout("120");

		try (MockedStatic<CliExtensionManager> CliExtensionManagerMock = mockStatic(CliExtensionManager.class)) {
			// Initialize the extension manager required by the agent context
			final ExtensionManager extensionManager = ExtensionManager
				.builder()
				.withProtocolExtensions(List.of(new SnmpTestExtension()))
				.build();

			CliExtensionManagerMock
				.when(() -> CliExtensionManager.getExtensionManagerSingleton())
				.thenReturn(extensionManager);

			// Create an SnmpTestConfiguration and call method toProtocol
			final SnmpTestConfiguration snmpConfiguration = (SnmpTestConfiguration) snmpConfigCli.toProtocol(
				"user1",
				password.toCharArray()
			);

			// Check the resulting snmp configuration
			assertNotNull(snmpConfiguration);
			assertEquals(SnmpTestConfiguration.SnmpVersion.V1, snmpConfiguration.getVersion());
			assertEquals("SNMP v1 (community)", snmpConfiguration.toString());
		}
	}
}
