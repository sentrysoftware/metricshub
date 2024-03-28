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
import static org.sentrysoftware.metricshub.engine.constants.Constants.AGENT_REV_RESULT;
import static org.sentrysoftware.metricshub.engine.constants.Constants.ARCCONF_PATH;
import static org.sentrysoftware.metricshub.engine.constants.Constants.AWK_EMBEDDED_CONTENT_PERCENT_SUDO;
import static org.sentrysoftware.metricshub.engine.constants.Constants.AWK_EMBEDDED_CONTENT_SUDO;
import static org.sentrysoftware.metricshub.engine.constants.Constants.BAT;
import static org.sentrysoftware.metricshub.engine.constants.Constants.BAT_FILE_EXTENSION;
import static org.sentrysoftware.metricshub.engine.constants.Constants.CLEAR_PASSWORD_COMMAND;
import static org.sentrysoftware.metricshub.engine.constants.Constants.CMD;
import static org.sentrysoftware.metricshub.engine.constants.Constants.CMD_COMMAND;
import static org.sentrysoftware.metricshub.engine.constants.Constants.COMMAND_TO_UPDATE;
import static org.sentrysoftware.metricshub.engine.constants.Constants.ECHO_HELLO_WORLD;
import static org.sentrysoftware.metricshub.engine.constants.Constants.ECHO_OS;
import static org.sentrysoftware.metricshub.engine.constants.Constants.ECHO_TEST_LOWER_CASE;
import static org.sentrysoftware.metricshub.engine.constants.Constants.ECHO_TEST_UPPER_CASE;
import static org.sentrysoftware.metricshub.engine.constants.Constants.EMBEDDED_FILE_1_COPY_COMMAND_LINE;
import static org.sentrysoftware.metricshub.engine.constants.Constants.EMBEDDED_FILE_1_REF;
import static org.sentrysoftware.metricshub.engine.constants.Constants.EMBEDDED_FILE_2_REF;
import static org.sentrysoftware.metricshub.engine.constants.Constants.EMBEDDED_TEMP_FILE_PREFIX;
import static org.sentrysoftware.metricshub.engine.constants.Constants.EMPTY;
import static org.sentrysoftware.metricshub.engine.constants.Constants.END_OF_LINE;
import static org.sentrysoftware.metricshub.engine.constants.Constants.END_OF_LINE_IN_BRACKETS;
import static org.sentrysoftware.metricshub.engine.constants.Constants.ERROR_IN_FILE1;
import static org.sentrysoftware.metricshub.engine.constants.Constants.HARD_DRIVE;
import static org.sentrysoftware.metricshub.engine.constants.Constants.HOST;
import static org.sentrysoftware.metricshub.engine.constants.Constants.HOSTNAME;
import static org.sentrysoftware.metricshub.engine.constants.Constants.HOSTNAME_MACRO;
import static org.sentrysoftware.metricshub.engine.constants.Constants.HOST_CAMEL_CASE;
import static org.sentrysoftware.metricshub.engine.constants.Constants.ID;
import static org.sentrysoftware.metricshub.engine.constants.Constants.KEY;
import static org.sentrysoftware.metricshub.engine.constants.Constants.NAVISECCLI_CAMEL_CASE;
import static org.sentrysoftware.metricshub.engine.constants.Constants.NAVISECCLI_COMMAND;
import static org.sentrysoftware.metricshub.engine.constants.Constants.NO_PASSWORD_COMMAND;
import static org.sentrysoftware.metricshub.engine.constants.Constants.PASSWORD;
import static org.sentrysoftware.metricshub.engine.constants.Constants.PAUSE;
import static org.sentrysoftware.metricshub.engine.constants.Constants.PERCENT_USERNAME;
import static org.sentrysoftware.metricshub.engine.constants.Constants.PWD_COMMAND;
import static org.sentrysoftware.metricshub.engine.constants.Constants.Q_HOST;
import static org.sentrysoftware.metricshub.engine.constants.Constants.Q_HOSTNAME;
import static org.sentrysoftware.metricshub.engine.constants.Constants.Q_USERNAME;
import static org.sentrysoftware.metricshub.engine.constants.Constants.RAIDCTL_COMMAND;
import static org.sentrysoftware.metricshub.engine.constants.Constants.RAIDCTL_PATH;
import static org.sentrysoftware.metricshub.engine.constants.Constants.RESULT;
import static org.sentrysoftware.metricshub.engine.constants.Constants.SEN_EMBEDDED_0001_PATH;
import static org.sentrysoftware.metricshub.engine.constants.Constants.SH_EMBEDDED_FILE_1;
import static org.sentrysoftware.metricshub.engine.constants.Constants.SH_SEN_EMBEDDED_0001_PATH;
import static org.sentrysoftware.metricshub.engine.constants.Constants.SINGLE_SPACE;
import static org.sentrysoftware.metricshub.engine.constants.Constants.SLEEP_5;
import static org.sentrysoftware.metricshub.engine.constants.Constants.SPACE_KEY;
import static org.sentrysoftware.metricshub.engine.constants.Constants.SUDO_KEY;
import static org.sentrysoftware.metricshub.engine.constants.Constants.SUDO_KEYWORD;
import static org.sentrysoftware.metricshub.engine.constants.Constants.SUDO_KEY_RESULT;
import static org.sentrysoftware.metricshub.engine.constants.Constants.SUDO_NAVISECCLI_COMMAND;
import static org.sentrysoftware.metricshub.engine.constants.Constants.SUDO_RAIDCTL_COMMAND;
import static org.sentrysoftware.metricshub.engine.constants.Constants.TEMP_EMBEDDED_1;
import static org.sentrysoftware.metricshub.engine.constants.Constants.TEMP_EMBEDDED_2;
import static org.sentrysoftware.metricshub.engine.constants.Constants.TEST_RESULT;
import static org.sentrysoftware.metricshub.engine.constants.Constants.TEXT;
import static org.sentrysoftware.metricshub.engine.constants.Constants.UPDATED_COMMAND;
import static org.sentrysoftware.metricshub.engine.constants.Constants.USERNAME;
import static org.sentrysoftware.metricshub.engine.constants.Constants.WINDOWS_NT_HELLO_WORLD;
import static org.sentrysoftware.metricshub.engine.constants.Constants.WMI_EXCEPTION_OTHER_MESSAGE;
import static org.sentrysoftware.metricshub.engine.strategy.utils.CommandLineHelper.TEMP_FILE_CREATOR;

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
import org.sentrysoftware.metricshub.engine.client.ClientsExecutor;
import org.sentrysoftware.metricshub.engine.common.exception.ControlledSshException;
import org.sentrysoftware.metricshub.engine.common.exception.NoCredentialProvidedException;
import org.sentrysoftware.metricshub.engine.common.helpers.LocalOsHandler;
import org.sentrysoftware.metricshub.engine.configuration.CommandLineConfiguration;
import org.sentrysoftware.metricshub.engine.configuration.HostConfiguration;
import org.sentrysoftware.metricshub.engine.configuration.SshConfiguration;
import org.sentrysoftware.metricshub.engine.configuration.WmiConfiguration;
import org.sentrysoftware.metricshub.engine.connector.model.common.DeviceKind;
import org.sentrysoftware.metricshub.engine.connector.model.common.EmbeddedFile;
import org.sentrysoftware.metricshub.engine.telemetry.SshSemaphoreFactory;
import org.sentrysoftware.metricshub.engine.telemetry.TelemetryManager;

