package org.sentrysoftware.metricshub.cli.service.protocol;

import static org.junit.jupiter.api.Assertions.assertArrayEquals;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.mockito.Mockito.mockStatic;

import java.util.List;
import org.junit.jupiter.api.Test;
import org.mockito.MockedStatic;
import org.sentrysoftware.metricshub.agent.extension.Snmpv3TestConfiguration;
import org.sentrysoftware.metricshub.agent.extension.Snmpv3TestExtension;
import org.sentrysoftware.metricshub.cli.service.CliExtensionManager;
import org.sentrysoftware.metricshub.engine.extension.ExtensionManager;

class Snmpv3ConfigCliTest {

	@Test
	void testToProtocol() throws Exception {
		final Snmpv3ConfigCli snmpv3ConfigCli = new Snmpv3ConfigCli();
		snmpv3ConfigCli.setCommunity("community");
		snmpv3ConfigCli.setPrivacy("AES");
		snmpv3ConfigCli.setPrivacyPassword("passwordPrivacy");
		snmpv3ConfigCli.setAuthType("SHA");
		snmpv3ConfigCli.setUsername("testUser");
		snmpv3ConfigCli.setPassword("testPassword");
		snmpv3ConfigCli.setContextName("testContext");
		snmpv3ConfigCli.setTimeout("60");
		snmpv3ConfigCli.setPort(161);

		try (MockedStatic<CliExtensionManager> cliExtensionManagerMock = mockStatic(CliExtensionManager.class)) {
			// Initialize the extension manager required by the agent context
			final ExtensionManager extensionManager = ExtensionManager
				.builder()
				.withProtocolExtensions(List.of(new Snmpv3TestExtension()))
				.build();

			cliExtensionManagerMock
				.when(() -> CliExtensionManager.getExtensionManagerSingleton())
				.thenReturn(extensionManager);

			// Create an Snmpv3TestConfiguration and call method toProtocol
			final Snmpv3TestConfiguration snmpv3Configuration = (Snmpv3TestConfiguration) snmpv3ConfigCli.toProtocol(
				"user1",
				"password".toCharArray()
			);

			assertNotNull(snmpv3Configuration);
			assertEquals(3, snmpv3Configuration.getIntVersion());
			assertEquals("SNMP V3 (community)", snmpv3Configuration.toString());
			assertEquals(Snmpv3TestConfiguration.Privacy.AES, snmpv3Configuration.getPrivacy());
			assertArrayEquals("passwordPrivacy".toCharArray(), snmpv3Configuration.getPrivacyPassword());
			assertEquals(Snmpv3TestConfiguration.AuthType.SHA, snmpv3Configuration.getAuthType());
			assertEquals("testUser", snmpv3Configuration.getUsername());
			assertArrayEquals("testPassword".toCharArray(), snmpv3Configuration.getPassword());
			assertEquals("testContext", snmpv3Configuration.getContextName());
		}
	}
}
