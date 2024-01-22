package org.sentrysoftware.metricshub.engine.strategy.utils;

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.ArgumentMatchers.isNull;
import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.mockStatic;
import static org.mockito.Mockito.when;
import static org.sentrysoftware.metricshub.engine.strategy.utils.OsCommandHelper.TEMP_FILE_CREATOR;

import java.io.File;
import java.io.FilenameFilter;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.time.LocalDate;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.concurrent.Semaphore;
import java.util.concurrent.TimeoutException;
import java.util.function.Function;
import java.util.stream.Collectors;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.condition.EnabledOnOs;
import org.junit.jupiter.api.condition.OS;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.api.io.TempDir;
import org.mockito.MockedStatic;
import org.mockito.Mockito;
import org.mockito.junit.jupiter.MockitoExtension;
import org.sentrysoftware.metricshub.engine.client.ClientsExecutor;
import org.sentrysoftware.metricshub.engine.common.exception.ControlledSshException;
import org.sentrysoftware.metricshub.engine.common.exception.NoCredentialProvidedException;
import org.sentrysoftware.metricshub.engine.common.helpers.LocalOsHandler;
import org.sentrysoftware.metricshub.engine.configuration.HostConfiguration;
import org.sentrysoftware.metricshub.engine.configuration.OsCommandConfiguration;
import org.sentrysoftware.metricshub.engine.configuration.SshConfiguration;
import org.sentrysoftware.metricshub.engine.configuration.WmiConfiguration;
import org.sentrysoftware.metricshub.engine.connector.model.common.DeviceKind;
import org.sentrysoftware.metricshub.engine.connector.model.common.EmbeddedFile;
import org.sentrysoftware.metricshub.engine.constants.Constants;
import org.sentrysoftware.metricshub.engine.telemetry.SshSemaphoreFactory;
import org.sentrysoftware.metricshub.engine.telemetry.TelemetryManager;

@ExtendWith(MockitoExtension.class)
class OsCommandHelperTest {

	private static final Map<String, EmbeddedFile> EMPTY_EMBEDDED_FILE_MAP = Collections.emptyMap();
	private static Map<String, EmbeddedFile> commandLineEmbeddedFiles;

	@TempDir
	static File tempDir;

	static Function<String, File> jUnitTempFileCreator;

	/**
	 * Setup unit tests.
	 */
	@BeforeAll
	static void setup() {
		// Initialize temporary file creator for JUnit tests.
		jUnitTempFileCreator =
			extension -> {
				try {
					return File.createTempFile(Constants.EMBEDDED_TEMP_FILE_PREFIX, extension, tempDir);
				} catch (IOException e) {
					throw new OsCommandHelper.TempFileCreationException(e);
				}
			};
		commandLineEmbeddedFiles = new HashMap<>();
	}

	@BeforeEach
	void clearEmbeddedFiles() {
		commandLineEmbeddedFiles.clear();
	}

	/**
	 * Retrieve temporary embedded files using the {@link FilenameFilter}.
	 *
	 * @return Array of {@link File} instances
	 */
	private static File[] getTempEmbeddedFiles() {
		return tempDir.listFiles((directory, fileName) -> fileName.startsWith(Constants.EMBEDDED_TEMP_FILE_PREFIX));
	}

	/**
	 * Assert that temporary embedded files are removed correctly.
	 */
	private static void checkNoTempEmbeddedFileExist() {
		assertEquals(0, getTempEmbeddedFiles().length);
	}

	@Test
	void testCreateOsCommandEmbeddedFiles() throws Exception {
		checkNoTempEmbeddedFileExist();

		assertThrows(
			IllegalArgumentException.class,
			() -> OsCommandHelper.createOsCommandEmbeddedFiles(null, null, EMPTY_EMBEDDED_FILE_MAP, jUnitTempFileCreator)
		);

		// Embedded files are referenced in the command line but cannot be found
		assertThrows(
			IllegalStateException.class,
			() ->
				OsCommandHelper.createOsCommandEmbeddedFiles(
					Constants.EMBEDDED_FILE_1_COPY_COMMAND_LINE,
					null,
					EMPTY_EMBEDDED_FILE_MAP,
					jUnitTempFileCreator
				)
		);

		assertEquals(
			Collections.emptyMap(),
			OsCommandHelper.createOsCommandEmbeddedFiles(Constants.CMD, null, EMPTY_EMBEDDED_FILE_MAP, jUnitTempFileCreator)
		);

		// case embeddedFile not found
		commandLineEmbeddedFiles.put(Constants.EMBEDDED_FILE_1_REF, new EmbeddedFile());
		assertThrows(
			IllegalStateException.class,
			() ->
				OsCommandHelper.createOsCommandEmbeddedFiles(
					Constants.EMBEDDED_FILE_1_COPY_COMMAND_LINE,
					null,
					commandLineEmbeddedFiles,
					jUnitTempFileCreator
				)
		);

		// case embeddedFile content null
		assertThrows(
			IllegalStateException.class,
			() ->
				OsCommandHelper.createOsCommandEmbeddedFiles(
					Constants.EMBEDDED_FILE_1_COPY_COMMAND_LINE,
					null,
					commandLineEmbeddedFiles,
					jUnitTempFileCreator
				)
		);

		checkNoTempEmbeddedFileExist();

		// case IOException in temp file creation
		try (final MockedStatic<OsCommandHelper> mockedOsCommandHelper = mockStatic(OsCommandHelper.class)) {
			commandLineEmbeddedFiles.put(
				Constants.EMBEDDED_FILE_1_REF,
				new EmbeddedFile(Constants.ECHO_OS, Constants.BAT, Constants.EMBEDDED_FILE_1_REF)
			);
			commandLineEmbeddedFiles.put(
				Constants.EMBEDDED_FILE_2_REF,
				new EmbeddedFile(Constants.ECHO_HELLO_WORLD, null, Constants.EMBEDDED_FILE_2_REF)
			);

			mockedOsCommandHelper
				.when(() -> OsCommandHelper.createTempFileWithEmbeddedFileContent(any(EmbeddedFile.class), isNull(), any()))
				.thenThrow(IOException.class);

			mockedOsCommandHelper
				.when(() ->
					OsCommandHelper.createOsCommandEmbeddedFiles(
						Constants.EMBEDDED_FILE_1_COPY_COMMAND_LINE,
						null,
						commandLineEmbeddedFiles,
						jUnitTempFileCreator
					)
				)
				.thenCallRealMethod();

			assertThrows(
				IOException.class,
				() ->
					OsCommandHelper.createOsCommandEmbeddedFiles(
						Constants.EMBEDDED_FILE_1_COPY_COMMAND_LINE,
						null,
						commandLineEmbeddedFiles,
						jUnitTempFileCreator
					)
			);
		}

		// case OK
		{
			checkNoTempEmbeddedFileExist();

			commandLineEmbeddedFiles.put(
				Constants.EMBEDDED_FILE_2_REF,
				new EmbeddedFile(Constants.AWK_EMBEDDED_CONTENT_PERCENT_SUDO, null, Constants.EMBEDDED_FILE_2_REF)
			);

			final Map<String, File> embeddedTempFiles = OsCommandHelper.createOsCommandEmbeddedFiles(
				Constants.EMBEDDED_FILE_1_COPY_COMMAND_LINE,
				OsCommandConfiguration.builder().useSudo(true).useSudoCommands(Set.of(Constants.ARCCONF_PATH)).build(),
				commandLineEmbeddedFiles,
				jUnitTempFileCreator
			);

			assertEquals(2, embeddedTempFiles.size());

			{
				final File file = embeddedTempFiles.get(Constants.EMBEDDED_FILE_1_REF);
				assertNotNull(file);
				assertTrue(file.exists());
				assertTrue(file.getName().matches(Constants.EMBEDDED_TEMP_FILE_PREFIX + Constants.BAT_FILE_EXTENSION));
				Assertions.assertEquals(
					Constants.ECHO_OS,
					Files.readAllLines(Paths.get(file.getAbsolutePath())).stream().collect(Collectors.joining())
				);
				file.delete();
			}
			{
				final File file = embeddedTempFiles.get(Constants.EMBEDDED_FILE_2_REF);
				assertNotNull(file);
				assertTrue(file.exists());
				assertTrue(file.getName().matches(Constants.EMBEDDED_TEMP_FILE_PREFIX + "\\w+"));
				/*
				 * assertEquals(
-						expectedContent.replaceAll("[\r\n]", EMPTY),
-						Files.readAllLines(Paths.get(file.getAbsolutePath())).stream().collect(Collectors.joining()));
				 */
				Assertions.assertEquals(
					Constants.AWK_EMBEDDED_CONTENT_SUDO.replaceAll(Constants.END_OF_LINE_IN_BRACKETS, Constants.EMPTY),
					Files.readAllLines(Paths.get(file.getAbsolutePath())).stream().collect(Collectors.joining())
				);
				file.delete();
			}
		}
	}