@ExtendWith(MockitoExtension.class)
class CommandLineHelperTest {

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
					throw new CommandLineHelper.TempFileCreationException(e);
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
	void testCreateCommandLineEmbeddedFiles() throws Exception {
		checkNoTempEmbeddedFileExist();

		assertThrows(
			IllegalArgumentException.class,
			() -> CommandLineHelper.createCommandLineEmbeddedFiles(null, null, EMPTY_EMBEDDED_FILE_MAP, jUnitTempFileCreator)
		);

		// Embedded files are referenced in the command line but cannot be found
		assertThrows(
			IllegalStateException.class,
			() ->
				CommandLineHelper.createCommandLineEmbeddedFiles(
					EMBEDDED_FILE_1_COPY_COMMAND_LINE,
					null,
					EMPTY_EMBEDDED_FILE_MAP,
					jUnitTempFileCreator
				)
		);

		assertEquals(
			Collections.emptyMap(),
			CommandLineHelper.createCommandLineEmbeddedFiles(CMD, null, EMPTY_EMBEDDED_FILE_MAP, jUnitTempFileCreator)
		);

		// case embeddedFile not found
		commandLineEmbeddedFiles.put(EMBEDDED_FILE_1_REF, new EmbeddedFile());
		assertThrows(
			IllegalStateException.class,
			() ->
				CommandLineHelper.createCommandLineEmbeddedFiles(
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
				CommandLineHelper.createCommandLineEmbeddedFiles(
					EMBEDDED_FILE_1_COPY_COMMAND_LINE,
					null,
					commandLineEmbeddedFiles,
					jUnitTempFileCreator
				)
		);

		checkNoTempEmbeddedFileExist();

		// case IOException in temp file creation
		try (final MockedStatic<CommandLineHelper> mockedCommandLineHelper = mockStatic(CommandLineHelper.class)) {
			commandLineEmbeddedFiles.put(EMBEDDED_FILE_1_REF, new EmbeddedFile(ECHO_OS, BAT, EMBEDDED_FILE_1_REF));
			commandLineEmbeddedFiles.put(EMBEDDED_FILE_2_REF, new EmbeddedFile(ECHO_HELLO_WORLD, null, EMBEDDED_FILE_2_REF));

			mockedCommandLineHelper
				.when(() -> CommandLineHelper.createTempFileWithEmbeddedFileContent(any(EmbeddedFile.class), isNull(), any()))
				.thenThrow(IOException.class);

			mockedCommandLineHelper
				.when(() ->
					CommandLineHelper.createCommandLineEmbeddedFiles(
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
					CommandLineHelper.createCommandLineEmbeddedFiles(
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

			final Map<String, File> embeddedTempFiles = CommandLineHelper.createCommandLineEmbeddedFiles(
				EMBEDDED_FILE_1_COPY_COMMAND_LINE,
				CommandLineConfiguration.builder().useSudo(true).useSudoCommands(Set.of(ARCCONF_PATH)).build(),
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
		assertNull(CommandLineHelper.replaceSudo(null, null));
		assertNull(CommandLineHelper.replaceSudo(null, CommandLineConfiguration.builder().build()));

		assertEquals(EMPTY, CommandLineHelper.replaceSudo(EMPTY, null));
		assertEquals(EMPTY, CommandLineHelper.replaceSudo(EMPTY, CommandLineConfiguration.builder().build()));
		assertEquals(SINGLE_SPACE, CommandLineHelper.replaceSudo(SINGLE_SPACE, null));
		assertEquals(SINGLE_SPACE, CommandLineHelper.replaceSudo(SINGLE_SPACE, CommandLineConfiguration.builder().build()));

		assertEquals(TEXT, CommandLineHelper.replaceSudo(TEXT, null));
		assertEquals(TEXT, CommandLineHelper.replaceSudo(TEXT, CommandLineConfiguration.builder().build()));

		// Check replace sudo tag with empty string.
		assertEquals(SPACE_KEY, CommandLineHelper.replaceSudo(SUDO_KEY, null));
		assertEquals(SPACE_KEY, CommandLineHelper.replaceSudo(SUDO_KEY, CommandLineConfiguration.builder().build()));
		assertEquals(
			SPACE_KEY,
			CommandLineHelper.replaceSudo(SUDO_KEY, CommandLineConfiguration.builder().useSudo(true).build())
		);
		assertEquals(
			SPACE_KEY + END_OF_LINE + SPACE_KEY,
			CommandLineHelper.replaceSudo(
				SUDO_KEY + END_OF_LINE + SUDO_KEY,
				CommandLineConfiguration.builder().useSudo(true).build()
			)
		);

		assertEquals(
			SUDO_KEYWORD + SPACE_KEY,
			CommandLineHelper.replaceSudo(
				SUDO_KEY,
				CommandLineConfiguration.builder().useSudo(true).useSudoCommands(Set.of(KEY)).build()
			)
		);

		assertEquals(
			SUDO_KEY_RESULT,
			CommandLineHelper.replaceSudo(
				SUDO_KEY + END_OF_LINE + SUDO_KEY,
				CommandLineConfiguration.builder().useSudo(true).useSudoCommands(Set.of(KEY)).build()
			)
		);
	}

	@Test
	void testRunLocalCommandKO() throws Exception {
		assertThrows(IllegalArgumentException.class, () -> CommandLineHelper.runLocalCommand(null, 1, null));
		assertThrows(IllegalArgumentException.class, () -> CommandLineHelper.runLocalCommand(CMD, -1, null));
		assertThrows(IllegalArgumentException.class, () -> CommandLineHelper.runLocalCommand(CMD, 0, null));

		// case Process null Linux
		final Runtime runtime = mock(Runtime.class);
		try (
			final MockedStatic<LocalOsHandler> mockedLocalOSHandler = mockStatic(LocalOsHandler.class);
			final MockedStatic<Runtime> mockedRuntime = mockStatic(Runtime.class)
		) {
			mockedLocalOSHandler.when(LocalOsHandler::isWindows).thenReturn(false);
			mockedRuntime.when(Runtime::getRuntime).thenReturn(runtime);
			when(runtime.exec(CMD)).thenReturn(null);

			assertThrows(IllegalStateException.class, () -> CommandLineHelper.runLocalCommand(CMD, 1, null));
		}

		// case Process null Windows
		try (
			final MockedStatic<LocalOsHandler> mockedLocalOSHandler = mockStatic(LocalOsHandler.class);
			final MockedStatic<Runtime> mockedRuntime = mockStatic(Runtime.class)
		) {
			mockedLocalOSHandler.when(LocalOsHandler::isWindows).thenReturn(true);
			mockedRuntime.when(Runtime::getRuntime).thenReturn(runtime);
			when(runtime.exec(CMD_COMMAND)).thenReturn(null);

			assertThrows(IllegalStateException.class, () -> CommandLineHelper.runLocalCommand(CMD, 1, null));
		}
	}

	@Test
	@EnabledOnOs(OS.WINDOWS)
	void testRunLocalCommandWindowsTimeout() throws Exception {
		assertThrows(TimeoutException.class, () -> CommandLineHelper.runLocalCommand(PAUSE, 1, null));
	}

	@Test
	@EnabledOnOs(OS.LINUX)
	void testRunLocalCommandLinuxTimeout() throws Exception {
		assertThrows(TimeoutException.class, () -> CommandLineHelper.runLocalCommand(SLEEP_5, 1, null));
	}

	@Test
	@EnabledOnOs(OS.WINDOWS)
	void testRunLocalCommandWindows() throws Exception {
		assertEquals(TEST_RESULT, CommandLineHelper.runLocalCommand(ECHO_TEST_UPPER_CASE, 1, null));
	}

	@Test
	@EnabledOnOs(OS.LINUX)
	void testRunLocalCommandLinux() throws Exception {
		assertEquals(TEST_RESULT, CommandLineHelper.runLocalCommand(ECHO_TEST_LOWER_CASE, 1, null));
	}

	@Test
	void testRunSshCommand() throws Exception {
		final SshConfiguration sshConfiguration = mock(SshConfiguration.class);
		final int timeout = 1000;

		assertThrows(
			IllegalArgumentException.class,
			() -> CommandLineHelper.runSshCommand(null, HOST, sshConfiguration, timeout, null, null)
		);
		assertThrows(
			IllegalArgumentException.class,
			() -> CommandLineHelper.runSshCommand(CMD, null, sshConfiguration, timeout, null, null)
		);
		assertThrows(
			IllegalArgumentException.class,
			() -> CommandLineHelper.runSshCommand(CMD, HOST, null, timeout, null, null)
		);
		assertThrows(
			IllegalArgumentException.class,
			() -> CommandLineHelper.runSshCommand(CMD, HOST, sshConfiguration, -1, null, null)
		);
		assertThrows(
			IllegalArgumentException.class,
			() -> CommandLineHelper.runSshCommand(CMD, HOST, sshConfiguration, 0, null, null)
		);

		try (final MockedStatic<ClientsExecutor> mockedClientsExecutor = mockStatic(ClientsExecutor.class)) {
			when(sshConfiguration.getUsername()).thenReturn(USERNAME);
			when(sshConfiguration.getPassword()).thenReturn(PASSWORD.toCharArray());

			mockedClientsExecutor
				.when(() ->
					ClientsExecutor.runRemoteSshCommand(HOST, USERNAME, PASSWORD.toCharArray(), null, CMD, timeout, null, null)
				)
				.thenReturn(RESULT);

			assertEquals(RESULT, CommandLineHelper.runSshCommand(CMD, HOST, sshConfiguration, timeout, null, null));
		}
	}

	@Test
	void testGetFileNameFromSudoCommand() {
		assertThrows(IllegalArgumentException.class, () -> CommandLineHelper.getFileNameFromSudoCommand(null));
		assertEquals(Optional.empty(), CommandLineHelper.getFileNameFromSudoCommand(RAIDCTL_COMMAND));
		assertEquals(Optional.of(RAIDCTL_PATH), CommandLineHelper.getFileNameFromSudoCommand(SUDO_RAIDCTL_COMMAND));
		assertEquals(
			Optional.of(NAVISECCLI_CAMEL_CASE),
			CommandLineHelper.getFileNameFromSudoCommand(SUDO_NAVISECCLI_COMMAND)
		);
	}

	@Test
	void testToCaseInsensitiveRegex() {
		assertThrows(IllegalArgumentException.class, () -> CommandLineHelper.toCaseInsensitiveRegex(null));
		assertThrows(IllegalArgumentException.class, () -> CommandLineHelper.toCaseInsensitiveRegex(EMPTY));
		assertEquals(SINGLE_SPACE, CommandLineHelper.toCaseInsensitiveRegex(SINGLE_SPACE));
		assertEquals(Q_HOST, CommandLineHelper.toCaseInsensitiveRegex(HOST_CAMEL_CASE));
		assertEquals(Q_USERNAME, CommandLineHelper.toCaseInsensitiveRegex(PERCENT_USERNAME));
		assertEquals(Q_HOSTNAME, CommandLineHelper.toCaseInsensitiveRegex(HOSTNAME_MACRO));
	}

	@Test
	void testGetTimeout() {
		final CommandLineConfiguration commandLineConfig = new CommandLineConfiguration();
		commandLineConfig.setTimeout(2L);

		final WmiConfiguration wmiConfiguration = new WmiConfiguration();
		wmiConfiguration.setTimeout(3L);

		final SshConfiguration sshConfiguration = SshConfiguration
			.sshConfigurationBuilder()
			.username(USERNAME)
			.password(PASSWORD.toCharArray())
			.build();
		sshConfiguration.setTimeout(4L);

		assertEquals(1, CommandLineHelper.getTimeout(1L, commandLineConfig, sshConfiguration, 5));
		assertEquals(2, CommandLineHelper.getTimeout(null, commandLineConfig, sshConfiguration, 5));
		assertEquals(3, CommandLineHelper.getTimeout(null, null, wmiConfiguration, 5));
		assertEquals(4, CommandLineHelper.getTimeout(null, null, sshConfiguration, 5));
		assertEquals(5, CommandLineHelper.getTimeout(null, null, null, 5));
		assertEquals(30, CommandLineHelper.getTimeout(null, new CommandLineConfiguration(), sshConfiguration, 5));
		assertEquals(120, CommandLineHelper.getTimeout(null, null, new WmiConfiguration(), 5));
		assertEquals(
			30,
			CommandLineHelper.getTimeout(
				null,
				null,
				SshConfiguration.sshConfigurationBuilder().username(USERNAME).password(PASSWORD.toCharArray()).build(),
				5
			)
		);
	}

	@Test
	void testGetUsername() {
		assertEquals(Optional.empty(), CommandLineHelper.getUsername(null));
		assertEquals(Optional.empty(), CommandLineHelper.getUsername(new CommandLineConfiguration()));
		assertEquals(Optional.empty(), CommandLineHelper.getUsername(new WmiConfiguration()));
		assertEquals(
			Optional.empty(),
			CommandLineHelper.getUsername(SshConfiguration.sshConfigurationBuilder().password(PASSWORD.toCharArray()).build())
		);

		final WmiConfiguration wmiConfiguration = new WmiConfiguration();
		wmiConfiguration.setUsername(USERNAME);
		assertEquals(Optional.of(USERNAME), CommandLineHelper.getUsername(wmiConfiguration));

		assertEquals(
			Optional.of(USERNAME),
			CommandLineHelper.getUsername(
				SshConfiguration.sshConfigurationBuilder().username(USERNAME).password(PASSWORD.toCharArray()).build()
			)
		);
	}

	@Test
	void testGetPassword() {
		assertEquals(Optional.empty(), CommandLineHelper.getPassword(null));
		assertEquals(Optional.empty(), CommandLineHelper.getPassword(new CommandLineConfiguration()));
		assertEquals(Optional.empty(), CommandLineHelper.getPassword(new WmiConfiguration()));
		assertEquals(
			Optional.empty(),
			CommandLineHelper.getPassword(SshConfiguration.sshConfigurationBuilder().username(USERNAME).build())
		);

		final WmiConfiguration wmiConfiguration = new WmiConfiguration();
		char[] charArrayPassword = PASSWORD.toCharArray();
		wmiConfiguration.setPassword(charArrayPassword);
		assertEquals(Optional.of(charArrayPassword), CommandLineHelper.getPassword(wmiConfiguration));

		assertEquals(
			Optional.of(charArrayPassword),
			CommandLineHelper.getPassword(
				SshConfiguration.sshConfigurationBuilder().username(USERNAME).password(charArrayPassword).build()
			)
		);
	}

	@Test
	void testRunCommandLineCommandLineNull() {
		final TelemetryManager telemetryManager = TelemetryManager.builder().build();
		assertThrows(
			IllegalArgumentException.class,
			() -> CommandLineHelper.runCommandLine(null, telemetryManager, 120L, false, false)
		);
	}

	@Test
	void testRunCommandLineTelemetryManagerNull() {
		assertThrows(IllegalArgumentException.class, () -> CommandLineHelper.runCommandLine(CMD, null, 120L, false, false));
	}

	@Test
	void testRunCommandLineRemoteNoUser() {
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
			() -> CommandLineHelper.runCommandLine(NAVISECCLI_COMMAND, telemetryManager, 120L, false, false)
		);
	}

	@Test
	void testRunCommandLineRemoteWindowsEmbeddedFilesError() {
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

		try (
			final MockedStatic<CommandLineHelper> mockedCommandLineHelper = mockStatic(CommandLineHelper.class);
			final MockedStatic<EmbeddedFileHelper> mockedEmbeddedFileHelper = mockStatic(EmbeddedFileHelper.class)
		) {
			mockedCommandLineHelper.when(() -> CommandLineHelper.getUsername(wmiConfiguration)).thenCallRealMethod();
			mockedCommandLineHelper
				.when(() -> CommandLineHelper.runCommandLine(COMMAND_TO_UPDATE, telemetryManager, 120L, false, false))
				.thenCallRealMethod();

			mockedEmbeddedFileHelper
				.when(() -> EmbeddedFileHelper.findEmbeddedFiles(anyString()))
				.thenReturn(commandLineEmbeddedFiles);

			mockedCommandLineHelper
				.when(() ->
					CommandLineHelper.createCommandLineEmbeddedFiles(
						COMMAND_TO_UPDATE,
						null,
						commandLineEmbeddedFiles,
						TEMP_FILE_CREATOR
					)
				)
				.thenThrow(new IOException(ERROR_IN_FILE1));

			assertThrows(
				IOException.class,
				() -> CommandLineHelper.runCommandLine(COMMAND_TO_UPDATE, telemetryManager, 120L, false, false)
			);
		}
	}

	@Test
	@EnabledOnOs(OS.WINDOWS)
	void testRunCommandLineWindowsError() {
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

		assertThrows(
			TimeoutException.class,
			() -> CommandLineHelper.runCommandLine(PAUSE, telemetryManager, 1L, false, true)
		);
	}

	@Test
	@EnabledOnOs(OS.LINUX)
	void testRunCommandLineLinuxError() {
		final HostConfiguration hostConfiguration = HostConfiguration
			.builder()
			.hostId(ID)
			.hostname(HOST)
			.hostType(DeviceKind.LINUX)
			.build();

		final TelemetryManager telemetryManager = TelemetryManager.builder().hostConfiguration(hostConfiguration).build();

		assertThrows(
			TimeoutException.class,
			() -> CommandLineHelper.runCommandLine(SLEEP_5, telemetryManager, 1L, false, true)
		);
	}

	@Test
	@EnabledOnOs(OS.WINDOWS)
	void testRunCommandLineLocalWindows() throws Exception {
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

		final CommandLineResult expect = new CommandLineResult(TEST_RESULT, ECHO_TEST_UPPER_CASE);

		assertEquals(expect, CommandLineHelper.runCommandLine(ECHO_TEST_UPPER_CASE, telemetryManager, 120L, false, true));
	}

	@Test
	@EnabledOnOs(OS.LINUX)
	void testRunCommandLineLocalLinux() throws Exception {
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

		final CommandLineResult expect = new CommandLineResult(TEST_RESULT, ECHO_TEST_LOWER_CASE);

		assertEquals(expect, CommandLineHelper.runCommandLine(ECHO_TEST_LOWER_CASE, telemetryManager, 120L, false, true));
	}

	@Test
	@EnabledOnOs(OS.WINDOWS)
	void testRunCommandLineRemoteExecutedLocallyWindows() throws Exception {
		final CommandLineResult expect = new CommandLineResult(TEST_RESULT, ECHO_TEST_UPPER_CASE);

		final HostConfiguration hostConfiguration = HostConfiguration
			.builder()
			.hostId(ID)
			.hostname(HOST)
			.hostType(DeviceKind.WINDOWS)
			.build();

		final TelemetryManager telemetryManager = TelemetryManager.builder().hostConfiguration(hostConfiguration).build();

		assertEquals(expect, CommandLineHelper.runCommandLine(ECHO_TEST_UPPER_CASE, telemetryManager, 120L, true, false));
	}

	@Test
	@EnabledOnOs(OS.LINUX)
	void testRunCommandLineRemoteExecutedLocallyLinux() throws Exception {
		final CommandLineResult expect = new CommandLineResult(TEST_RESULT, ECHO_TEST_LOWER_CASE);

		final HostConfiguration hostConfiguration = HostConfiguration
			.builder()
			.hostId(ID)
			.hostname(HOST)
			.hostType(DeviceKind.LINUX)
			.build();

		final TelemetryManager telemetryManager = TelemetryManager.builder().hostConfiguration(hostConfiguration).build();

		assertEquals(expect, CommandLineHelper.runCommandLine(ECHO_TEST_LOWER_CASE, telemetryManager, 120L, true, false));
	}

	@Test
	void testRunCommandLineRemoteWindows() throws Exception {
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
			final MockedStatic<CommandLineHelper> mockedCommandLineHelper = mockStatic(CommandLineHelper.class);
			final MockedStatic<ClientsExecutor> mockedClientsExecutor = mockStatic(ClientsExecutor.class);
			final MockedStatic<EmbeddedFileHelper> mockedEmbeddedFileHelper = mockStatic(EmbeddedFileHelper.class)
		) {
			mockedCommandLineHelper.when(() -> CommandLineHelper.getUsername(wmiConfiguration)).thenCallRealMethod();
			mockedCommandLineHelper
				.when(() -> CommandLineHelper.getTimeout(120L, null, wmiConfiguration, 300))
				.thenCallRealMethod();
			mockedCommandLineHelper.when(() -> CommandLineHelper.getPassword(wmiConfiguration)).thenCallRealMethod();
			mockedCommandLineHelper.when(() -> CommandLineHelper.replaceSudo(anyString(), isNull())).thenCallRealMethod();
			mockedCommandLineHelper
				.when(() -> CommandLineHelper.getFileNameFromSudoCommand(anyString()))
				.thenCallRealMethod();
			mockedCommandLineHelper.when(() -> CommandLineHelper.toCaseInsensitiveRegex(anyString())).thenCallRealMethod();
			mockedEmbeddedFileHelper
				.when(() -> EmbeddedFileHelper.findEmbeddedFiles(anyString()))
				.thenReturn(commandLineEmbeddedFiles);

			mockedCommandLineHelper
				.when(() ->
					CommandLineHelper.createCommandLineEmbeddedFiles(
						COMMAND_TO_UPDATE,
						null,
						commandLineEmbeddedFiles,
						TEMP_FILE_CREATOR
					)
				)
				.thenReturn(embeddedTempFiles);

			doReturn(TEMP_EMBEDDED_1).when(file1).getAbsolutePath();
			doReturn(TEMP_EMBEDDED_2).when(file2).getAbsolutePath();

			mockedClientsExecutor
				.when(() -> ClientsExecutor.executeWinRemoteCommand(eq(HOST), eq(wmiConfiguration), eq(UPDATED_COMMAND), any()))
				.thenReturn(WINDOWS_NT_HELLO_WORLD);

			mockedCommandLineHelper
				.when(() -> CommandLineHelper.runCommandLine(COMMAND_TO_UPDATE, telemetryManager, 120L, false, false))
				.thenCallRealMethod();

			final CommandLineResult expect = new CommandLineResult(WINDOWS_NT_HELLO_WORLD, UPDATED_COMMAND);

			final CommandLineResult res = CommandLineHelper.runCommandLine(
				COMMAND_TO_UPDATE,
				telemetryManager,
				120L,
				false,
				false
			);
			assertEquals(expect, res);
		}
	}

	@Test
	void testRunCommandLineRemoteLinuxCommandLineConfigNull() throws Exception {
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

		try (final MockedStatic<CommandLineHelper> mockedCommandLineHelper = mockStatic(CommandLineHelper.class)) {
			mockedCommandLineHelper.when(() -> CommandLineHelper.getUsername(sshConfiguration)).thenCallRealMethod();
			mockedCommandLineHelper
				.when(() -> CommandLineHelper.getTimeout(120L, null, sshConfiguration, 300))
				.thenCallRealMethod();
			mockedCommandLineHelper.when(() -> CommandLineHelper.getPassword(sshConfiguration)).thenCallRealMethod();
			mockedCommandLineHelper.when(() -> CommandLineHelper.replaceSudo(anyString(), isNull())).thenCallRealMethod();
			mockedCommandLineHelper
				.when(() -> CommandLineHelper.getFileNameFromSudoCommand(anyString()))
				.thenCallRealMethod();
			mockedCommandLineHelper.when(() -> CommandLineHelper.toCaseInsensitiveRegex(anyString())).thenCallRealMethod();
			mockedCommandLineHelper
				.when(() ->
					CommandLineHelper.createCommandLineEmbeddedFiles(
						NAVISECCLI_COMMAND,
						null,
						commandLineEmbeddedFiles,
						TEMP_FILE_CREATOR
					)
				)
				.thenCallRealMethod();

			mockedCommandLineHelper
				.when(() -> {
					CommandLineHelper.runSshCommand(
						CLEAR_PASSWORD_COMMAND,
						HOST,
						sshConfiguration,
						120,
						Collections.emptyList(),
						NO_PASSWORD_COMMAND
					);
				})
				.thenReturn(AGENT_REV_RESULT);

			mockedCommandLineHelper
				.when(() -> CommandLineHelper.runCommandLine(NAVISECCLI_COMMAND, telemetryManager, 120L, false, false))
				.thenCallRealMethod();

			final CommandLineResult expect = new CommandLineResult(AGENT_REV_RESULT, NO_PASSWORD_COMMAND);

			assertEquals(expect, CommandLineHelper.runCommandLine(NAVISECCLI_COMMAND, telemetryManager, 120L, false, false));
		}
	}

	@Test
	void testRunCommandLineRemoteLinuxNoSudo() throws Exception {
		final SshConfiguration sshConfiguration = SshConfiguration
			.sshConfigurationBuilder()
			.username(USERNAME)
			.password(PWD_COMMAND.toCharArray())
			.build();

		final CommandLineConfiguration commandLineConfiguration = new CommandLineConfiguration();
		commandLineConfiguration.setUseSudoCommands(Collections.singleton(NAVISECCLI_CAMEL_CASE.toLowerCase()));

		final HostConfiguration hostConfiguration = HostConfiguration
			.builder()
			.hostId(ID)
			.hostname(HOST)
			.hostType(DeviceKind.LINUX)
			.configurations(
				Map.of(
					sshConfiguration.getClass(),
					sshConfiguration,
					commandLineConfiguration.getClass(),
					commandLineConfiguration
				)
			)
			.build();

		final TelemetryManager telemetryManager = TelemetryManager.builder().hostConfiguration(hostConfiguration).build();

		try (final MockedStatic<CommandLineHelper> mockedCommandLineHelper = mockStatic(CommandLineHelper.class)) {
			mockedCommandLineHelper.when(() -> CommandLineHelper.getUsername(sshConfiguration)).thenCallRealMethod();
			mockedCommandLineHelper
				.when(() -> CommandLineHelper.getTimeout(120L, commandLineConfiguration, sshConfiguration, 300))
				.thenCallRealMethod();
			mockedCommandLineHelper.when(() -> CommandLineHelper.getPassword(sshConfiguration)).thenCallRealMethod();
			mockedCommandLineHelper
				.when(() -> CommandLineHelper.replaceSudo(anyString(), eq(commandLineConfiguration)))
				.thenCallRealMethod();
			mockedCommandLineHelper
				.when(() -> CommandLineHelper.getFileNameFromSudoCommand(anyString()))
				.thenCallRealMethod();
			mockedCommandLineHelper.when(() -> CommandLineHelper.toCaseInsensitiveRegex(anyString())).thenCallRealMethod();
			mockedCommandLineHelper
				.when(() ->
					CommandLineHelper.createCommandLineEmbeddedFiles(
						NAVISECCLI_COMMAND,
						commandLineConfiguration,
						commandLineEmbeddedFiles,
						TEMP_FILE_CREATOR
					)
				)
				.thenCallRealMethod();

			mockedCommandLineHelper
				.when(() ->
					CommandLineHelper.runSshCommand(
						CLEAR_PASSWORD_COMMAND,
						HOST,
						sshConfiguration,
						120L,
						Collections.emptyList(),
						NO_PASSWORD_COMMAND
					)
				)
				.thenReturn(AGENT_REV_RESULT);

			mockedCommandLineHelper
				.when(() -> CommandLineHelper.runCommandLine(NAVISECCLI_COMMAND, telemetryManager, 120L, false, false))
				.thenCallRealMethod();

			final CommandLineResult expect = new CommandLineResult(AGENT_REV_RESULT, NO_PASSWORD_COMMAND);

			assertEquals(expect, CommandLineHelper.runCommandLine(NAVISECCLI_COMMAND, telemetryManager, 120L, false, false));
		}
	}

	@Test
	void testRunCommandLineRemoteLinuxNotInUseSudoCommands() throws Exception {
		final SshConfiguration sshConfiguration = SshConfiguration
			.sshConfigurationBuilder()
			.username(USERNAME)
			.password(PWD_COMMAND.toCharArray())
			.build();

		final CommandLineConfiguration commandLineConfiguration = new CommandLineConfiguration();
		commandLineConfiguration.setUseSudo(true);
		commandLineConfiguration.setUseSudoCommands(Collections.singleton(WMI_EXCEPTION_OTHER_MESSAGE));

		final HostConfiguration hostConfiguration = HostConfiguration
			.builder()
			.hostId(ID)
			.hostname(HOST)
			.hostType(DeviceKind.LINUX)
			.configurations(
				Map.of(
					sshConfiguration.getClass(),
					sshConfiguration,
					commandLineConfiguration.getClass(),
					commandLineConfiguration
				)
			)
			.build();

		final TelemetryManager telemetryManager = TelemetryManager.builder().hostConfiguration(hostConfiguration).build();

		try (final MockedStatic<CommandLineHelper> mockedCommandLineHelper = mockStatic(CommandLineHelper.class)) {
			mockedCommandLineHelper.when(() -> CommandLineHelper.getUsername(sshConfiguration)).thenCallRealMethod();
			mockedCommandLineHelper
				.when(() -> CommandLineHelper.getTimeout(120L, commandLineConfiguration, sshConfiguration, 300))
				.thenCallRealMethod();
			mockedCommandLineHelper.when(() -> CommandLineHelper.getPassword(sshConfiguration)).thenCallRealMethod();
			mockedCommandLineHelper
				.when(() -> CommandLineHelper.replaceSudo(anyString(), eq(commandLineConfiguration)))
				.thenCallRealMethod();
			mockedCommandLineHelper
				.when(() -> CommandLineHelper.getFileNameFromSudoCommand(anyString()))
				.thenCallRealMethod();
			mockedCommandLineHelper.when(() -> CommandLineHelper.toCaseInsensitiveRegex(anyString())).thenCallRealMethod();
			mockedCommandLineHelper
				.when(() ->
					CommandLineHelper.createCommandLineEmbeddedFiles(
						NAVISECCLI_COMMAND,
						commandLineConfiguration,
						commandLineEmbeddedFiles,
						TEMP_FILE_CREATOR
					)
				)
				.thenCallRealMethod();

			mockedCommandLineHelper
				.when(() ->
					CommandLineHelper.runSshCommand(
						CLEAR_PASSWORD_COMMAND,
						HOST,
						sshConfiguration,
						120,
						Collections.emptyList(),
						NO_PASSWORD_COMMAND
					)
				)
				.thenReturn(AGENT_REV_RESULT);

			mockedCommandLineHelper
				.when(() -> CommandLineHelper.runCommandLine(NAVISECCLI_COMMAND, telemetryManager, 120L, false, false))
				.thenCallRealMethod();

			final CommandLineResult expect = new CommandLineResult(AGENT_REV_RESULT, NO_PASSWORD_COMMAND);

			assertEquals(expect, CommandLineHelper.runCommandLine(NAVISECCLI_COMMAND, telemetryManager, 120L, false, false));
		}
	}

	@Test
	void testRunCommandLineRemoteLinuxWithSudoReplaced() throws Exception {
		final SshConfiguration sshConfiguration = SshConfiguration
			.sshConfigurationBuilder()
			.username(USERNAME)
			.password(PWD_COMMAND.toCharArray())
			.build();

		final CommandLineConfiguration commandLineConfiguration = new CommandLineConfiguration();
		commandLineConfiguration.setUseSudo(true);
		commandLineConfiguration.setUseSudoCommands(Collections.singleton(NAVISECCLI_CAMEL_CASE.toLowerCase()));

		final HostConfiguration hostConfiguration = HostConfiguration
			.builder()
			.hostId(ID)
			.hostname(HOST)
			.hostType(DeviceKind.LINUX)
			.configurations(
				Map.of(
					sshConfiguration.getClass(),
					sshConfiguration,
					commandLineConfiguration.getClass(),
					commandLineConfiguration
				)
			)
			.build();

		final TelemetryManager telemetryManager = TelemetryManager.builder().hostConfiguration(hostConfiguration).build();

		try (final MockedStatic<CommandLineHelper> mockedCommandLineHelper = mockStatic(CommandLineHelper.class)) {
			mockedCommandLineHelper.when(() -> CommandLineHelper.getUsername(sshConfiguration)).thenCallRealMethod();
			mockedCommandLineHelper
				.when(() -> CommandLineHelper.getTimeout(120L, commandLineConfiguration, sshConfiguration, 300))
				.thenCallRealMethod();
			mockedCommandLineHelper.when(() -> CommandLineHelper.getPassword(sshConfiguration)).thenCallRealMethod();
			mockedCommandLineHelper
				.when(() -> CommandLineHelper.replaceSudo(anyString(), eq(commandLineConfiguration)))
				.thenCallRealMethod();
			mockedCommandLineHelper
				.when(() -> CommandLineHelper.getFileNameFromSudoCommand(anyString()))
				.thenCallRealMethod();
			mockedCommandLineHelper.when(() -> CommandLineHelper.toCaseInsensitiveRegex(anyString())).thenCallRealMethod();
			mockedCommandLineHelper
				.when(() ->
					CommandLineHelper.createCommandLineEmbeddedFiles(
						NAVISECCLI_COMMAND,
						commandLineConfiguration,
						commandLineEmbeddedFiles,
						TEMP_FILE_CREATOR
					)
				)
				.thenCallRealMethod();

			mockedCommandLineHelper
				.when(() -> {
					CommandLineHelper.runSshCommand(
						SUDO_KEYWORD + CLEAR_PASSWORD_COMMAND,
						HOST,
						sshConfiguration,
						120,
						Collections.emptyList(),
						SUDO_KEYWORD + NO_PASSWORD_COMMAND
					);
				})
				.thenReturn(AGENT_REV_RESULT);

			mockedCommandLineHelper
				.when(() -> CommandLineHelper.runCommandLine(NAVISECCLI_COMMAND, telemetryManager, 120L, false, false))
				.thenCallRealMethod();

			final CommandLineResult expect = new CommandLineResult(AGENT_REV_RESULT, SUDO_KEYWORD + NO_PASSWORD_COMMAND);

			assertEquals(expect, CommandLineHelper.runCommandLine(NAVISECCLI_COMMAND, telemetryManager, 120L, false, false));
		}
	}

	@Test
	void testRunCommandLineRemoteLinuxWithEmbeddedFilesReplaced() throws Exception {
		commandLineEmbeddedFiles.put(
			EMBEDDED_FILE_1_REF,
			new EmbeddedFile(AWK_EMBEDDED_CONTENT_PERCENT_SUDO, null, EMBEDDED_FILE_1_REF)
		);

		final File localFile = mock(File.class);
		final Map<String, File> embeddedTempFiles = new HashMap<>();
		embeddedTempFiles.put(EMBEDDED_FILE_1_REF, localFile);

		final CommandLineConfiguration commandLineConfiguration = new CommandLineConfiguration();
		commandLineConfiguration.setUseSudo(true);
		commandLineConfiguration.setUseSudoCommands(Collections.singleton(ARCCONF_PATH));

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
				Map.of(
					sshConfiguration.getClass(),
					sshConfiguration,
					commandLineConfiguration.getClass(),
					commandLineConfiguration
				)
			)
			.build();

		final TelemetryManager telemetryManager = TelemetryManager.builder().hostConfiguration(hostConfiguration).build();

		try (
			final MockedStatic<CommandLineHelper> mockedCommandLineHelper = mockStatic(CommandLineHelper.class);
			final MockedStatic<EmbeddedFileHelper> mockedEmbeddedFileHelper = mockStatic(EmbeddedFileHelper.class)
		) {
			mockedCommandLineHelper.when(() -> CommandLineHelper.getUsername(sshConfiguration)).thenCallRealMethod();
			mockedCommandLineHelper
				.when(() -> CommandLineHelper.getTimeout(120L, commandLineConfiguration, sshConfiguration, 300))
				.thenCallRealMethod();
			mockedCommandLineHelper.when(() -> CommandLineHelper.getPassword(sshConfiguration)).thenCallRealMethod();
			mockedCommandLineHelper.when(() -> CommandLineHelper.toCaseInsensitiveRegex(anyString())).thenCallRealMethod();
			mockedCommandLineHelper
				.when(() -> CommandLineHelper.getFileNameFromSudoCommand(anyString()))
				.thenCallRealMethod();
			mockedCommandLineHelper
				.when(() -> CommandLineHelper.replaceSudo(anyString(), eq(commandLineConfiguration)))
				.thenCallRealMethod();

			mockedCommandLineHelper
				.when(() ->
					CommandLineHelper.createCommandLineEmbeddedFiles(
						SH_EMBEDDED_FILE_1,
						commandLineConfiguration,
						commandLineEmbeddedFiles,
						TEMP_FILE_CREATOR
					)
				)
				.thenReturn(embeddedTempFiles);

			mockedEmbeddedFileHelper
				.when(() -> EmbeddedFileHelper.findEmbeddedFiles(anyString()))
				.thenReturn(commandLineEmbeddedFiles);

			doReturn(SEN_EMBEDDED_0001_PATH).when(localFile).getAbsolutePath();

			mockedCommandLineHelper
				.when(() -> {
					CommandLineHelper.runSshCommand(
						SH_SEN_EMBEDDED_0001_PATH,
						HOST,
						sshConfiguration,
						120,
						List.of(localFile),
						SH_SEN_EMBEDDED_0001_PATH
					);
				})
				.thenReturn(HARD_DRIVE);

			mockedCommandLineHelper
				.when(() -> CommandLineHelper.runCommandLine(SH_EMBEDDED_FILE_1, telemetryManager, 120L, false, false))
				.thenCallRealMethod();

			final CommandLineResult expect = new CommandLineResult(HARD_DRIVE, SH_SEN_EMBEDDED_0001_PATH);
			final CommandLineResult actual = CommandLineHelper.runCommandLine(
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

		assertEquals(7, CommandLineHelper.runControlledSshCommand(semaphore::availablePermits, HOSTNAME, 30));

		assertDoesNotThrow(() -> CommandLineHelper.runControlledSshCommand(LocalDate.MIN::toString, HOSTNAME, 30));

		semaphore.acquire(8);
		assertThrows(
			ControlledSshException.class,
			() -> CommandLineHelper.runControlledSshCommand(LocalDate.MIN::toString, HOSTNAME, 1)
		);
	}
}
