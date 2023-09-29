package com.sentrysoftware.matrix.agent.helper;

import static com.sentrysoftware.matrix.agent.helper.AgentConstants.CONFIG_EXAMPLE_FILENAME;
import static com.sentrysoftware.matrix.agent.helper.AgentConstants.DEFAULT_CONFIG_FILENAME;
import static com.sentrysoftware.matrix.agent.helper.TestConstants.SENTRY_PARIS_RESOURCE_GROUP_KEY;
import static com.sentrysoftware.matrix.agent.helper.TestConstants.SERVER_1_RESOURCE_GROUP_KEY;
import static com.sentrysoftware.matrix.agent.helper.TestConstants.TEST_CONFIG_FILE_PATH;
import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.mockStatic;

import com.sentrysoftware.matrix.agent.config.AgentConfig;
import com.sentrysoftware.matrix.agent.config.protocols.SnmpProtocolConfig;
import com.sentrysoftware.matrix.agent.context.AgentContext;
import com.sentrysoftware.matrix.common.helpers.JsonHelper;
import com.sentrysoftware.matrix.common.helpers.MatrixConstants;
import com.sentrysoftware.matrix.common.helpers.ResourceHelper;
import com.sentrysoftware.matrix.configuration.HostConfiguration;
import com.sentrysoftware.matrix.configuration.HttpConfiguration;
import com.sentrysoftware.matrix.configuration.SnmpConfiguration.SnmpVersion;
import com.sentrysoftware.matrix.connector.model.Connector;
import com.sentrysoftware.matrix.connector.model.ConnectorStore;
import com.sentrysoftware.matrix.telemetry.TelemetryManager;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.util.Collections;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.condition.EnabledOnOs;
import org.junit.jupiter.api.condition.OS;
import org.junit.jupiter.api.io.TempDir;
import org.mockito.MockedStatic;

class ConfigHelperTest {

	private static final String USERNAME_CONFIG_VALUE = "username";
	private static final String VCENTER_HOSTNAME = "vcenter";
	private static final String RESOURCE_KEY = "resource-test-key";
	private static final String PURE_STORAGE_REST_CONNECTOR_ID = "PureStorageREST";

	@TempDir
	static Path tempDir;

	@Test
	@EnabledOnOs(OS.WINDOWS)
	void testGetProgramDataConfigFile() {
		// ProgramData invalid
		{
			try (
				final MockedStatic<ConfigHelper> mockedConfigHelper = mockStatic(ConfigHelper.class);
				final MockedStatic<ResourceHelper> mockedResourceHelper = mockStatic(ResourceHelper.class)
			) {
				mockedConfigHelper.when(() -> ConfigHelper.getProgramDataPath()).thenReturn(Optional.empty());
				mockedConfigHelper
					.when(() -> ConfigHelper.getProgramDataConfigFile(anyString(), anyString()))
					.thenCallRealMethod();
				mockedConfigHelper.when(() -> ConfigHelper.getSubPath(anyString())).thenCallRealMethod();
				mockedConfigHelper.when(() -> ConfigHelper.getSourceDirectory()).thenCallRealMethod();

				mockedResourceHelper
					.when(() -> ResourceHelper.findSourceDirectory(ConfigHelper.class))
					.thenAnswer(invocation -> tempDir.resolve("metricshub/app/jar").toFile());

				final Path configFileOnWindows = ConfigHelper.getProgramDataConfigFile("config", DEFAULT_CONFIG_FILENAME);

				final String expectedPath = "metricshub\\app\\..\\config\\" + DEFAULT_CONFIG_FILENAME;

				assertNotNull(configFileOnWindows);
				assertTrue(
					() -> configFileOnWindows.endsWith("metricshub\\app\\..\\config\\" + DEFAULT_CONFIG_FILENAME),
					String.format("Found path %s. Expected path ends with %s.", configFileOnWindows.toString(), expectedPath)
				);
			}
		}

		// ProgramData valid
		{
			try (final MockedStatic<ConfigHelper> mockedConfigHelper = mockStatic(ConfigHelper.class)) {
				mockedConfigHelper.when(() -> ConfigHelper.getProgramDataPath()).thenReturn(Optional.of(tempDir.toString()));

				mockedConfigHelper.when(() -> ConfigHelper.createDirectories(any(Path.class))).thenCallRealMethod();
				mockedConfigHelper
					.when(() -> ConfigHelper.getProgramDataConfigFile(anyString(), anyString()))
					.thenCallRealMethod();

				final Path configFileOnWindows = ConfigHelper.getProgramDataConfigFile("config", DEFAULT_CONFIG_FILENAME);

				final String expectedPath = "metricshub\\config\\" + DEFAULT_CONFIG_FILENAME;

				assertNotNull(configFileOnWindows);
				assertTrue(
					() -> configFileOnWindows.endsWith(expectedPath),
					String.format("Found path %s. Expected path ends with %s.", configFileOnWindows.toString(), expectedPath)
				);
			}
		}
	}

