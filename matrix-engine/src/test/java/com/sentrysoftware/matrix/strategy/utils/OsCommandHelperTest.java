package com.sentrysoftware.matrix.strategy.utils;

import static com.sentrysoftware.matrix.constants.Constants.AGENT_REV_RESULT;
import static com.sentrysoftware.matrix.constants.Constants.ARCCONF_PATH;
import static com.sentrysoftware.matrix.constants.Constants.AWK_EMBEDDED_CONTENT_PERCENT_SUDO;
import static com.sentrysoftware.matrix.constants.Constants.AWK_EMBEDDED_CONTENT_SUDO;
import static com.sentrysoftware.matrix.constants.Constants.BAT;
import static com.sentrysoftware.matrix.constants.Constants.BAT_FILE_EXTENSION;
import static com.sentrysoftware.matrix.constants.Constants.CLEAR_PASSWORD_COMMAND;
import static com.sentrysoftware.matrix.constants.Constants.CMD;
import static com.sentrysoftware.matrix.constants.Constants.CMD_COMMAND;
import static com.sentrysoftware.matrix.constants.Constants.COMMAND_TO_UPDATE;
import static com.sentrysoftware.matrix.constants.Constants.ECHO_HELLO_WORLD;
import static com.sentrysoftware.matrix.constants.Constants.ECHO_OS;
import static com.sentrysoftware.matrix.constants.Constants.ECHO_TEST_LOWER_CASE;
import static com.sentrysoftware.matrix.constants.Constants.ECHO_TEST_UPPER_CASE;
import static com.sentrysoftware.matrix.constants.Constants.EMBEDDED_FILE_1_COPY_COMMAND_LINE;
import static com.sentrysoftware.matrix.constants.Constants.EMBEDDED_FILE_1_REF;
import static com.sentrysoftware.matrix.constants.Constants.EMBEDDED_FILE_2_REF;
import static com.sentrysoftware.matrix.constants.Constants.EMBEDDED_TEMP_FILE_PREFIX;
import static com.sentrysoftware.matrix.constants.Constants.EMPTY;
import static com.sentrysoftware.matrix.constants.Constants.END_OF_LINE;
import static com.sentrysoftware.matrix.constants.Constants.END_OF_LINE_IN_BRACKETS;
import static com.sentrysoftware.matrix.constants.Constants.ERROR_IN_FILE1;
import static com.sentrysoftware.matrix.constants.Constants.HARD_DRIVE;
import static com.sentrysoftware.matrix.constants.Constants.HOST;
import static com.sentrysoftware.matrix.constants.Constants.HOSTNAME;
import static com.sentrysoftware.matrix.constants.Constants.HOSTNAME_MACRO;
import static com.sentrysoftware.matrix.constants.Constants.HOST_CAMEL_CASE;
import static com.sentrysoftware.matrix.constants.Constants.ID;
import static com.sentrysoftware.matrix.constants.Constants.KEY;
import static com.sentrysoftware.matrix.constants.Constants.NAVISECCLI_CAMEL_CASE;
import static com.sentrysoftware.matrix.constants.Constants.NAVISECCLI_COMMAND;
import static com.sentrysoftware.matrix.constants.Constants.NO_PASSWORD_COMMAND;
import static com.sentrysoftware.matrix.constants.Constants.PASSWORD;
import static com.sentrysoftware.matrix.constants.Constants.PAUSE;
import static com.sentrysoftware.matrix.constants.Constants.PERCENT_USERNAME;
import static com.sentrysoftware.matrix.constants.Constants.PWD_COMMAND;
import static com.sentrysoftware.matrix.constants.Constants.Q_HOST;
import static com.sentrysoftware.matrix.constants.Constants.Q_HOSTNAME;
import static com.sentrysoftware.matrix.constants.Constants.Q_USERNAME;
import static com.sentrysoftware.matrix.constants.Constants.RAIDCTL_COMMAND;
import static com.sentrysoftware.matrix.constants.Constants.RAIDCTL_PATH;
import static com.sentrysoftware.matrix.constants.Constants.RESULT;
import static com.sentrysoftware.matrix.constants.Constants.SEN_EMBEDDED_0001_PATH;
import static com.sentrysoftware.matrix.constants.Constants.SH_EMBEDDED_FILE_1;
import static com.sentrysoftware.matrix.constants.Constants.SH_SEN_EMBEDDED_0001_PATH;
import static com.sentrysoftware.matrix.constants.Constants.SINGLE_SPACE;
import static com.sentrysoftware.matrix.constants.Constants.SLEEP_5;
import static com.sentrysoftware.matrix.constants.Constants.SPACE_KEY;
import static com.sentrysoftware.matrix.constants.Constants.SUDO_KEY;
import static com.sentrysoftware.matrix.constants.Constants.SUDO_KEYWORD;
import static com.sentrysoftware.matrix.constants.Constants.SUDO_KEY_RESULT;
import static com.sentrysoftware.matrix.constants.Constants.SUDO_NAVISECCLI_COMMAND;
import static com.sentrysoftware.matrix.constants.Constants.SUDO_RAIDCTL_COMMAND;
import static com.sentrysoftware.matrix.constants.Constants.TEMP_EMBEDDED_1;
import static com.sentrysoftware.matrix.constants.Constants.TEMP_EMBEDDED_2;
import static com.sentrysoftware.matrix.constants.Constants.TEST_RESULT;
import static com.sentrysoftware.matrix.constants.Constants.TEXT;
import static com.sentrysoftware.matrix.constants.Constants.UPDATED_COMMAND;
import static com.sentrysoftware.matrix.constants.Constants.USERNAME;
import static com.sentrysoftware.matrix.constants.Constants.WINDOWS_NT_HELLO_WORLD;
import static com.sentrysoftware.matrix.constants.Constants.WMI_EXCEPTION_OTHER_MESSAGE;
import static com.sentrysoftware.matrix.strategy.utils.OsCommandHelper.TEMP_FILE_CREATOR;
import static org.junit.Assert.assertNull;
import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
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