	@Test
	void testReplaceSudo() {
		assertNull(OsCommandHelper.replaceSudo(null, null));
		assertNull(OsCommandHelper.replaceSudo(null, OsCommandConfiguration.builder().build()));

		assertEquals(Constants.EMPTY, OsCommandHelper.replaceSudo(Constants.EMPTY, null));
		assertEquals(
			Constants.EMPTY,
			OsCommandHelper.replaceSudo(Constants.EMPTY, OsCommandConfiguration.builder().build())
		);
		assertEquals(Constants.SINGLE_SPACE, OsCommandHelper.replaceSudo(Constants.SINGLE_SPACE, null));
		assertEquals(
			Constants.SINGLE_SPACE,
			OsCommandHelper.replaceSudo(Constants.SINGLE_SPACE, OsCommandConfiguration.builder().build())
		);

		assertEquals(Constants.TEXT, OsCommandHelper.replaceSudo(Constants.TEXT, null));
		assertEquals(Constants.TEXT, OsCommandHelper.replaceSudo(Constants.TEXT, OsCommandConfiguration.builder().build()));

		// Check replace sudo tag with empty string.
		assertEquals(Constants.SPACE_KEY, OsCommandHelper.replaceSudo(Constants.SUDO_KEY, null));
		assertEquals(
			Constants.SPACE_KEY,
			OsCommandHelper.replaceSudo(Constants.SUDO_KEY, OsCommandConfiguration.builder().build())
		);
		assertEquals(
			Constants.SPACE_KEY,
			OsCommandHelper.replaceSudo(Constants.SUDO_KEY, OsCommandConfiguration.builder().useSudo(true).build())
		);
		assertEquals(
			Constants.SPACE_KEY + Constants.END_OF_LINE + Constants.SPACE_KEY,
			OsCommandHelper.replaceSudo(
				Constants.SUDO_KEY + Constants.END_OF_LINE + Constants.SUDO_KEY,
				OsCommandConfiguration.builder().useSudo(true).build()
			)
		);

		assertEquals(
			Constants.SUDO_KEYWORD + Constants.SPACE_KEY,
			OsCommandHelper.replaceSudo(
				Constants.SUDO_KEY,
				OsCommandConfiguration.builder().useSudo(true).useSudoCommands(Set.of(Constants.KEY)).build()
			)
		);

		assertEquals(
			Constants.SUDO_KEY_RESULT,
			OsCommandHelper.replaceSudo(
				Constants.SUDO_KEY + Constants.END_OF_LINE + Constants.SUDO_KEY,
				OsCommandConfiguration.builder().useSudo(true).useSudoCommands(Set.of(Constants.KEY)).build()
			)
		);
	}

	@Test
	void testRunLocalCommandKO() throws Exception {
		assertThrows(IllegalArgumentException.class, () -> OsCommandHelper.runLocalCommand(null, 1, null));
		assertThrows(IllegalArgumentException.class, () -> OsCommandHelper.runLocalCommand(Constants.CMD, -1, null));
		assertThrows(IllegalArgumentException.class, () -> OsCommandHelper.runLocalCommand(Constants.CMD, 0, null));

		// case Process null Linux
		final Runtime runtime = mock(Runtime.class);
		try (
			final MockedStatic<LocalOsHandler> mockedLocalOSHandler = mockStatic(LocalOsHandler.class);
			final MockedStatic<Runtime> mockedRuntime = mockStatic(Runtime.class)
		) {
			mockedLocalOSHandler.when(LocalOsHandler::isWindows).thenReturn(false);
			mockedRuntime.when(Runtime::getRuntime).thenReturn(runtime);
			when(runtime.exec(Constants.CMD)).thenReturn(null);

			assertThrows(IllegalStateException.class, () -> OsCommandHelper.runLocalCommand(Constants.CMD, 1, null));
		}

		// case Process null Windows
		try (
			final MockedStatic<LocalOsHandler> mockedLocalOSHandler = mockStatic(LocalOsHandler.class);
			final MockedStatic<Runtime> mockedRuntime = mockStatic(Runtime.class)
		) {
			mockedLocalOSHandler.when(LocalOsHandler::isWindows).thenReturn(true);
			mockedRuntime.when(Runtime::getRuntime).thenReturn(runtime);
			when(runtime.exec(Constants.CMD_COMMAND)).thenReturn(null);

			assertThrows(IllegalStateException.class, () -> OsCommandHelper.runLocalCommand(Constants.CMD, 1, null));
		}
	}