	@Test
	void testGetDefaultConfigFilePermission() throws IOException {
		try (final MockedStatic<ConfigHelper> mockedConfigHelper = mockStatic(ConfigHelper.class)) {
			// Build a config directory
			final Path configDir = Files.createDirectories(tempDir.resolve("metricshub\\config").toAbsolutePath());

			// Create the example file
			final Path examplePath = Path.of(configDir + "\\" + CONFIG_EXAMPLE_FILENAME);
			Files.copy(
				Path.of("src", "test", "resources", "config", DEFAULT_CONFIG_FILENAME),
				examplePath,
				StandardCopyOption.REPLACE_EXISTING
			);

			// Mock the method which gets the real production config file
			mockedConfigHelper
				.when(() -> ConfigHelper.getDefaultConfigFilePath(anyString(), anyString()))
				.thenAnswer(invocation -> Paths.get(configDir.toString(), DEFAULT_CONFIG_FILENAME));

			// Call real method when invoking getDefaultConfigFile
			mockedConfigHelper
				.when(() -> ConfigHelper.getDefaultConfigFile(anyString(), anyString(), anyString()))
				.thenCallRealMethod();

			// Mock getSubPath as it will try to retrieve the example file deployed in production environment
			mockedConfigHelper.when(() -> ConfigHelper.getSubPath(anyString())).thenReturn(examplePath);

			// Call the real method
			File file = ConfigHelper.getDefaultConfigFile("config", DEFAULT_CONFIG_FILENAME, CONFIG_EXAMPLE_FILENAME);
			assertTrue(file.canWrite());
		}
	}

	@Test
	void testBuildTelemetryManagers() throws IOException {
		final File configFile = ConfigHelper.findConfigFile(TEST_CONFIG_FILE_PATH);

		final ConnectorStore connectorStore = new ConnectorStore(Path.of("src/test/resources"));
		final Connector connector = new Connector();
		connector.getOrCreateConnectorIdentity().setCompiledFilename(PURE_STORAGE_REST_CONNECTOR_ID);
		connectorStore.addOne(PURE_STORAGE_REST_CONNECTOR_ID, connector);

		final AgentConfig agentConfig = JsonHelper.deserialize(
			AgentContext.AGENT_CONFIG_OBJECT_MAPPER,
			new FileInputStream(configFile),
			AgentConfig.class
		);
		ConfigHelper.normalizeAgentConfiguration(agentConfig, connectorStore);

		final Map<String, Map<String, TelemetryManager>> telemetryManagers = ConfigHelper.buildTelemetryManagers(
			agentConfig,
			connectorStore
		);

		final Map<String, TelemetryManager> resourceGroupTelemetryManagers = telemetryManagers.get(
			SENTRY_PARIS_RESOURCE_GROUP_KEY
		);
		assertNotNull(resourceGroupTelemetryManagers);
		final TelemetryManager telemetryManager = resourceGroupTelemetryManagers.get(SERVER_1_RESOURCE_GROUP_KEY);
		assertNotNull(telemetryManager);
		final HostConfiguration hostConfiguration = telemetryManager.getHostConfiguration();
		assertEquals(
			agentConfig
				.getResourceGroups()
				.get(SENTRY_PARIS_RESOURCE_GROUP_KEY)
				.getResources()
				.get(SERVER_1_RESOURCE_GROUP_KEY)
				.getAttributes()
				.get(MatrixConstants.HOST_NAME),
			hostConfiguration.getHostname()
		);

		assertEquals(Set.of(PURE_STORAGE_REST_CONNECTOR_ID), hostConfiguration.getSelectedConnectors());
		assertEquals(Collections.emptySet(), hostConfiguration.getExcludedConnectors());
		assertNotNull(hostConfiguration.getConfigurations().get(HttpConfiguration.class));
	}