import com.sentrysoftware.matrix.common.exception.ControlledSshException;
import com.sentrysoftware.matrix.common.exception.NoCredentialProvidedException;
import com.sentrysoftware.matrix.common.helpers.LocalOsHandler;
import com.sentrysoftware.matrix.configuration.HostConfiguration;
import com.sentrysoftware.matrix.configuration.OsCommandConfiguration;
import com.sentrysoftware.matrix.configuration.SshConfiguration;
import com.sentrysoftware.matrix.configuration.WmiConfiguration;
import com.sentrysoftware.matrix.connector.model.common.DeviceKind;
import com.sentrysoftware.matrix.connector.model.common.EmbeddedFile;
import com.sentrysoftware.matrix.matsya.MatsyaClientsExecutor;
import com.sentrysoftware.matrix.telemetry.SshSemaphoreFactory;
import com.sentrysoftware.matrix.telemetry.TelemetryManager;
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
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.condition.EnabledOnOs;
import org.junit.jupiter.api.condition.OS;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.api.io.TempDir;
import org.mockito.MockedStatic;
import org.mockito.junit.jupiter.MockitoExtension;

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
					return File.createTempFile(EMBEDDED_TEMP_FILE_PREFIX, extension, tempDir);
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
		return tempDir.listFiles((directory, fileName) -> fileName.startsWith(EMBEDDED_TEMP_FILE_PREFIX));
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
					EMBEDDED_FILE_1_COPY_COMMAND_LINE,
					null,
					EMPTY_EMBEDDED_FILE_MAP,
					jUnitTempFileCreator
				)
		);

		assertEquals(
			Collections.emptyMap(),
			OsCommandHelper.createOsCommandEmbeddedFiles(CMD, null, EMPTY_EMBEDDED_FILE_MAP, jUnitTempFileCreator)
		);

		// case embeddedFile not found
		commandLineEmbeddedFiles.put(EMBEDDED_FILE_1_REF, new EmbeddedFile());
		assertThrows(
			IllegalStateException.class,
			() ->
				OsCommandHelper.createOsCommandEmbeddedFiles(
					EMBEDDED_FILE_1_COPY_COMMAND_LINE,
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
					EMBEDDED_FILE_1_COPY_COMMAND_LINE,
					null,
					commandLineEmbeddedFiles,
					jUnitTempFileCreator
				)
		);

		checkNoTempEmbeddedFileExist();

		// case IOException in temp file creation
		try (final MockedStatic<OsCommandHelper> mockedOsCommandHelper = mockStatic(OsCommandHelper.class)) {
			commandLineEmbeddedFiles.put(EMBEDDED_FILE_1_REF, new EmbeddedFile(ECHO_OS, BAT, EMBEDDED_FILE_1_REF));
			commandLineEmbeddedFiles.put(EMBEDDED_FILE_2_REF, new EmbeddedFile(ECHO_HELLO_WORLD, null, EMBEDDED_FILE_2_REF));

			mockedOsCommandHelper
				.when(() -> OsCommandHelper.createTempFileWithEmbeddedFileContent(any(EmbeddedFile.class), isNull(), any()))
				.thenThrow(IOException.class);

			mockedOsCommandHelper
				.when(() ->
					OsCommandHelper.createOsCommandEmbeddedFiles(
						EMBEDDED_FILE_1_COPY_COMMAND_LINE,
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
						EMBEDDED_FILE_1_COPY_COMMAND_LINE,
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
				EMBEDDED_FILE_2_REF,
				new EmbeddedFile(AWK_EMBEDDED_CONTENT_PERCENT_SUDO, null, EMBEDDED_FILE_2_REF)
			);

			final Map<String, File> embeddedTempFiles = OsCommandHelper.createOsCommandEmbeddedFiles(
				EMBEDDED_FILE_1_COPY_COMMAND_LINE,
				OsCommandConfiguration.builder().useSudo(true).useSudoCommands(Set.of(ARCCONF_PATH)).build(),
				commandLineEmbeddedFiles,
				jUnitTempFileCreator
			);

			assertEquals(2, embeddedTempFiles.size());

			{
				final File file = embeddedTempFiles.get(EMBEDDED_FILE_1_REF);
				assertNotNull(file);
				assertTrue(file.exists());
				assertTrue(file.getName().matches(EMBEDDED_TEMP_FILE_PREFIX + BAT_FILE_EXTENSION));
				assertEquals(
					ECHO_OS,
					Files.readAllLines(Paths.get(file.getAbsolutePath())).stream().collect(Collectors.joining())
				);
				file.delete();
			}
			{
				final File file = embeddedTempFiles.get(EMBEDDED_FILE_2_REF);
				assertNotNull(file);
				assertTrue(file.exists());
				assertTrue(file.getName().matches(EMBEDDED_TEMP_FILE_PREFIX + "\\w+"));
				/*
				 * assertEquals(
-						expectedContent.replaceAll("[\r\n]", EMPTY),
-						Files.readAllLines(Paths.get(file.getAbsolutePath())).stream().collect(Collectors.joining()));
				 */
				assertEquals(
					AWK_EMBEDDED_CONTENT_SUDO.replaceAll(END_OF_LINE_IN_BRACKETS, EMPTY),
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

		assertEquals(EMPTY, OsCommandHelper.replaceSudo(EMPTY, null));
		assertEquals(EMPTY, OsCommandHelper.replaceSudo(EMPTY, OsCommandConfiguration.builder().build()));
		assertEquals(SINGLE_SPACE, OsCommandHelper.replaceSudo(SINGLE_SPACE, null));
		assertEquals(SINGLE_SPACE, OsCommandHelper.replaceSudo(SINGLE_SPACE, OsCommandConfiguration.builder().build()));

		assertEquals(TEXT, OsCommandHelper.replaceSudo(TEXT, null));
		assertEquals(TEXT, OsCommandHelper.replaceSudo(TEXT, OsCommandConfiguration.builder().build()));

		// Check replace sudo tag with empty string.
		assertEquals(SPACE_KEY, OsCommandHelper.replaceSudo(SUDO_KEY, null));
		assertEquals(SPACE_KEY, OsCommandHelper.replaceSudo(SUDO_KEY, OsCommandConfiguration.builder().build()));
		assertEquals(
			SPACE_KEY,
			OsCommandHelper.replaceSudo(SUDO_KEY, OsCommandConfiguration.builder().useSudo(true).build())
		);
		assertEquals(
			SPACE_KEY + END_OF_LINE + SPACE_KEY,
			OsCommandHelper.replaceSudo(
				SUDO_KEY + END_OF_LINE + SUDO_KEY,
				OsCommandConfiguration.builder().useSudo(true).build()
			)
		);

		assertEquals(
			SUDO_KEYWORD + SPACE_KEY,
			OsCommandHelper.replaceSudo(
				SUDO_KEY,
				OsCommandConfiguration.builder().useSudo(true).useSudoCommands(Set.of(KEY)).build()
			)
		);

		assertEquals(
			SUDO_KEY_RESULT,
			OsCommandHelper.replaceSudo(
				SUDO_KEY + END_OF_LINE + SUDO_KEY,
				OsCommandConfiguration.builder().useSudo(true).useSudoCommands(Set.of(KEY)).build()
			)
		);
	}

	@Test
	void testRunLocalCommandKO() throws Exception {
		assertThrows(IllegalArgumentException.class, () -> OsCommandHelper.runLocalCommand(null, 1, null));
		assertThrows(IllegalArgumentException.class, () -> OsCommandHelper.runLocalCommand(CMD, -1, null));
		assertThrows(IllegalArgumentException.class, () -> OsCommandHelper.runLocalCommand(CMD, 0, null));

		// case Process null Linux
		final Runtime runtime = mock(Runtime.class);
		try (
			final MockedStatic<LocalOsHandler> mockedLocalOSHandler = mockStatic(LocalOsHandler.class);
			final MockedStatic<Runtime> mockedRuntime = mockStatic(Runtime.class)
		) {
			mockedLocalOSHandler.when(LocalOsHandler::isWindows).thenReturn(false);
			mockedRuntime.when(Runtime::getRuntime).thenReturn(runtime);
			when(runtime.exec(CMD)).thenReturn(null);

			assertThrows(IllegalStateException.class, () -> OsCommandHelper.runLocalCommand(CMD, 1, null));
		}

		// case Process null Windows
		try (
			final MockedStatic<LocalOsHandler> mockedLocalOSHandler = mockStatic(LocalOsHandler.class);
			final MockedStatic<Runtime> mockedRuntime = mockStatic(Runtime.class)
		) {
			mockedLocalOSHandler.when(LocalOsHandler::isWindows).thenReturn(true);
			mockedRuntime.when(Runtime::getRuntime).thenReturn(runtime);
			when(runtime.exec(CMD_COMMAND)).thenReturn(null);

			assertThrows(IllegalStateException.class, () -> OsCommandHelper.runLocalCommand(CMD, 1, null));
		}
	}

	@Test
	@EnabledOnOs(OS.WINDOWS)
	void testRunLocalCommandWindowsTimeout() throws Exception {
		assertThrows(TimeoutException.class, () -> OsCommandHelper.runLocalCommand(PAUSE, 1, null));
	}

	@Test
	@EnabledOnOs(OS.LINUX)
	void testRunLocalCommandLinuxTimeout() throws Exception {
		assertThrows(TimeoutException.class, () -> OsCommandHelper.runLocalCommand(SLEEP_5, 1, null));
	}

	@Test
	@EnabledOnOs(OS.WINDOWS)
	void testRunLocalCommandWindows() throws Exception {
		assertEquals(TEST_RESULT, OsCommandHelper.runLocalCommand(ECHO_TEST_UPPER_CASE, 1, null));
	}

	@Test
	@EnabledOnOs(OS.LINUX)
	void testRunLocalCommandLinux() throws Exception {
		assertEquals(TEST_RESULT, OsCommandHelper.runLocalCommand(ECHO_TEST_LOWER_CASE, 1, null));
	}

	@Test
	void testRunSshCommand() throws Exception {
		final SshConfiguration sshConfiguration = mock(SshConfiguration.class);
		final int timeout = 1000;

		assertThrows(
			IllegalArgumentException.class,
			() -> OsCommandHelper.runSshCommand(null, HOST, sshConfiguration, timeout, null, null)
		);
		assertThrows(
			IllegalArgumentException.class,
			() -> OsCommandHelper.runSshCommand(CMD, null, sshConfiguration, timeout, null, null)
		);
		assertThrows(
			IllegalArgumentException.class,
			() -> OsCommandHelper.runSshCommand(CMD, HOST, null, timeout, null, null)
		);
		assertThrows(
			IllegalArgumentException.class,
			() -> OsCommandHelper.runSshCommand(CMD, HOST, sshConfiguration, -1, null, null)
		);
		assertThrows(
			IllegalArgumentException.class,
			() -> OsCommandHelper.runSshCommand(CMD, HOST, sshConfiguration, 0, null, null)
		);

		try (
			final MockedStatic<MatsyaClientsExecutor> mockedMatsyaClientsExecutor = mockStatic(MatsyaClientsExecutor.class)
		) {
			when(sshConfiguration.getUsername()).thenReturn(USERNAME);
			when(sshConfiguration.getPassword()).thenReturn(PASSWORD.toCharArray());

			mockedMatsyaClientsExecutor
				.when(() ->
					MatsyaClientsExecutor.runRemoteSshCommand(
						HOST,
						USERNAME,
						PASSWORD.toCharArray(),
						null,
						CMD,
						timeout,
						null,
						null
					)
				)
				.thenReturn(RESULT);

			assertEquals(RESULT, OsCommandHelper.runSshCommand(CMD, HOST, sshConfiguration, timeout, null, null));
		}
	}

	@Test
	void testGetFileNameFromSudoCommand() {
		assertThrows(IllegalArgumentException.class, () -> OsCommandHelper.getFileNameFromSudoCommand(null));
		assertEquals(Optional.empty(), OsCommandHelper.getFileNameFromSudoCommand(RAIDCTL_COMMAND));
		assertEquals(Optional.of(RAIDCTL_PATH), OsCommandHelper.getFileNameFromSudoCommand(SUDO_RAIDCTL_COMMAND));
		assertEquals(
			Optional.of(NAVISECCLI_CAMEL_CASE),
			OsCommandHelper.getFileNameFromSudoCommand(SUDO_NAVISECCLI_COMMAND)
		);
	}

	@Test
	void testToCaseInsensitiveRegex() {
		assertThrows(IllegalArgumentException.class, () -> OsCommandHelper.toCaseInsensitiveRegex(null));
		assertThrows(IllegalArgumentException.class, () -> OsCommandHelper.toCaseInsensitiveRegex(EMPTY));
		assertEquals(SINGLE_SPACE, OsCommandHelper.toCaseInsensitiveRegex(SINGLE_SPACE));
		assertEquals(Q_HOST, OsCommandHelper.toCaseInsensitiveRegex(HOST_CAMEL_CASE));
		assertEquals(Q_USERNAME, OsCommandHelper.toCaseInsensitiveRegex(PERCENT_USERNAME));
		assertEquals(Q_HOSTNAME, OsCommandHelper.toCaseInsensitiveRegex(HOSTNAME_MACRO));
	}

	@Test
	void testGetTimeout() {
		final OsCommandConfiguration osCommandConfig = new OsCommandConfiguration();
		osCommandConfig.setTimeout(2L);

		final WmiConfiguration wmiConfiguration = new WmiConfiguration();
		wmiConfiguration.setTimeout(3L);

		final SshConfiguration sshConfiguration = SshConfiguration
			.sshConfigurationBuilder()
			.username(USERNAME)
			.password(PASSWORD.toCharArray())
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
				SshConfiguration.sshConfigurationBuilder().username(USERNAME).password(PASSWORD.toCharArray()).build(),
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
			OsCommandHelper.getUsername(SshConfiguration.sshConfigurationBuilder().password(PASSWORD.toCharArray()).build())
		);

		final WmiConfiguration wmiConfiguration = new WmiConfiguration();
		wmiConfiguration.setUsername(USERNAME);
		assertEquals(Optional.of(USERNAME), OsCommandHelper.getUsername(wmiConfiguration));

		assertEquals(
			Optional.of(USERNAME),
			OsCommandHelper.getUsername(
				SshConfiguration.sshConfigurationBuilder().username(USERNAME).password(PASSWORD.toCharArray()).build()
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
			OsCommandHelper.getPassword(SshConfiguration.sshConfigurationBuilder().username(USERNAME).build())
		);

		final WmiConfiguration wmiConfiguration = new WmiConfiguration();
		char[] charArrayPassword = PASSWORD.toCharArray();
		wmiConfiguration.setPassword(charArrayPassword);
		assertEquals(Optional.of(charArrayPassword), OsCommandHelper.getPassword(wmiConfiguration));

		assertEquals(
			Optional.of(charArrayPassword),
			OsCommandHelper.getPassword(
				SshConfiguration.sshConfigurationBuilder().username(USERNAME).password(charArrayPassword).build()
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
		assertThrows(IllegalArgumentException.class, () -> OsCommandHelper.runOsCommand(CMD, null, 120L, false, false));
	}

	@Test
	void testRunOsCommandRemoteNoUser() {
		final SshConfiguration sshConfiguration = SshConfiguration
			.sshConfigurationBuilder()
			.username(SINGLE_SPACE)
			.password(PWD_COMMAND.toCharArray())
			.build();

		final HostConfiguration hostConfiguration = HostConfiguration
			.builder()
			.hostId(ID)
			.hostname(HOST)
			.hostType(DeviceKind.WINDOWS)
			.configurations(Map.of(sshConfiguration.getClass(), sshConfiguration))
			.build();

		final TelemetryManager telemetryManager = TelemetryManager.builder().hostConfiguration(hostConfiguration).build();

		assertThrows(
			NoCredentialProvidedException.class,
			() -> OsCommandHelper.runOsCommand(NAVISECCLI_COMMAND, telemetryManager, 120L, false, false)
		);
	}

	@Test
	void testRunOsCommandRemoteWindowsEmbeddedFilesError() {
		final Map<String, EmbeddedFile> embeddedFiles = new HashMap<>();
		embeddedFiles.put(EMBEDDED_FILE_1_REF, new EmbeddedFile(ECHO_OS, BAT, EMBEDDED_FILE_1_REF));
		embeddedFiles.put(EMBEDDED_FILE_2_REF, new EmbeddedFile(ECHO_HELLO_WORLD, null, EMBEDDED_FILE_2_REF));

		final WmiConfiguration wmiConfiguration = new WmiConfiguration();
		wmiConfiguration.setUsername(USERNAME);
		wmiConfiguration.setPassword(PWD_COMMAND.toCharArray());

		final HostConfiguration hostConfiguration = HostConfiguration
			.builder()
			.hostId(ID)
			.hostname(HOST)
			.hostType(DeviceKind.WINDOWS)
			.configurations(Map.of(wmiConfiguration.getClass(), wmiConfiguration))
			.build();

		final TelemetryManager telemetryManager = TelemetryManager.builder().hostConfiguration(hostConfiguration).build();

		try (final MockedStatic<OsCommandHelper> mockedOsCommandHelper = mockStatic(OsCommandHelper.class)) {
			mockedOsCommandHelper.when(() -> OsCommandHelper.getUsername(wmiConfiguration)).thenCallRealMethod();
			mockedOsCommandHelper
				.when(() -> OsCommandHelper.runOsCommand(COMMAND_TO_UPDATE, telemetryManager, 120L, false, false))
				.thenCallRealMethod();

			mockedOsCommandHelper
				.when(() ->
					OsCommandHelper.createOsCommandEmbeddedFiles(
						COMMAND_TO_UPDATE,
						null,
						commandLineEmbeddedFiles,
						TEMP_FILE_CREATOR
					)
				)
				.thenThrow(new IOException(ERROR_IN_FILE1));

			assertThrows(
				IOException.class,
				() -> OsCommandHelper.runOsCommand(COMMAND_TO_UPDATE, telemetryManager, 120L, false, false)
			);
		}
	}

	@Test
	@EnabledOnOs(OS.WINDOWS)
	void testRunOsCommandWindowsError() {
		final SshConfiguration sshConfiguration = SshConfiguration
			.sshConfigurationBuilder()
			.username(USERNAME)
			.password(PWD_COMMAND.toCharArray())
			.build();

		final HostConfiguration hostConfiguration = HostConfiguration
			.builder()
			.hostId(ID)
			.hostname(HOST)
			.hostType(DeviceKind.WINDOWS)
			.configurations(Map.of(sshConfiguration.getClass(), sshConfiguration))
			.build();

		final TelemetryManager telemetryManager = TelemetryManager.builder().hostConfiguration(hostConfiguration).build();

		assertThrows(TimeoutException.class, () -> OsCommandHelper.runOsCommand(PAUSE, telemetryManager, 1L, false, true));
	}

	@Test
	@EnabledOnOs(OS.LINUX)
	void testRunOsCommandLinuxError() {
		final HostConfiguration hostConfiguration = HostConfiguration
			.builder()
			.hostId(ID)
			.hostname(HOST)
			.hostType(DeviceKind.LINUX)
			.build();

		final TelemetryManager telemetryManager = TelemetryManager.builder().hostConfiguration(hostConfiguration).build();

		assertThrows(
			TimeoutException.class,
			() -> OsCommandHelper.runOsCommand(SLEEP_5, telemetryManager, 1L, false, true)
		);
	}

	@Test
	@EnabledOnOs(OS.WINDOWS)
	void testRunOsCommandLocalWindows() throws Exception {
		final WmiConfiguration wmiConfiguration = new WmiConfiguration();
		wmiConfiguration.setUsername(USERNAME);
		wmiConfiguration.setPassword(PWD_COMMAND.toCharArray());

		final HostConfiguration hostConfiguration = HostConfiguration
			.builder()
			.hostId(ID)
			.hostname(HOST)
			.hostType(DeviceKind.WINDOWS)
			.configurations(Map.of(wmiConfiguration.getClass(), wmiConfiguration))
			.build();

		final TelemetryManager telemetryManager = TelemetryManager.builder().hostConfiguration(hostConfiguration).build();

		final OsCommandResult expect = new OsCommandResult(TEST_RESULT, ECHO_TEST_UPPER_CASE);

		assertEquals(expect, OsCommandHelper.runOsCommand(ECHO_TEST_UPPER_CASE, telemetryManager, 120L, false, true));
	}

	@Test
	@EnabledOnOs(OS.LINUX)
	void testRunOsCommandLocalLinux() throws Exception {
		final SshConfiguration sshConfiguration = SshConfiguration
			.sshConfigurationBuilder()
			.username(USERNAME)
			.password(PWD_COMMAND.toCharArray())
			.build();

		final HostConfiguration hostConfiguration = HostConfiguration
			.builder()
			.hostId(ID)
			.hostname(HOST)
			.hostType(DeviceKind.LINUX)
			.configurations(Map.of(sshConfiguration.getClass(), sshConfiguration))
			.build();

		final TelemetryManager telemetryManager = TelemetryManager.builder().hostConfiguration(hostConfiguration).build();

		final OsCommandResult expect = new OsCommandResult(TEST_RESULT, ECHO_TEST_LOWER_CASE);

		assertEquals(expect, OsCommandHelper.runOsCommand(ECHO_TEST_LOWER_CASE, telemetryManager, 120L, false, true));
	}

	@Test
	@EnabledOnOs(OS.WINDOWS)
	void testRunOsCommandRemoteExecutedLocallyWindows() throws Exception {
		final OsCommandResult expect = new OsCommandResult(TEST_RESULT, ECHO_TEST_UPPER_CASE);

		final HostConfiguration hostConfiguration = HostConfiguration
			.builder()
			.hostId(ID)
			.hostname(HOST)
			.hostType(DeviceKind.WINDOWS)
			.build();

		final TelemetryManager telemetryManager = TelemetryManager.builder().hostConfiguration(hostConfiguration).build();

		assertEquals(expect, OsCommandHelper.runOsCommand(ECHO_TEST_UPPER_CASE, telemetryManager, 120L, true, false));
	}

	@Test
	@EnabledOnOs(OS.LINUX)
	void testRunOsCommandRemoteExecutedLocallyLinux() throws Exception {
		final OsCommandResult expect = new OsCommandResult(TEST_RESULT, ECHO_TEST_LOWER_CASE);

		final HostConfiguration hostConfiguration = HostConfiguration
			.builder()
			.hostId(ID)
			.hostname(HOST)
			.hostType(DeviceKind.LINUX)
			.build();

		final TelemetryManager telemetryManager = TelemetryManager.builder().hostConfiguration(hostConfiguration).build();

		assertEquals(expect, OsCommandHelper.runOsCommand(ECHO_TEST_LOWER_CASE, telemetryManager, 120L, true, false));
	}

	@Test
	void testRunOsCommandRemoteWindows() throws Exception {
		commandLineEmbeddedFiles.put(EMBEDDED_FILE_1_REF, new EmbeddedFile(ECHO_OS, BAT, EMBEDDED_FILE_1_REF));
		commandLineEmbeddedFiles.put(EMBEDDED_FILE_2_REF, new EmbeddedFile(ECHO_HELLO_WORLD, null, EMBEDDED_FILE_2_REF));

		final File file1 = mock(File.class);
		final File file2 = mock(File.class);
		final Map<String, File> embeddedTempFiles = new HashMap<>();
		embeddedTempFiles.put(EMBEDDED_FILE_1_REF, file1);
		embeddedTempFiles.put(EMBEDDED_FILE_2_REF, file2);

		final WmiConfiguration wmiConfiguration = new WmiConfiguration();
		wmiConfiguration.setUsername(USERNAME);
		wmiConfiguration.setPassword(PWD_COMMAND.toCharArray());

		final HostConfiguration hostConfiguration = HostConfiguration
			.builder()
			.hostId(ID)
			.hostname(HOST)
			.hostType(DeviceKind.WINDOWS)
			.configurations(Map.of(wmiConfiguration.getClass(), wmiConfiguration))
			.build();

		final TelemetryManager telemetryManager = TelemetryManager.builder().hostConfiguration(hostConfiguration).build();

		try (
			final MockedStatic<OsCommandHelper> mockedOsCommandHelper = mockStatic(OsCommandHelper.class);
			final MockedStatic<MatsyaClientsExecutor> mockedMatsyaClientsExecutor = mockStatic(MatsyaClientsExecutor.class);
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
						COMMAND_TO_UPDATE,
						null,
						commandLineEmbeddedFiles,
						TEMP_FILE_CREATOR
					)
				)
				.thenReturn(embeddedTempFiles);

			doReturn(TEMP_EMBEDDED_1).when(file1).getAbsolutePath();
			doReturn(TEMP_EMBEDDED_2).when(file2).getAbsolutePath();

			mockedMatsyaClientsExecutor
				.when(() ->
					MatsyaClientsExecutor.executeWinRemoteCommand(eq(HOST), eq(wmiConfiguration), eq(UPDATED_COMMAND), any())
				)
				.thenReturn(WINDOWS_NT_HELLO_WORLD);

			mockedOsCommandHelper
				.when(() -> OsCommandHelper.runOsCommand(COMMAND_TO_UPDATE, telemetryManager, 120L, false, false))
				.thenCallRealMethod();

			final OsCommandResult expect = new OsCommandResult(WINDOWS_NT_HELLO_WORLD, UPDATED_COMMAND);

			final OsCommandResult res = OsCommandHelper.runOsCommand(COMMAND_TO_UPDATE, telemetryManager, 120L, false, false);
			assertEquals(expect, res);
		}
	}

	@Test
	void testRunOsCommandRemoteLinuxOSCommandConfigNull() throws Exception {
		final SshConfiguration sshConfiguration = SshConfiguration
			.sshConfigurationBuilder()
			.username(USERNAME)
			.password(PWD_COMMAND.toCharArray())
			.build();

		final HostConfiguration hostConfiguration = HostConfiguration
			.builder()
			.hostId(ID)
			.hostname(HOST)
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
						NAVISECCLI_COMMAND,
						null,
						commandLineEmbeddedFiles,
						TEMP_FILE_CREATOR
					)
				)
				.thenCallRealMethod();

			mockedOsCommandHelper
				.when(() -> {
					OsCommandHelper.runSshCommand(
						CLEAR_PASSWORD_COMMAND,
						HOST,
						sshConfiguration,
						120,
						Collections.emptyList(),
						NO_PASSWORD_COMMAND
					);
				})
				.thenReturn(AGENT_REV_RESULT);

			mockedOsCommandHelper
				.when(() -> OsCommandHelper.runOsCommand(NAVISECCLI_COMMAND, telemetryManager, 120L, false, false))
				.thenCallRealMethod();

			final OsCommandResult expect = new OsCommandResult(AGENT_REV_RESULT, NO_PASSWORD_COMMAND);

			assertEquals(expect, OsCommandHelper.runOsCommand(NAVISECCLI_COMMAND, telemetryManager, 120L, false, false));
		}
	}

	@Test
	void testRunOsCommandRemoteLinuxNoSudo() throws Exception {
		final SshConfiguration sshConfiguration = SshConfiguration
			.sshConfigurationBuilder()
			.username(USERNAME)
			.password(PWD_COMMAND.toCharArray())
			.build();

		final OsCommandConfiguration osCommandConfiguration = new OsCommandConfiguration();
		osCommandConfiguration.setUseSudoCommands(Collections.singleton(NAVISECCLI_CAMEL_CASE.toLowerCase()));

		final HostConfiguration hostConfiguration = HostConfiguration
			.builder()
			.hostId(ID)
			.hostname(HOST)
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
						NAVISECCLI_COMMAND,
						osCommandConfiguration,
						commandLineEmbeddedFiles,
						TEMP_FILE_CREATOR
					)
				)
				.thenCallRealMethod();

			mockedOsCommandHelper
				.when(() ->
					OsCommandHelper.runSshCommand(
						CLEAR_PASSWORD_COMMAND,
						HOST,
						sshConfiguration,
						120L,
						Collections.emptyList(),
						NO_PASSWORD_COMMAND
					)
				)
				.thenReturn(AGENT_REV_RESULT);

			mockedOsCommandHelper
				.when(() -> OsCommandHelper.runOsCommand(NAVISECCLI_COMMAND, telemetryManager, 120L, false, false))
				.thenCallRealMethod();

			final OsCommandResult expect = new OsCommandResult(AGENT_REV_RESULT, NO_PASSWORD_COMMAND);

			assertEquals(expect, OsCommandHelper.runOsCommand(NAVISECCLI_COMMAND, telemetryManager, 120L, false, false));
		}
	}

	@Test
	void testRunOsCommandRemoteLinuxNotInUseSudoCommands() throws Exception {
		final SshConfiguration sshConfiguration = SshConfiguration
			.sshConfigurationBuilder()
			.username(USERNAME)
			.password(PWD_COMMAND.toCharArray())
			.build();

		final OsCommandConfiguration osCommandConfiguration = new OsCommandConfiguration();
		osCommandConfiguration.setUseSudo(true);
		osCommandConfiguration.setUseSudoCommands(Collections.singleton(WMI_EXCEPTION_OTHER_MESSAGE));

		final HostConfiguration hostConfiguration = HostConfiguration
			.builder()
			.hostId(ID)
			.hostname(HOST)
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
						NAVISECCLI_COMMAND,
						osCommandConfiguration,
						commandLineEmbeddedFiles,
						TEMP_FILE_CREATOR
					)
				)
				.thenCallRealMethod();

			mockedOsCommandHelper
				.when(() ->
					OsCommandHelper.runSshCommand(
						CLEAR_PASSWORD_COMMAND,
						HOST,
						sshConfiguration,
						120,
						Collections.emptyList(),
						NO_PASSWORD_COMMAND
					)
				)
				.thenReturn(AGENT_REV_RESULT);

			mockedOsCommandHelper
				.when(() -> OsCommandHelper.runOsCommand(NAVISECCLI_COMMAND, telemetryManager, 120L, false, false))
				.thenCallRealMethod();

			final OsCommandResult expect = new OsCommandResult(AGENT_REV_RESULT, NO_PASSWORD_COMMAND);

			assertEquals(expect, OsCommandHelper.runOsCommand(NAVISECCLI_COMMAND, telemetryManager, 120L, false, false));
		}
	}

	@Test
	void testRunOsCommandRemoteLinuxWithSudoReplaced() throws Exception {
		final SshConfiguration sshConfiguration = SshConfiguration
			.sshConfigurationBuilder()
			.username(USERNAME)
			.password(PWD_COMMAND.toCharArray())
			.build();

		final OsCommandConfiguration osCommandConfiguration = new OsCommandConfiguration();
		osCommandConfiguration.setUseSudo(true);
		osCommandConfiguration.setUseSudoCommands(Collections.singleton(NAVISECCLI_CAMEL_CASE.toLowerCase()));

		final HostConfiguration hostConfiguration = HostConfiguration
			.builder()
			.hostId(ID)
			.hostname(HOST)
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
						NAVISECCLI_COMMAND,
						osCommandConfiguration,
						commandLineEmbeddedFiles,
						TEMP_FILE_CREATOR
					)
				)
				.thenCallRealMethod();

			mockedOsCommandHelper
				.when(() -> {
					OsCommandHelper.runSshCommand(
						SUDO_KEYWORD + CLEAR_PASSWORD_COMMAND,
						HOST,
						sshConfiguration,
						120,
						Collections.emptyList(),
						SUDO_KEYWORD + NO_PASSWORD_COMMAND
					);
				})
				.thenReturn(AGENT_REV_RESULT);

			mockedOsCommandHelper
				.when(() -> OsCommandHelper.runOsCommand(NAVISECCLI_COMMAND, telemetryManager, 120L, false, false))
				.thenCallRealMethod();

			final OsCommandResult expect = new OsCommandResult(AGENT_REV_RESULT, SUDO_KEYWORD + NO_PASSWORD_COMMAND);

			assertEquals(expect, OsCommandHelper.runOsCommand(NAVISECCLI_COMMAND, telemetryManager, 120L, false, false));
		}
	}

	@Test
	void testRunOsCommandRemoteLinuxWithEmbeddedFilesReplaced() throws Exception {
		commandLineEmbeddedFiles.put(
			EMBEDDED_FILE_1_REF,
			new EmbeddedFile(AWK_EMBEDDED_CONTENT_PERCENT_SUDO, null, EMBEDDED_FILE_1_REF)
		);

		final File localFile = mock(File.class);
		final Map<String, File> embeddedTempFiles = new HashMap<>();
		embeddedTempFiles.put(EMBEDDED_FILE_1_REF, localFile);

		final OsCommandConfiguration osCommandConfiguration = new OsCommandConfiguration();
		osCommandConfiguration.setUseSudo(true);
		osCommandConfiguration.setUseSudoCommands(Collections.singleton(ARCCONF_PATH));

		final SshConfiguration sshConfiguration = SshConfiguration
			.sshConfigurationBuilder()
			.username(USERNAME)
			.password(PWD_COMMAND.toCharArray())
			.build();

		final HostConfiguration hostConfiguration = HostConfiguration
			.builder()
			.hostId(ID)
			.hostname(HOST)
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
						SH_EMBEDDED_FILE_1,
						osCommandConfiguration,
						commandLineEmbeddedFiles,
						TEMP_FILE_CREATOR
					)
				)
				.thenReturn(embeddedTempFiles);

			mockedEmbeddedFileHelper
				.when(() -> EmbeddedFileHelper.findEmbeddedFiles(anyString()))
				.thenReturn(commandLineEmbeddedFiles);

			doReturn(SEN_EMBEDDED_0001_PATH).when(localFile).getAbsolutePath();

			mockedOsCommandHelper
				.when(() -> {
					OsCommandHelper.runSshCommand(
						SH_SEN_EMBEDDED_0001_PATH,
						HOST,
						sshConfiguration,
						120,
						List.of(localFile),
						SH_SEN_EMBEDDED_0001_PATH
					);
				})
				.thenReturn(HARD_DRIVE);

			mockedOsCommandHelper
				.when(() -> OsCommandHelper.runOsCommand(SH_EMBEDDED_FILE_1, telemetryManager, 120L, false, false))
				.thenCallRealMethod();

			final OsCommandResult expect = new OsCommandResult(HARD_DRIVE, SH_SEN_EMBEDDED_0001_PATH);
			final OsCommandResult actual = OsCommandHelper.runOsCommand(
				SH_EMBEDDED_FILE_1,
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
		Semaphore semaphore = SshSemaphoreFactory.getInstance().createOrGetSempahore(HOSTNAME);

		assertEquals(7, OsCommandHelper.runControlledSshCommand(semaphore::availablePermits, HOSTNAME, 30));

		assertDoesNotThrow(() -> OsCommandHelper.runControlledSshCommand(LocalDate.MIN::toString, HOSTNAME, 30));

		semaphore.acquire(8);
		assertThrows(
			ControlledSshException.class,
			() -> OsCommandHelper.runControlledSshCommand(LocalDate.MIN::toString, HOSTNAME, 1)
		);
	}
}