	@Test
	@EnabledOnOs(OS.WINDOWS)
	void testRunLocalCommandWindowsTimeout() throws Exception {
		assertThrows(TimeoutException.class, () -> OsCommandHelper.runLocalCommand(Constants.PAUSE, 1, null));
	}

	@Test
	@EnabledOnOs(OS.LINUX)
	void testRunLocalCommandLinuxTimeout() throws Exception {
		assertThrows(TimeoutException.class, () -> OsCommandHelper.runLocalCommand(Constants.SLEEP_5, 1, null));
	}

	@Test
	@EnabledOnOs(OS.WINDOWS)
	void testRunLocalCommandWindows() throws Exception {
		assertEquals(Constants.TEST_RESULT, OsCommandHelper.runLocalCommand(Constants.ECHO_TEST_UPPER_CASE, 1, null));
	}

	@Test
	@EnabledOnOs(OS.LINUX)
	void testRunLocalCommandLinux() throws Exception {
		assertEquals(Constants.TEST_RESULT, OsCommandHelper.runLocalCommand(Constants.ECHO_TEST_LOWER_CASE, 1, null));
	}

	@Test
	void testRunSshCommand() throws Exception {
		final SshConfiguration sshConfiguration = mock(SshConfiguration.class);
		final int timeout = 1000;

		assertThrows(
			IllegalArgumentException.class,
			() -> OsCommandHelper.runSshCommand(null, Constants.HOST, sshConfiguration, timeout, null, null)
		);
		assertThrows(
			IllegalArgumentException.class,
			() -> OsCommandHelper.runSshCommand(Constants.CMD, null, sshConfiguration, timeout, null, null)
		);
		assertThrows(
			IllegalArgumentException.class,
			() -> OsCommandHelper.runSshCommand(Constants.CMD, Constants.HOST, null, timeout, null, null)
		);
		assertThrows(
			IllegalArgumentException.class,
			() -> OsCommandHelper.runSshCommand(Constants.CMD, Constants.HOST, sshConfiguration, -1, null, null)
		);
		assertThrows(
			IllegalArgumentException.class,
			() -> OsCommandHelper.runSshCommand(Constants.CMD, Constants.HOST, sshConfiguration, 0, null, null)
		);

		try (final MockedStatic<ClientsExecutor> mockedClientsExecutor = mockStatic(ClientsExecutor.class)) {
			when(sshConfiguration.getUsername()).thenReturn(Constants.USERNAME);
			when(sshConfiguration.getPassword()).thenReturn(Constants.PASSWORD.toCharArray());

			mockedClientsExecutor
				.when(() ->
					ClientsExecutor.runRemoteSshCommand(
						Constants.HOST,
						Constants.USERNAME,
						Constants.PASSWORD.toCharArray(),
						null,
						Constants.CMD,
						timeout,
						null,
						null
					)
				)
				.thenReturn(Constants.RESULT);

			assertEquals(
				Constants.RESULT,
				OsCommandHelper.runSshCommand(Constants.CMD, Constants.HOST, sshConfiguration, timeout, null, null)
			);
		}
	}

	@Test
	void testGetFileNameFromSudoCommand() {
		assertThrows(IllegalArgumentException.class, () -> OsCommandHelper.getFileNameFromSudoCommand(null));
		assertEquals(Optional.empty(), OsCommandHelper.getFileNameFromSudoCommand(Constants.RAIDCTL_COMMAND));
		assertEquals(
			Optional.of(Constants.RAIDCTL_PATH),
			OsCommandHelper.getFileNameFromSudoCommand(Constants.SUDO_RAIDCTL_COMMAND)
		);
		assertEquals(
			Optional.of(Constants.NAVISECCLI_CAMEL_CASE),
			OsCommandHelper.getFileNameFromSudoCommand(Constants.SUDO_NAVISECCLI_COMMAND)
		);
	}

	@Test
	void testToCaseInsensitiveRegex() {
		assertThrows(IllegalArgumentException.class, () -> OsCommandHelper.toCaseInsensitiveRegex(null));
		assertThrows(IllegalArgumentException.class, () -> OsCommandHelper.toCaseInsensitiveRegex(Constants.EMPTY));
		assertEquals(Constants.SINGLE_SPACE, OsCommandHelper.toCaseInsensitiveRegex(Constants.SINGLE_SPACE));
		assertEquals(Constants.Q_HOST, OsCommandHelper.toCaseInsensitiveRegex(Constants.HOST_CAMEL_CASE));
		assertEquals(Constants.Q_USERNAME, OsCommandHelper.toCaseInsensitiveRegex(Constants.PERCENT_USERNAME));
		assertEquals(Constants.Q_HOSTNAME, OsCommandHelper.toCaseInsensitiveRegex(Constants.HOSTNAME_MACRO));
	}

	@Test
	void testGetTimeout() {
		final OsCommandConfiguration osCommandConfig = new OsCommandConfiguration();
		osCommandConfig.setTimeout(2L);

		final WmiConfiguration wmiConfiguration = new WmiConfiguration();
		wmiConfiguration.setTimeout(3L);

		final SshConfiguration sshConfiguration = SshConfiguration
			.sshConfigurationBuilder()
			.username(Constants.USERNAME)
			.password(Constants.PASSWORD.toCharArray())
			.build();
		sshConfiguration.setTimeout(4L);

		assertEquals(1, OsCommandHelper.getTimeout(1L, osCommandConfig, sshConfiguration, 5));
		assertEquals(2, OsCommandHelper.getTimeout(null, osCommandConfig, sshConfiguration, 5));
		assertEquals(3, OsCommandHelper.getTimeout(null, null, wmiConfiguration, 5));
		assertEquals(4, OsCommandHelper.getTimeout(null, null, sshConfiguration, 5));
		assertEquals(5, OsCommandHelper.getTimeout(null, null, null, 5));
		assertEquals(30, OsCommandHelper.getTimeout(null, new OsCommandConfiguration(), sshConfiguration, 5));
		assertEquals(120, OsCommandHelper.getTimeout(null, null, new WmiConfiguration(), 5));
		assertEquals(
			30,
			OsCommandHelper.getTimeout(
				null,
				null,
				SshConfiguration
					.sshConfigurationBuilder()
					.username(Constants.USERNAME)
					.password(Constants.PASSWORD.toCharArray())
					.build(),
				5
			)
		);
	}