	@Test
	void testValidateSnmpInfo() {
		final char[] community = "public".toCharArray();
		final char[] emptyCommunity = new char[] {};

		{
			final SnmpProtocolConfig snmpProtocolConfig = SnmpProtocolConfig
				.builder()
				.community(emptyCommunity)
				.port(1234)
				.timeout(60L)
				.version(SnmpVersion.V1)
				.username(null)
				.build();

			assertThrows(IllegalStateException.class, () -> ConfigHelper.validateSnmpInfo(RESOURCE_KEY, snmpProtocolConfig));
		}

		{
			final SnmpProtocolConfig snmpProtocolConfig = SnmpProtocolConfig
				.builder()
				.community(null)
				.port(1234)
				.timeout(60L)
				.version(SnmpVersion.V1)
				.username(null)
				.build();

			assertThrows(IllegalStateException.class, () -> ConfigHelper.validateSnmpInfo(RESOURCE_KEY, snmpProtocolConfig));
		}

		{
			final SnmpProtocolConfig snmpProtocolConfig = SnmpProtocolConfig
				.builder()
				.community(community)
				.port(-1)
				.timeout(60L)
				.version(SnmpVersion.V1)
				.username(null)
				.build();

			assertThrows(IllegalStateException.class, () -> ConfigHelper.validateSnmpInfo(RESOURCE_KEY, snmpProtocolConfig));
		}

		{
			final SnmpProtocolConfig snmpProtocolConfig = SnmpProtocolConfig
				.builder()
				.community(community)
				.port(66666)
				.timeout(60L)
				.version(SnmpVersion.V1)
				.username(null)
				.build();

			assertThrows(IllegalStateException.class, () -> ConfigHelper.validateSnmpInfo(RESOURCE_KEY, snmpProtocolConfig));
		}

		{
			final SnmpProtocolConfig snmpProtocolConfig = SnmpProtocolConfig
				.builder()
				.community(community)
				.port(null)
				.timeout(60L)
				.version(SnmpVersion.V1)
				.username(null)
				.build();

			assertThrows(IllegalStateException.class, () -> ConfigHelper.validateSnmpInfo(RESOURCE_KEY, snmpProtocolConfig));
		}

		{
			final SnmpProtocolConfig snmpProtocolConfig = SnmpProtocolConfig
				.builder()
				.community(community)
				.port(1234)
				.timeout(-60L)
				.version(SnmpVersion.V1)
				.username(null)
				.build();

			assertThrows(IllegalStateException.class, () -> ConfigHelper.validateSnmpInfo(RESOURCE_KEY, snmpProtocolConfig));
		}

		{
			final SnmpProtocolConfig snmpProtocolConfig = SnmpProtocolConfig
				.builder()
				.community(community)
				.port(1234)
				.timeout(null)
				.version(SnmpVersion.V1)
				.username(null)
				.build();

			assertThrows(IllegalStateException.class, () -> ConfigHelper.validateSnmpInfo(RESOURCE_KEY, snmpProtocolConfig));
		}

		{
			final SnmpProtocolConfig snmpProtocolConfig = SnmpProtocolConfig
				.builder()
				.community(community)
				.port(1234)
				.timeout(60L)
				.version(SnmpVersion.V3_SHA)
				.username(null)
				.build();

			assertThrows(IllegalStateException.class, () -> ConfigHelper.validateSnmpInfo(RESOURCE_KEY, snmpProtocolConfig));
		}

		{
			final SnmpProtocolConfig snmpProtocolConfig = SnmpProtocolConfig
				.builder()
				.community(community)
				.port(1234)
				.timeout(60L)
				.version(SnmpVersion.V3_SHA)
				.username("")
				.build();

			assertThrows(IllegalStateException.class, () -> ConfigHelper.validateSnmpInfo(RESOURCE_KEY, snmpProtocolConfig));
		}

		{
			final SnmpProtocolConfig snmpProtocolConfig = SnmpProtocolConfig
				.builder()
				.community(community)
				.port(1234)
				.timeout(60L)
				.version(SnmpVersion.V3_SHA)
				.username(USERNAME_CONFIG_VALUE)
				.build();

			assertDoesNotThrow(() -> ConfigHelper.validateSnmpInfo(RESOURCE_KEY, snmpProtocolConfig));
		}

		{
			final SnmpProtocolConfig snmpProtocolConfig = SnmpProtocolConfig
				.builder()
				.community(community)
				.port(1234)
				.timeout(60L)
				.version(SnmpVersion.V3_NO_AUTH)
				.username(null)
				.build();

			assertDoesNotThrow(() -> ConfigHelper.validateSnmpInfo(RESOURCE_KEY, snmpProtocolConfig));
		}
	}

