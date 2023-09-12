package com.sentrysoftware.matrix.agent.helper;

import static com.sentrysoftware.matrix.agent.helper.AgentConstants.*;
import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.mockStatic;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.util.Optional;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.condition.EnabledOnOs;
import org.junit.jupiter.api.condition.OS;
import org.junit.jupiter.api.io.TempDir;
import org.mockito.MockedStatic;

import com.sentrysoftware.matrix.common.helpers.ResourceHelper;


class ConfigHelperTest {

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
					mockedConfigHelper.when(() -> ConfigHelper.getProgramDataConfigFile(anyString(), anyString())).thenCallRealMethod();
					mockedConfigHelper.when(() -> ConfigHelper.getSubPath(anyString())).thenCallRealMethod();
					mockedConfigHelper.when(() -> ConfigHelper.getSourceDirectory()).thenCallRealMethod();

					mockedResourceHelper
						.when(() -> ResourceHelper.findSourceDirectory(ConfigHelper.class))
						.thenAnswer((invocation) -> tempDir.resolve("matrix/app/jar").toFile());

					final Path configFileOnWindows = ConfigHelper.getProgramDataConfigFile("config", DEFAULT_CONFIG_FILENAME);

					final String expectedPath = "matrix\\app\\..\\config\\" + DEFAULT_CONFIG_FILENAME;

					assertNotNull(configFileOnWindows);
					assertTrue(
						() -> configFileOnWindows.endsWith("matrix\\app\\..\\config\\" + DEFAULT_CONFIG_FILENAME),
						String
							.format(
								"Found path %s. Expected path ends with %s.",
								configFileOnWindows.toString(),
								expectedPath
							)
					);
				}
		}

		// ProgramData valid
		{
			try (final MockedStatic<ConfigHelper> mockedConfigHelper = mockStatic(ConfigHelper.class)) {

					mockedConfigHelper
						.when(() -> ConfigHelper.getProgramDataPath())
						.thenReturn(Optional.of(tempDir.toString()));

					mockedConfigHelper.when(() -> ConfigHelper.createDirectories(any(Path.class))).thenCallRealMethod();
					mockedConfigHelper.when(() -> ConfigHelper.getProgramDataConfigFile(anyString(), anyString())).thenCallRealMethod();

					final Path configFileOnWindows = ConfigHelper.getProgramDataConfigFile("config", DEFAULT_CONFIG_FILENAME);

					final String expectedPath = "matrix\\config\\" + DEFAULT_CONFIG_FILENAME;

					assertNotNull(configFileOnWindows);
					assertTrue(
						() -> configFileOnWindows.endsWith(expectedPath),
						String
							.format(
								"Found path %s. Expected path ends with %s.",
								configFileOnWindows.toString(),
								expectedPath
							)
					);
			}
		}
	}

	@Test
	void testGetDefaultConfigFilePermission() throws IOException {

		try (final MockedStatic<ConfigHelper> mockedConfigHelper = mockStatic(ConfigHelper.class)) {

			// Build a config directory
			final Path configDir = Files.createDirectories(tempDir.resolve("matrix\\config").toAbsolutePath());

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
				.thenAnswer((invocation) -> Paths.get(configDir.toString(), DEFAULT_CONFIG_FILENAME));

			// Call real method when invoking getDefaultConfigFile
			mockedConfigHelper.when(() -> ConfigHelper.getDefaultConfigFile(anyString(), anyString(), anyString())).thenCallRealMethod();

			// Mock getSubPath as it will try to retrieve the example file deployed in production environment
			mockedConfigHelper.when(() -> ConfigHelper.getSubPath(anyString())).thenReturn(examplePath);

			// Call the real method
			File file = ConfigHelper.getDefaultConfigFile("config", DEFAULT_CONFIG_FILENAME, CONFIG_EXAMPLE_FILENAME);
			assertTrue(file.canWrite());

		}
	}
}