	@Test
	void testGetUsername() {
		assertEquals(Optional.empty(), OsCommandHelper.getUsername(null));
		assertEquals(Optional.empty(), OsCommandHelper.getUsername(new OsCommandConfiguration()));
		assertEquals(Optional.empty(), OsCommandHelper.getUsername(new WmiConfiguration()));
		assertEquals(
			Optional.empty(),
			OsCommandHelper.getUsername(
				SshConfiguration.sshConfigurationBuilder().password(Constants.PASSWORD.toCharArray()).build()
			)
		);

		final WmiConfiguration wmiConfiguration = new WmiConfiguration();
		wmiConfiguration.setUsername(Constants.USERNAME);
		assertEquals(Optional.of(Constants.USERNAME), OsCommandHelper.getUsername(wmiConfiguration));

		assertEquals(
			Optional.of(Constants.USERNAME),
			OsCommandHelper.getUsername(
				SshConfiguration
					.sshConfigurationBuilder()
					.username(Constants.USERNAME)
					.password(Constants.PASSWORD.toCharArray())
					.build()
			)
		);
	}

	@Test
	void testGetPassword() {
		assertEquals(Optional.empty(), OsCommandHelper.getPassword(null));
		assertEquals(Optional.empty(), OsCommandHelper.getPassword(new OsCommandConfiguration()));
		assertEquals(Optional.empty(), OsCommandHelper.getPassword(new WmiConfiguration()));
		assertEquals(
			Optional.empty(),
			OsCommandHelper.getPassword(SshConfiguration.sshConfigurationBuilder().username(Constants.USERNAME).build())
		);

		final WmiConfiguration wmiConfiguration = new WmiConfiguration();
		char[] charArrayPassword = Constants.PASSWORD.toCharArray();
		wmiConfiguration.setPassword(charArrayPassword);
		assertEquals(Optional.of(charArrayPassword), OsCommandHelper.getPassword(wmiConfiguration));

		assertEquals(
			Optional.of(charArrayPassword),
			OsCommandHelper.getPassword(
				SshConfiguration.sshConfigurationBuilder().username(Constants.USERNAME).password(charArrayPassword).build()
			)
		);
	}

	@Test
	void testRunOsCommandCommandLineNull() {
		final TelemetryManager telemetryManager = TelemetryManager.builder().build();
		assertThrows(
			IllegalArgumentException.class,
			() -> OsCommandHelper.runOsCommand(null, telemetryManager, 120L, false, false)
		);
	}

	@Test
	void testRunOsCommandTelemetryManagerNull() {
		assertThrows(
			IllegalArgumentException.class,
			() -> OsCommandHelper.runOsCommand(Constants.CMD, null, 120L, false, false)
		);
	}

	@Test
	void testRunOsCommandRemoteNoUser() {
		final SshConfiguration sshConfiguration = SshConfiguration
			.sshConfigurationBuilder()
			.username(Constants.SINGLE_SPACE)
			.password(Constants.PWD_COMMAND.toCharArray())
			.build();

		final HostConfiguration hostConfiguration = HostConfiguration
			.builder()
			.hostId(Constants.ID)
			.hostname(Constants.HOST)
			.hostType(DeviceKind.WINDOWS)
			.configurations(Map.of(sshConfiguration.getClass(), sshConfiguration))
			.build();

		final TelemetryManager telemetryManager = TelemetryManager.builder().hostConfiguration(hostConfiguration).build();

		assertThrows(
			NoCredentialProvidedException.class,
			() -> OsCommandHelper.runOsCommand(Constants.NAVISECCLI_COMMAND, telemetryManager, 120L, false, false)
		);
	}

	@Test
	void testRunOsCommandRemoteWindowsEmbeddedFilesError() {
		final Map<String, EmbeddedFile> embeddedFiles = new HashMap<>();
		embeddedFiles.put(
			Constants.EMBEDDED_FILE_1_REF,
			new EmbeddedFile(Constants.ECHO_OS, Constants.BAT, Constants.EMBEDDED_FILE_1_REF)
		);
		embeddedFiles.put(
			Constants.EMBEDDED_FILE_2_REF,
			new EmbeddedFile(Constants.ECHO_HELLO_WORLD, null, Constants.EMBEDDED_FILE_2_REF)
		);

		final WmiConfiguration wmiConfiguration = new WmiConfiguration();
		wmiConfiguration.setUsername(Constants.USERNAME);
		wmiConfiguration.setPassword(Constants.PWD_COMMAND.toCharArray());

		final HostConfiguration hostConfiguration = HostConfiguration
			.builder()
			.hostId(Constants.ID)
			.hostname(Constants.HOST)
			.hostType(DeviceKind.WINDOWS)
			.configurations(Map.of(wmiConfiguration.getClass(), wmiConfiguration))
			.build();

		final TelemetryManager telemetryManager = TelemetryManager.builder().hostConfiguration(hostConfiguration).build();

		try (final MockedStatic<OsCommandHelper> mockedOsCommandHelper = mockStatic(OsCommandHelper.class)) {
			mockedOsCommandHelper.when(() -> OsCommandHelper.getUsername(wmiConfiguration)).thenCallRealMethod();
			mockedOsCommandHelper
				.when(() -> OsCommandHelper.runOsCommand(Constants.COMMAND_TO_UPDATE, telemetryManager, 120L, false, false))
				.thenCallRealMethod();

			mockedOsCommandHelper
				.when(() ->
					OsCommandHelper.createOsCommandEmbeddedFiles(
						Constants.COMMAND_TO_UPDATE,
						null,
						commandLineEmbeddedFiles,
						TEMP_FILE_CREATOR
					)
				)
				.thenThrow(new IOException(Constants.ERROR_IN_FILE1));

			assertThrows(
				IOException.class,
				() -> OsCommandHelper.runOsCommand(Constants.COMMAND_TO_UPDATE, telemetryManager, 120L, false, false)
			);
		}
	}

	@Test
	@EnabledOnOs(OS.WINDOWS)
	void testRunOsCommandWindowsError() {
		final SshConfiguration sshConfiguration = SshConfiguration
			.sshConfigurationBuilder()
			.username(Constants.USERNAME)
			.password(Constants.PWD_COMMAND.toCharArray())
			.build();

		final HostConfiguration hostConfiguration = HostConfiguration
			.builder()
			.hostId(Constants.ID)
			.hostname(Constants.HOST)
			.hostType(DeviceKind.WINDOWS)
			.configurations(Map.of(sshConfiguration.getClass(), sshConfiguration))
			.build();

		final TelemetryManager telemetryManager = TelemetryManager.builder().hostConfiguration(hostConfiguration).build();

		assertThrows(
			TimeoutException.class,
			() -> OsCommandHelper.runOsCommand(Constants.PAUSE, telemetryManager, 1L, false, true)
		);
	}