	@Test
	void testValidateIpmiInfo() {
		assertThrows(IllegalStateException.class, () -> ConfigHelper.validateIpmiInfo(RESOURCE_KEY, "", 60L));
		assertThrows(IllegalStateException.class, () -> ConfigHelper.validateIpmiInfo(RESOURCE_KEY, null, 60L));
		assertThrows(
			IllegalStateException.class,
			() -> ConfigHelper.validateIpmiInfo(RESOURCE_KEY, USERNAME_CONFIG_VALUE, -60L)
		);
		assertThrows(
			IllegalStateException.class,
			() -> ConfigHelper.validateIpmiInfo(RESOURCE_KEY, USERNAME_CONFIG_VALUE, null)
		);
		assertDoesNotThrow(() -> ConfigHelper.validateIpmiInfo(RESOURCE_KEY, USERNAME_CONFIG_VALUE, 60L));
	}

	@Test
	void testValidateSshInfo() {
		assertThrows(IllegalStateException.class, () -> ConfigHelper.validateSshInfo(RESOURCE_KEY, "", 60L));
		assertThrows(IllegalStateException.class, () -> ConfigHelper.validateSshInfo(RESOURCE_KEY, null, 60L));
		assertThrows(
			IllegalStateException.class,
			() -> ConfigHelper.validateSshInfo(RESOURCE_KEY, USERNAME_CONFIG_VALUE, -60L)
		);
		assertThrows(
			IllegalStateException.class,
			() -> ConfigHelper.validateSshInfo(RESOURCE_KEY, USERNAME_CONFIG_VALUE, null)
		);
		assertDoesNotThrow(() -> ConfigHelper.validateSshInfo(RESOURCE_KEY, USERNAME_CONFIG_VALUE, 60L));
	}

	@Test
	void testValidateWbemInfo() {
		assertThrows(
			IllegalStateException.class,
			() -> ConfigHelper.validateWbemInfo(RESOURCE_KEY, null, -60L, 1234, VCENTER_HOSTNAME)
		);
		assertThrows(
			IllegalStateException.class,
			() -> ConfigHelper.validateWbemInfo(RESOURCE_KEY, "", null, 1234, VCENTER_HOSTNAME)
		);
		assertThrows(
			IllegalStateException.class,
			() -> ConfigHelper.validateWbemInfo(RESOURCE_KEY, USERNAME_CONFIG_VALUE, -60L, 1234, VCENTER_HOSTNAME)
		);
		assertThrows(
			IllegalStateException.class,
			() -> ConfigHelper.validateWbemInfo(RESOURCE_KEY, USERNAME_CONFIG_VALUE, null, 1234, VCENTER_HOSTNAME)
		);
		assertThrows(
			IllegalStateException.class,
			() -> ConfigHelper.validateWbemInfo(RESOURCE_KEY, USERNAME_CONFIG_VALUE, 60L, -1, VCENTER_HOSTNAME)
		);
		assertThrows(
			IllegalStateException.class,
			() -> ConfigHelper.validateWbemInfo(RESOURCE_KEY, USERNAME_CONFIG_VALUE, 60L, null, VCENTER_HOSTNAME)
		);
		assertThrows(
			IllegalStateException.class,
			() -> ConfigHelper.validateWbemInfo(RESOURCE_KEY, USERNAME_CONFIG_VALUE, 60L, 66666, VCENTER_HOSTNAME)
		);
		assertThrows(
			IllegalStateException.class,
			() -> ConfigHelper.validateWbemInfo(RESOURCE_KEY, USERNAME_CONFIG_VALUE, 60L, null, "")
		);
		assertDoesNotThrow(() ->
			ConfigHelper.validateWbemInfo(RESOURCE_KEY, USERNAME_CONFIG_VALUE, 60L, 1234, VCENTER_HOSTNAME)
		);
		assertDoesNotThrow(() -> ConfigHelper.validateWbemInfo(RESOURCE_KEY, USERNAME_CONFIG_VALUE, 60L, 1234, null));
	}

