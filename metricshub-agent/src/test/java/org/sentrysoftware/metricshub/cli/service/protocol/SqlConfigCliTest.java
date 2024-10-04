package org.sentrysoftware.metricshub.cli.service.protocol;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.mockito.Mockito.mockStatic;

import java.util.List;
import org.junit.jupiter.api.Test;
import org.mockito.MockedStatic;
import org.sentrysoftware.metricshub.cli.service.CliExtensionManager;
import org.sentrysoftware.metricshub.engine.extension.ExtensionManager;
import org.sentrysoftware.metricshub.extension.sql.SqlConfiguration;
import org.sentrysoftware.metricshub.extension.sql.SqlExtension;

class SqlConfigCliTest {

	@Test
	void testToProtocol() throws Exception {
		final SqlConfigCli sqlConfigCli = new SqlConfigCli();
		sqlConfigCli.setUrl("jdbc:mysql://localhost:3306/mydatabase".toCharArray());
		sqlConfigCli.setUsername("testUser");
		final char[] password = "testPassword".toCharArray();
		sqlConfigCli.setPassword(password);
		sqlConfigCli.setTimeout(60);
		sqlConfigCli.setPort(3306);
		sqlConfigCli.setDatabase("mydatabase");
		sqlConfigCli.setType("MySQL");

		try (MockedStatic<CliExtensionManager> cliExtensionManagerMock = mockStatic(CliExtensionManager.class)) {
			// Initialize the extension manager with SQL protocol extension
			final ExtensionManager extensionManager = ExtensionManager
				.builder()
				.withProtocolExtensions(List.of(new SqlExtension()))
				.build();

			cliExtensionManagerMock
				.when(() -> CliExtensionManager.getExtensionManagerSingleton())
				.thenReturn(extensionManager);

			// Call the toProtocol method
			final SqlConfiguration sqlConfiguration = (SqlConfiguration) sqlConfigCli.toProtocol(
				"defaultUser",
				"defaultPassword".toCharArray()
			);

			// Verify the configuration returned by toProtocol
			assertNotNull(sqlConfiguration);
			assertEquals("jdbc:mysql://localhost:3306/mydatabase", String.valueOf(sqlConfiguration.getUrl()));
			assertEquals("testUser", sqlConfiguration.getUsername());
			assertEquals(60, sqlConfiguration.getTimeout());
			assertEquals(3306, sqlConfiguration.getPort());
			assertEquals("mydatabase", sqlConfiguration.getDatabase());
			assertEquals("MySQL", sqlConfiguration.getType());
			assertEquals("testPassword", String.valueOf(sqlConfiguration.getPassword()));
		}
	}
}