	@Test
	@EnabledOnOs(OS.LINUX)
	void testRunOsCommandLinuxError() {
		final HostConfiguration hostConfiguration = HostConfiguration
			.builder()
			.hostId(Constants.ID)
			.hostname(Constants.HOST)
			.hostType(DeviceKind.LINUX)
			.build();

		final TelemetryManager telemetryManager = TelemetryManager.builder().hostConfiguration(hostConfiguration).build();

		assertThrows(
			TimeoutException.class,
			() -> OsCommandHelper.runOsCommand(Constants.SLEEP_5, telemetryManager, 1L, false, true)
		);
	}

	@Test
	@EnabledOnOs(OS.WINDOWS)
	void testRunOsCommandLocalWindows() throws Exception {
		final WmiConfiguration wmiConfiguration = new WmiConfiguration();
		wmiConfiguration.setUsername(Constants.USERNAME);
		wmiConfiguration.setPassword(Constants.PWD_COMMAND.toCharArray());

		final HostConfiguration hostConfiguration = HostConfiguration
			.builder()
			.hostId(Constants.ID)
			.hostname(Constants.HOST)
			.hostType(DeviceKind.WINDOWS)
			.configurations(Map.of(wmiConfiguration.getClass(), wmiConfiguration))
			.build();

		final TelemetryManager telemetryManager = TelemetryManager.builder().hostConfiguration(hostConfiguration).build();

		final OsCommandResult expect = new OsCommandResult(Constants.TEST_RESULT, Constants.ECHO_TEST_UPPER_CASE);

		assertEquals(
			expect,
			OsCommandHelper.runOsCommand(Constants.ECHO_TEST_UPPER_CASE, telemetryManager, 120L, false, true)
		);
	}

	@Test
	@EnabledOnOs(OS.LINUX)
	void testRunOsCommandLocalLinux() throws Exception {
		final SshConfiguration sshConfiguration = SshConfiguration
			.sshConfigurationBuilder()
			.username(Constants.USERNAME)
			.password(Constants.PWD_COMMAND.toCharArray())
			.build();

		final HostConfiguration hostConfiguration = HostConfiguration
			.builder()
			.hostId(Constants.ID)
			.hostname(Constants.HOST)
			.hostType(DeviceKind.LINUX)
			.configurations(Map.of(sshConfiguration.getClass(), sshConfiguration))
			.build();

		final TelemetryManager telemetryManager = TelemetryManager.builder().hostConfiguration(hostConfiguration).build();

		final OsCommandResult expect = new OsCommandResult(Constants.TEST_RESULT, Constants.ECHO_TEST_LOWER_CASE);

		assertEquals(
			expect,
			OsCommandHelper.runOsCommand(Constants.ECHO_TEST_LOWER_CASE, telemetryManager, 120L, false, true)
		);
	}

	@Test
	@EnabledOnOs(OS.WINDOWS)
	void testRunOsCommandRemoteExecutedLocallyWindows() throws Exception {
		final OsCommandResult expect = new OsCommandResult(Constants.TEST_RESULT, Constants.ECHO_TEST_UPPER_CASE);

		final HostConfiguration hostConfiguration = HostConfiguration
			.builder()
			.hostId(Constants.ID)
			.hostname(Constants.HOST)
			.hostType(DeviceKind.WINDOWS)
			.build();

		final TelemetryManager telemetryManager = TelemetryManager.builder().hostConfiguration(hostConfiguration).build();

		assertEquals(
			expect,
			OsCommandHelper.runOsCommand(Constants.ECHO_TEST_UPPER_CASE, telemetryManager, 120L, true, false)
		);
	}

	@Test
	@EnabledOnOs(OS.LINUX)
	void testRunOsCommandRemoteExecutedLocallyLinux() throws Exception {
		final OsCommandResult expect = new OsCommandResult(Constants.TEST_RESULT, Constants.ECHO_TEST_LOWER_CASE);

		final HostConfiguration hostConfiguration = HostConfiguration
			.builder()
			.hostId(Constants.ID)
			.hostname(Constants.HOST)
			.hostType(DeviceKind.LINUX)
			.build();

		final TelemetryManager telemetryManager = TelemetryManager.builder().hostConfiguration(hostConfiguration).build();

		assertEquals(
			expect,
			OsCommandHelper.runOsCommand(Constants.ECHO_TEST_LOWER_CASE, telemetryManager, 120L, true, false)
		);
	}

	@Test
	void testRunOsCommandRemoteWindows() throws Exception {
		commandLineEmbeddedFiles.put(
			Constants.EMBEDDED_FILE_1_REF,
			new EmbeddedFile(Constants.ECHO_OS, Constants.BAT, Constants.EMBEDDED_FILE_1_REF)
		);
		commandLineEmbeddedFiles.put(
			Constants.EMBEDDED_FILE_2_REF,
			new EmbeddedFile(Constants.ECHO_HELLO_WORLD, null, Constants.EMBEDDED_FILE_2_REF)
		);

		final File file1 = mock(File.class);
		final File file2 = mock(File.class);
		final Map<String, File> embeddedTempFiles = new HashMap<>();
		embeddedTempFiles.put(Constants.EMBEDDED_FILE_1_REF, file1);
		embeddedTempFiles.put(Constants.EMBEDDED_FILE_2_REF, file2);

		final WmiConfiguration wmiConfiguration = new WmiConfiguration();
		wmiConfiguration.setUsername(Constants.USERNAME);
		wmiConfiguration.setPassword(Constants.PWD_COMMAND.toCharArray());

		final HostConfiguration hostConfiguration = HostConfiguration
			.builder()
			.hostId(Constants.ID)
			.hostname(Constants.HOST)
			.hostType(DeviceKind.WINDOWS)
			.configurations(Map.of(wmiConfiguration.getClass(), wmiConfiguration))
			.build();

		final TelemetryManager telemetryManager = TelemetryManager.builder().hostConfiguration(hostConfiguration).build();

		try (
			final MockedStatic<OsCommandHelper> mockedOsCommandHelper = mockStatic(OsCommandHelper.class);
			final MockedStatic<ClientsExecutor> mockedClientsExecutor = mockStatic(ClientsExecutor.class);
			final MockedStatic<EmbeddedFileHelper> mockedEmbeddedFileHelper = mockStatic(EmbeddedFileHelper.class)
		) {
			mockedOsCommandHelper.when(() -> OsCommandHelper.getUsername(wmiConfiguration)).thenCallRealMethod();
			mockedOsCommandHelper
				.when(() -> OsCommandHelper.getTimeout(120L, null, wmiConfiguration, 300))
				.thenCallRealMethod();
			mockedOsCommandHelper.when(() -> OsCommandHelper.getPassword(wmiConfiguration)).thenCallRealMethod();
			mockedOsCommandHelper.when(() -> OsCommandHelper.replaceSudo(anyString(), isNull())).thenCallRealMethod();
			mockedOsCommandHelper.when(() -> OsCommandHelper.getFileNameFromSudoCommand(anyString())).thenCallRealMethod();
			mockedOsCommandHelper.when(() -> OsCommandHelper.toCaseInsensitiveRegex(anyString())).thenCallRealMethod();
			mockedEmbeddedFileHelper
				.when(() -> EmbeddedFileHelper.findEmbeddedFiles(anyString()))
				.thenReturn(commandLineEmbeddedFiles);

			mockedOsCommandHelper
				.when(() ->
					OsCommandHelper.createOsCommandEmbeddedFiles(
						Constants.COMMAND_TO_UPDATE,
						null,
						commandLineEmbeddedFiles,
						TEMP_FILE_CREATOR
					)
				)
				.thenReturn(embeddedTempFiles);

			Mockito.doReturn(Constants.TEMP_EMBEDDED_1).when(file1).getAbsolutePath();
			Mockito.doReturn(Constants.TEMP_EMBEDDED_2).when(file2).getAbsolutePath();

			mockedClientsExecutor
				.when(() ->
					ClientsExecutor.executeWinRemoteCommand(
						eq(Constants.HOST),
						eq(wmiConfiguration),
						eq(Constants.UPDATED_COMMAND),
						any()
					)
				)
				.thenReturn(Constants.WINDOWS_NT_HELLO_WORLD);

			mockedOsCommandHelper
				.when(() -> OsCommandHelper.runOsCommand(Constants.COMMAND_TO_UPDATE, telemetryManager, 120L, false, false))
				.thenCallRealMethod();

			final OsCommandResult expect = new OsCommandResult(Constants.WINDOWS_NT_HELLO_WORLD, Constants.UPDATED_COMMAND);

			final OsCommandResult res = OsCommandHelper.runOsCommand(
				Constants.COMMAND_TO_UPDATE,
				telemetryManager,
				120L,
				false,
				false
			);
			assertEquals(expect, res);
		}
	}