	@Test
	void testValidateWmiInfo() {
		assertThrows(IllegalStateException.class, () -> ConfigHelper.validateWmiInfo(RESOURCE_KEY, -60L));
		assertThrows(IllegalStateException.class, () -> ConfigHelper.validateWmiInfo(RESOURCE_KEY, null));
		assertDoesNotThrow(() -> ConfigHelper.validateWmiInfo(RESOURCE_KEY, 60L));
	}

	@Test
	void testValidateHttpInfo() {
		assertThrows(IllegalStateException.class, () -> ConfigHelper.validateHttpInfo(RESOURCE_KEY, -60L, 1234));
		assertThrows(IllegalStateException.class, () -> ConfigHelper.validateHttpInfo(RESOURCE_KEY, null, 1234));
		assertThrows(IllegalStateException.class, () -> ConfigHelper.validateHttpInfo(RESOURCE_KEY, 60L, -1));
		assertThrows(IllegalStateException.class, () -> ConfigHelper.validateHttpInfo(RESOURCE_KEY, 60L, null));
		assertThrows(IllegalStateException.class, () -> ConfigHelper.validateHttpInfo(RESOURCE_KEY, 60L, 66666));
		assertDoesNotThrow(() -> ConfigHelper.validateHttpInfo(RESOURCE_KEY, 60L, 1234));
	}

	@Test
	void testValidateOsCommandInfo() {
		assertThrows(IllegalStateException.class, () -> ConfigHelper.validateOsCommandInfo(RESOURCE_KEY, -60L));
		assertThrows(IllegalStateException.class, () -> ConfigHelper.validateOsCommandInfo(RESOURCE_KEY, null));
		assertDoesNotThrow(() -> ConfigHelper.validateOsCommandInfo(RESOURCE_KEY, 60L));
	}

	@Test
	void testValidateWinRm() {
		assertThrows(
			IllegalStateException.class,
			() -> ConfigHelper.validateWinRmInfo(RESOURCE_KEY, 1234, -60L, USERNAME_CONFIG_VALUE)
		);
		assertThrows(
			IllegalStateException.class,
			() -> ConfigHelper.validateWinRmInfo(RESOURCE_KEY, 1234, null, USERNAME_CONFIG_VALUE)
		);
		assertThrows(
			IllegalStateException.class,
			() -> ConfigHelper.validateWinRmInfo(RESOURCE_KEY, null, 60L, USERNAME_CONFIG_VALUE)
		);
		assertThrows(
			IllegalStateException.class,
			() -> ConfigHelper.validateWinRmInfo(RESOURCE_KEY, -1234, 60L, USERNAME_CONFIG_VALUE)
		);
		assertThrows(IllegalStateException.class, () -> ConfigHelper.validateWinRmInfo(RESOURCE_KEY, 1234, 60L, null));
		assertThrows(IllegalStateException.class, () -> ConfigHelper.validateWinRmInfo(RESOURCE_KEY, 1234, 60L, ""));
		assertDoesNotThrow(() -> ConfigHelper.validateWinRmInfo(RESOURCE_KEY, 1234, 60L, USERNAME_CONFIG_VALUE));
	}
}