	@Test
	void testRunOsCommandRemoteLinuxOSCommandConfigNull() throws Exception {
		final SshConfiguration sshConfiguration = SshConfiguration
			.sshConfigurationBuilder()
			.username(Constants.USERNAME)
			.password(Constants.PWD_COMMAND.toCharArray())
			.build();

		final HostConfiguration hostConfiguration = HostConfiguration
			.builder()
			.hostId(Constants.ID)
			.hostname(Constants.HOST)
			.hostType(DeviceKind.LINUX)
			.configurations(Map.of(sshConfiguration.getClass(), sshConfiguration))
			.build();

		final TelemetryManager telemetryManager = TelemetryManager.builder().hostConfiguration(hostConfiguration).build();

		try (final MockedStatic<OsCommandHelper> mockedOsCommandHelper = mockStatic(OsCommandHelper.class)) {
			mockedOsCommandHelper.when(() -> OsCommandHelper.getUsername(sshConfiguration)).thenCallRealMethod();
			mockedOsCommandHelper
				.when(() -> OsCommandHelper.getTimeout(120L, null, sshConfiguration, 300))
				.thenCallRealMethod();
			mockedOsCommandHelper.when(() -> OsCommandHelper.getPassword(sshConfiguration)).thenCallRealMethod();
			mockedOsCommandHelper.when(() -> OsCommandHelper.replaceSudo(anyString(), isNull())).thenCallRealMethod();
			mockedOsCommandHelper.when(() -> OsCommandHelper.getFileNameFromSudoCommand(anyString())).thenCallRealMethod();
			mockedOsCommandHelper.when(() -> OsCommandHelper.toCaseInsensitiveRegex(anyString())).thenCallRealMethod();
			mockedOsCommandHelper
				.when(() ->
					OsCommandHelper.createOsCommandEmbeddedFiles(
						Constants.NAVISECCLI_COMMAND,
						null,
						commandLineEmbeddedFiles,
						TEMP_FILE_CREATOR
					)
				)
				.thenCallRealMethod();

			mockedOsCommandHelper
				.when(() -> {
					OsCommandHelper.runSshCommand(
						Constants.CLEAR_PASSWORD_COMMAND,
						Constants.HOST,
						sshConfiguration,
						120,
						Collections.emptyList(),
						Constants.NO_PASSWORD_COMMAND
					);
				})
				.thenReturn(Constants.AGENT_REV_RESULT);

			mockedOsCommandHelper
				.when(() -> OsCommandHelper.runOsCommand(Constants.NAVISECCLI_COMMAND, telemetryManager, 120L, false, false))
				.thenCallRealMethod();

			final OsCommandResult expect = new OsCommandResult(Constants.AGENT_REV_RESULT, Constants.NO_PASSWORD_COMMAND);

			assertEquals(
				expect,
				OsCommandHelper.runOsCommand(Constants.NAVISECCLI_COMMAND, telemetryManager, 120L, false, false)
			);
		}
	}

	@Test
	void testRunOsCommandRemoteLinuxNoSudo() throws Exception {
		final SshConfiguration sshConfiguration = SshConfiguration
			.sshConfigurationBuilder()
			.username(Constants.USERNAME)
			.password(Constants.PWD_COMMAND.toCharArray())
			.build();

		final OsCommandConfiguration osCommandConfiguration = new OsCommandConfiguration();
		osCommandConfiguration.setUseSudoCommands(Collections.singleton(Constants.NAVISECCLI_CAMEL_CASE.toLowerCase()));

		final HostConfiguration hostConfiguration = HostConfiguration
			.builder()
			.hostId(Constants.ID)
			.hostname(Constants.HOST)
			.hostType(DeviceKind.LINUX)
			.configurations(
				Map.of(sshConfiguration.getClass(), sshConfiguration, osCommandConfiguration.getClass(), osCommandConfiguration)
			)
			.build();

		final TelemetryManager telemetryManager = TelemetryManager.builder().hostConfiguration(hostConfiguration).build();

		try (final MockedStatic<OsCommandHelper> mockedOsCommandHelper = mockStatic(OsCommandHelper.class)) {
			mockedOsCommandHelper.when(() -> OsCommandHelper.getUsername(sshConfiguration)).thenCallRealMethod();
			mockedOsCommandHelper
				.when(() -> OsCommandHelper.getTimeout(120L, osCommandConfiguration, sshConfiguration, 300))
				.thenCallRealMethod();
			mockedOsCommandHelper.when(() -> OsCommandHelper.getPassword(sshConfiguration)).thenCallRealMethod();
			mockedOsCommandHelper
				.when(() -> OsCommandHelper.replaceSudo(anyString(), eq(osCommandConfiguration)))
				.thenCallRealMethod();
			mockedOsCommandHelper.when(() -> OsCommandHelper.getFileNameFromSudoCommand(anyString())).thenCallRealMethod();
			mockedOsCommandHelper.when(() -> OsCommandHelper.toCaseInsensitiveRegex(anyString())).thenCallRealMethod();
			mockedOsCommandHelper
				.when(() ->
					OsCommandHelper.createOsCommandEmbeddedFiles(
						Constants.NAVISECCLI_COMMAND,
						osCommandConfiguration,
						commandLineEmbeddedFiles,
						TEMP_FILE_CREATOR
					)
				)
				.thenCallRealMethod();

			mockedOsCommandHelper
				.when(() ->
					OsCommandHelper.runSshCommand(
						Constants.CLEAR_PASSWORD_COMMAND,
						Constants.HOST,
						sshConfiguration,
						120L,
						Collections.emptyList(),
						Constants.NO_PASSWORD_COMMAND
					)
				)
				.thenReturn(Constants.AGENT_REV_RESULT);

			mockedOsCommandHelper
				.when(() -> OsCommandHelper.runOsCommand(Constants.NAVISECCLI_COMMAND, telemetryManager, 120L, false, false))
				.thenCallRealMethod();

			final OsCommandResult expect = new OsCommandResult(Constants.AGENT_REV_RESULT, Constants.NO_PASSWORD_COMMAND);

			assertEquals(
				expect,
				OsCommandHelper.runOsCommand(Constants.NAVISECCLI_COMMAND, telemetryManager, 120L, false, false)
			);
		}
	}

	@Test
	void testRunOsCommandRemoteLinuxNotInUseSudoCommands() throws Exception {
		final SshConfiguration sshConfiguration = SshConfiguration
			.sshConfigurationBuilder()
			.username(Constants.USERNAME)
			.password(Constants.PWD_COMMAND.toCharArray())
			.build();

		final OsCommandConfiguration osCommandConfiguration = new OsCommandConfiguration();
		osCommandConfiguration.setUseSudo(true);
		osCommandConfiguration.setUseSudoCommands(Collections.singleton(Constants.WMI_EXCEPTION_OTHER_MESSAGE));

		final HostConfiguration hostConfiguration = HostConfiguration
			.builder()
			.hostId(Constants.ID)
			.hostname(Constants.HOST)
			.hostType(DeviceKind.LINUX)
			.configurations(
				Map.of(sshConfiguration.getClass(), sshConfiguration, osCommandConfiguration.getClass(), osCommandConfiguration)
			)
			.build();

		final TelemetryManager telemetryManager = TelemetryManager.builder().hostConfiguration(hostConfiguration).build();

		try (final MockedStatic<OsCommandHelper> mockedOsCommandHelper = mockStatic(OsCommandHelper.class)) {
			mockedOsCommandHelper.when(() -> OsCommandHelper.getUsername(sshConfiguration)).thenCallRealMethod();
			mockedOsCommandHelper
				.when(() -> OsCommandHelper.getTimeout(120L, osCommandConfiguration, sshConfiguration, 300))
				.thenCallRealMethod();
			mockedOsCommandHelper.when(() -> OsCommandHelper.getPassword(sshConfiguration)).thenCallRealMethod();
			mockedOsCommandHelper
				.when(() -> OsCommandHelper.replaceSudo(anyString(), eq(osCommandConfiguration)))
				.thenCallRealMethod();
			mockedOsCommandHelper.when(() -> OsCommandHelper.getFileNameFromSudoCommand(anyString())).thenCallRealMethod();
			mockedOsCommandHelper.when(() -> OsCommandHelper.toCaseInsensitiveRegex(anyString())).thenCallRealMethod();
			mockedOsCommandHelper
				.when(() ->
					OsCommandHelper.createOsCommandEmbeddedFiles(
						Constants.NAVISECCLI_COMMAND,
						osCommandConfiguration,
						commandLineEmbeddedFiles,
						TEMP_FILE_CREATOR
					)
				)
				.thenCallRealMethod();

			mockedOsCommandHelper
				.when(() ->
					OsCommandHelper.runSshCommand(
						Constants.CLEAR_PASSWORD_COMMAND,
						Constants.HOST,
						sshConfiguration,
						120,
						Collections.emptyList(),
						Constants.NO_PASSWORD_COMMAND
					)
				)
				.thenReturn(Constants.AGENT_REV_RESULT);

			mockedOsCommandHelper
				.when(() -> OsCommandHelper.runOsCommand(Constants.NAVISECCLI_COMMAND, telemetryManager, 120L, false, false))
				.thenCallRealMethod();

			final OsCommandResult expect = new OsCommandResult(Constants.AGENT_REV_RESULT, Constants.NO_PASSWORD_COMMAND);

			assertEquals(
				expect,
				OsCommandHelper.runOsCommand(Constants.NAVISECCLI_COMMAND, telemetryManager, 120L, false, false)
			);
		}
	}

	@Test
	void testRunOsCommandRemoteLinuxWithSudoReplaced() throws Exception {
		final SshConfiguration sshConfiguration = SshConfiguration
			.sshConfigurationBuilder()
			.username(Constants.USERNAME)
			.password(Constants.PWD_COMMAND.toCharArray())
			.build();

		final OsCommandConfiguration osCommandConfiguration = new OsCommandConfiguration();
		osCommandConfiguration.setUseSudo(true);
		osCommandConfiguration.setUseSudoCommands(Collections.singleton(Constants.NAVISECCLI_CAMEL_CASE.toLowerCase()));

		final HostConfiguration hostConfiguration = HostConfiguration
			.builder()
			.hostId(Constants.ID)
			.hostname(Constants.HOST)
			.hostType(DeviceKind.LINUX)
			.configurations(
				Map.of(sshConfiguration.getClass(), sshConfiguration, osCommandConfiguration.getClass(), osCommandConfiguration)
			)
			.build();

		final TelemetryManager telemetryManager = TelemetryManager.builder().hostConfiguration(hostConfiguration).build();

		try (final MockedStatic<OsCommandHelper> mockedOsCommandHelper = mockStatic(OsCommandHelper.class)) {
			mockedOsCommandHelper.when(() -> OsCommandHelper.getUsername(sshConfiguration)).thenCallRealMethod();
			mockedOsCommandHelper
				.when(() -> OsCommandHelper.getTimeout(120L, osCommandConfiguration, sshConfiguration, 300))
				.thenCallRealMethod();
			mockedOsCommandHelper.when(() -> OsCommandHelper.getPassword(sshConfiguration)).thenCallRealMethod();
			mockedOsCommandHelper
				.when(() -> OsCommandHelper.replaceSudo(anyString(), eq(osCommandConfiguration)))
				.thenCallRealMethod();
			mockedOsCommandHelper.when(() -> OsCommandHelper.getFileNameFromSudoCommand(anyString())).thenCallRealMethod();
			mockedOsCommandHelper.when(() -> OsCommandHelper.toCaseInsensitiveRegex(anyString())).thenCallRealMethod();
			mockedOsCommandHelper
				.when(() ->
					OsCommandHelper.createOsCommandEmbeddedFiles(
						Constants.NAVISECCLI_COMMAND,
						osCommandConfiguration,
						commandLineEmbeddedFiles,
						TEMP_FILE_CREATOR
					)
				)
				.thenCallRealMethod();

			mockedOsCommandHelper
				.when(() -> {
					OsCommandHelper.runSshCommand(
						Constants.SUDO_KEYWORD + Constants.CLEAR_PASSWORD_COMMAND,
						Constants.HOST,
						sshConfiguration,
						120,
						Collections.emptyList(),
						Constants.SUDO_KEYWORD + Constants.NO_PASSWORD_COMMAND
					);
				})
				.thenReturn(Constants.AGENT_REV_RESULT);

			mockedOsCommandHelper
				.when(() -> OsCommandHelper.runOsCommand(Constants.NAVISECCLI_COMMAND, telemetryManager, 120L, false, false))
				.thenCallRealMethod();

			final OsCommandResult expect = new OsCommandResult(
				Constants.AGENT_REV_RESULT,
				Constants.SUDO_KEYWORD + Constants.NO_PASSWORD_COMMAND
			);

			assertEquals(
				expect,
				OsCommandHelper.runOsCommand(Constants.NAVISECCLI_COMMAND, telemetryManager, 120L, false, false)
			);
		}
	}

	@Test
	void testRunOsCommandRemoteLinuxWithEmbeddedFilesReplaced() throws Exception {
		commandLineEmbeddedFiles.put(
			Constants.EMBEDDED_FILE_1_REF,
			new EmbeddedFile(Constants.AWK_EMBEDDED_CONTENT_PERCENT_SUDO, null, Constants.EMBEDDED_FILE_1_REF)
		);

		final File localFile = mock(File.class);
		final Map<String, File> embeddedTempFiles = new HashMap<>();
		embeddedTempFiles.put(Constants.EMBEDDED_FILE_1_REF, localFile);

		final OsCommandConfiguration osCommandConfiguration = new OsCommandConfiguration();
		osCommandConfiguration.setUseSudo(true);
		osCommandConfiguration.setUseSudoCommands(Collections.singleton(Constants.ARCCONF_PATH));

		final SshConfiguration sshConfiguration = SshConfiguration
			.sshConfigurationBuilder()
			.username(Constants.USERNAME)
			.password(Constants.PWD_COMMAND.toCharArray())
			.build();

		final HostConfiguration hostConfiguration = HostConfiguration
			.builder()
			.hostId(Constants.ID)
			.hostname(Constants.HOST)
			.hostType(DeviceKind.LINUX)
			.configurations(
				Map.of(sshConfiguration.getClass(), sshConfiguration, osCommandConfiguration.getClass(), osCommandConfiguration)
			)
			.build();

		final TelemetryManager telemetryManager = TelemetryManager.builder().hostConfiguration(hostConfiguration).build();

		try (
			final MockedStatic<OsCommandHelper> mockedOsCommandHelper = mockStatic(OsCommandHelper.class);
			final MockedStatic<EmbeddedFileHelper> mockedEmbeddedFileHelper = mockStatic(EmbeddedFileHelper.class)
		) {
			mockedOsCommandHelper.when(() -> OsCommandHelper.getUsername(sshConfiguration)).thenCallRealMethod();
			mockedOsCommandHelper
				.when(() -> OsCommandHelper.getTimeout(120L, osCommandConfiguration, sshConfiguration, 300))
				.thenCallRealMethod();
			mockedOsCommandHelper.when(() -> OsCommandHelper.getPassword(sshConfiguration)).thenCallRealMethod();
			mockedOsCommandHelper.when(() -> OsCommandHelper.toCaseInsensitiveRegex(anyString())).thenCallRealMethod();
			mockedOsCommandHelper.when(() -> OsCommandHelper.getFileNameFromSudoCommand(anyString())).thenCallRealMethod();
			mockedOsCommandHelper
				.when(() -> OsCommandHelper.replaceSudo(anyString(), eq(osCommandConfiguration)))
				.thenCallRealMethod();

			mockedOsCommandHelper
				.when(() ->
					OsCommandHelper.createOsCommandEmbeddedFiles(
						Constants.SH_EMBEDDED_FILE_1,
						osCommandConfiguration,
						commandLineEmbeddedFiles,
						TEMP_FILE_CREATOR
					)
				)
				.thenReturn(embeddedTempFiles);

			mockedEmbeddedFileHelper
				.when(() -> EmbeddedFileHelper.findEmbeddedFiles(anyString()))
				.thenReturn(commandLineEmbeddedFiles);

			Mockito.doReturn(Constants.SEN_EMBEDDED_0001_PATH).when(localFile).getAbsolutePath();

			mockedOsCommandHelper
				.when(() -> {
					OsCommandHelper.runSshCommand(
						Constants.SH_SEN_EMBEDDED_0001_PATH,
						Constants.HOST,
						sshConfiguration,
						120,
						List.of(localFile),
						Constants.SH_SEN_EMBEDDED_0001_PATH
					);
				})
				.thenReturn(Constants.HARD_DRIVE);

			mockedOsCommandHelper
				.when(() -> OsCommandHelper.runOsCommand(Constants.SH_EMBEDDED_FILE_1, telemetryManager, 120L, false, false))
				.thenCallRealMethod();

			final OsCommandResult expect = new OsCommandResult(Constants.HARD_DRIVE, Constants.SH_SEN_EMBEDDED_0001_PATH);
			final OsCommandResult actual = OsCommandHelper.runOsCommand(
				Constants.SH_EMBEDDED_FILE_1,
				telemetryManager,
				120L,
				false,
				false
			);
			assertEquals(expect, actual);
		}
	}

	@Test
	void testRunControlledSshCommand() throws InterruptedException, ControlledSshException {
		Semaphore semaphore = SshSemaphoreFactory.getInstance().createOrGetSempahore(Constants.HOSTNAME);

		assertEquals(7, OsCommandHelper.runControlledSshCommand(semaphore::availablePermits, Constants.HOSTNAME, 30));

		Assertions.assertDoesNotThrow(() ->
			OsCommandHelper.runControlledSshCommand(LocalDate.MIN::toString, Constants.HOSTNAME, 30)
		);

		semaphore.acquire(8);
		assertThrows(
			ControlledSshException.class,
			() -> OsCommandHelper.runControlledSshCommand(LocalDate.MIN::toString, Constants.HOSTNAME, 1)
		);
	}
}
