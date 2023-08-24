package com.sentrysoftware.matrix.strategy.utils;

import static com.sentrysoftware.matrix.constants.Constants.EMBEDDED_TEMP_FILE_PREFIX;
import static com.sentrysoftware.matrix.constants.Constants.EMPTY;
import static com.sentrysoftware.matrix.constants.Constants.HOSTNAME_MACRO;
import static com.sentrysoftware.matrix.constants.Constants.PASSWORD;
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
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.condition.EnabledOnOs;
import org.junit.jupiter.api.condition.OS;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.api.io.TempDir;
import org.mockito.MockedStatic;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
class OsCommandHelperTest {

	@TempDir
	static File tempDir;

	static Function<String, File> jUnitTempFileCreator;

	/**
	 * Setup unit tests.
	 */
	@BeforeAll
	static void setup() {
		// Initialize temporary file creator for JUnit tests.
		jUnitTempFileCreator = extension -> {
			try {
				return File.createTempFile(EMBEDDED_TEMP_FILE_PREFIX, extension, tempDir);
			} catch (IOException e) {
				throw new OsCommandHelper.TempFileCreationException(e);
			}
		};
	}

	/**
	 * Retrieve temporary embedded files using the {@link FilenameFilter}.
	 * 
	 * @return Array of {@link File} instances
	 */
	private static File[] getTempEmbeddedFiles() {

		return tempDir.listFiles(
				(directory, fileName) -> fileName.startsWith(EMBEDDED_TEMP_FILE_PREFIX)
				);
	}

	/**
	 * Assert that temporary embedded files are removed correctly.
	 */
	private static void checkNoTempEmbeddedFileExist() {
		assertEquals(0, getTempEmbeddedFiles().length);
	}

	@Test
	void testCreateOsCommandEmbeddedFiles() throws Exception {

		final String commandLine = "copy ${file::EmbeddedFile(1)} ${file::EmbeddedFile(1)}.bat > NUL & ${file::EmbeddedFile(1)}.bat %{USERNAME} %{PASSWORD} %{HOSTNAME} & del /F /Q ${file::EmbeddedFile(1)}.bat & del /F /Q ${file::EmbeddedFile(2)}.bat ";

		checkNoTempEmbeddedFileExist();

		final Map<String, EmbeddedFile> emptyMap = Collections.emptyMap();

		assertThrows(IllegalArgumentException.class, () -> OsCommandHelper
				.createOsCommandEmbeddedFiles(
						null,
						emptyMap,
						null,
						jUnitTempFileCreator
						)
				);

		assertEquals(Collections.emptyMap(), OsCommandHelper
				.createOsCommandEmbeddedFiles(
						commandLine,
						null,
						null,
						jUnitTempFileCreator
						)
				);

		assertEquals(Collections.emptyMap(), OsCommandHelper
				.createOsCommandEmbeddedFiles(
						"",
						emptyMap,
						null,
						jUnitTempFileCreator
						)
				);

		assertEquals(Collections.emptyMap(), OsCommandHelper
				.createOsCommandEmbeddedFiles(
						"cmd",
						emptyMap,
						null,
						jUnitTempFileCreator
						)
				);

		// case embeddedFile not found
		final Map<String, EmbeddedFile> singletonMapEmbeddedNotFound = Collections.singletonMap("EmbeddedFile(1)", new EmbeddedFile());
		assertThrows(IllegalArgumentException.class, () -> OsCommandHelper
				.createOsCommandEmbeddedFiles(
						commandLine,
						singletonMapEmbeddedNotFound,
						null,
						jUnitTempFileCreator
						)
				);

		// case embeddedFile content null
		final Map<String, EmbeddedFile> singletonMapEmbeddedNoContent = Collections.singletonMap("EmbeddedFile(1)", new EmbeddedFile());
		assertThrows(IllegalArgumentException.class, () -> OsCommandHelper
				.createOsCommandEmbeddedFiles(
						commandLine,
						singletonMapEmbeddedNoContent,
						null,
						jUnitTempFileCreator
						)
				);

		checkNoTempEmbeddedFileExist();

		// case IOException in temp file creation
		try (final MockedStatic<OsCommandHelper> mockedOsCommandHelper = mockStatic(OsCommandHelper.class)) {

			final Map<String, EmbeddedFile> embeddedFiles = new HashMap<>();
			embeddedFiles.put("EmbeddedFile(1)", new EmbeddedFile("ECHO %OS%", "bat", 1));
			embeddedFiles.put("EmbeddedFile(2)", new EmbeddedFile("echo Hello World", null, 2));

			mockedOsCommandHelper
			.when(() -> OsCommandHelper
					.createTempFileWithEmbeddedFileContent(
							any(EmbeddedFile.class),
							isNull(),
							any()
							)
					)
			.thenThrow(IOException.class);

			mockedOsCommandHelper
			.when(() -> OsCommandHelper
					.createOsCommandEmbeddedFiles(
							commandLine,
							embeddedFiles,
							null,
							jUnitTempFileCreator
							)
					)
			.thenCallRealMethod();

			assertThrows(IOException.class, () -> OsCommandHelper
					.createOsCommandEmbeddedFiles(
							commandLine,
							embeddedFiles,
							null,
							jUnitTempFileCreator
							)
					);
		}

		// case OK
		{
			checkNoTempEmbeddedFileExist();

			final String embeddedContent =
					"# Awk (or nawk)\n" +
							"if [ -f /usr/xpg4/bin/awk ]; then\n" +
							"	AWK=\"/usr/xpg4/bin/awk\";\n" +
							"elif [ -f /usr/bin/nawk ]; then\n" +
							"	AWK=\"/usr/bin/nawk\";\n" +
							"else\n" +
							"	AWK=\"awk\";\n" +
							"fi\n" +
							"if [ -f /opt/StorMan/arcconf ]; then\n" +
							"       STORMAN=\"/opt/StorMan\";\n" +
							"elif [ -f /usr/StorMan/arcconf ]; then\n" +
							"       STORMAN=\"/usr/StorMan\";\n" +
							"else\n" +
							"	echo No Storman Installed; exit;\n" +
							"fi\n" +
							"DEVICES=`%{SUDO:/[opt|usr]/StorMan/arcconf} $STORMAN/arcconf getversion | $AWK '($1 ~ /Controller/ && $2 ~ /#[0-9]/) {controller=$2;gsub(/#/,\"\",controller);print(controller)}'`\n" +
							"for CTRL in $DEVICES\n" +
							"                do\n" +
							"                echo MSHWController $CTRL\n" +
							"                %{SUDO:/[opt|usr]/StorMan/arcconf} $STORMAN/arcconf getconfig $CTRL PD\n" +
							"                done";

			final Map<String, EmbeddedFile> embeddedFiles = new HashMap<>();
			embeddedFiles.put("EmbeddedFile(1)", new EmbeddedFile("ECHO %OS%", "bat", 1));
			embeddedFiles.put("EmbeddedFile(2)", new EmbeddedFile(embeddedContent, null, 2));

			final Map<String, File> embeddedTempFiles = OsCommandHelper
					.createOsCommandEmbeddedFiles(
							commandLine,
							embeddedFiles,
							OsCommandConfiguration
							.builder()
							.useSudo(true)
							.useSudoCommands(Set.of("/[opt|usr]/StorMan/arcconf"))
							.build(),
							jUnitTempFileCreator
							);

			assertEquals(2, embeddedTempFiles.size());

			{
				final File file = embeddedTempFiles.get("EmbeddedFile(1)");
				assertNotNull(file);
				assertTrue(file.exists());
				assertTrue(file.getName().matches(EMBEDDED_TEMP_FILE_PREFIX+"\\w+\\.bat"));
				assertEquals(
						"ECHO %OS%",
						Files.readAllLines(Paths.get(file.getAbsolutePath())).stream().collect(Collectors.joining()));
				file.delete();
			}
			{
				final String expectedContent =
						"# Awk (or nawk)\n" +
								"if [ -f /usr/xpg4/bin/awk ]; then\n" +
								"	AWK=\"/usr/xpg4/bin/awk\";\n" +
								"elif [ -f /usr/bin/nawk ]; then\n" +
								"	AWK=\"/usr/bin/nawk\";\n" +
								"else\n" +
								"	AWK=\"awk\";\n" +
								"fi\n" +
								"if [ -f /opt/StorMan/arcconf ]; then\n" +
								"       STORMAN=\"/opt/StorMan\";\n" +
								"elif [ -f /usr/StorMan/arcconf ]; then\n" +
								"       STORMAN=\"/usr/StorMan\";\n" +
								"else\n" +
								"	echo No Storman Installed; exit;\n" +
								"fi\n" +
								"DEVICES=`sudo $STORMAN/arcconf getversion | $AWK '($1 ~ /Controller/ && $2 ~ /#[0-9]/) {controller=$2;gsub(/#/,\"\",controller);print(controller)}'`\n" +
								"for CTRL in $DEVICES\n" +
								"                do\n" +
								"                echo MSHWController $CTRL\n" +
								"                sudo $STORMAN/arcconf getconfig $CTRL PD\n" +
								"                done";

				final File file = embeddedTempFiles.get("EmbeddedFile(2)");
				assertNotNull(file);
				assertTrue(file.exists());
				assertTrue(file.getName().matches(EMBEDDED_TEMP_FILE_PREFIX+"\\w+"));
				assertEquals(
						expectedContent.replaceAll("[\r\n]", EMPTY),
						Files.readAllLines(Paths.get(file.getAbsolutePath())).stream().collect(Collectors.joining()));
				file.delete();
			}
		}
	}

	@Test
	void testReplaceSudo() {

		assertNull(OsCommandHelper.replaceSudo(null, null));
		assertNull(OsCommandHelper.replaceSudo(null, OsCommandConfiguration.builder().build()));

		assertEquals("", OsCommandHelper.replaceSudo("", null));
		assertEquals("", OsCommandHelper.replaceSudo("", OsCommandConfiguration.builder().build()));
		assertEquals(" ", OsCommandHelper.replaceSudo(" ", null));
		assertEquals(" ", OsCommandHelper.replaceSudo(" ", OsCommandConfiguration.builder().build()));

		assertEquals("text", OsCommandHelper.replaceSudo("text", null));
		assertEquals("text", OsCommandHelper.replaceSudo("text", OsCommandConfiguration.builder().build()));

		// Check replace sudo tag with empty string.
		assertEquals(" key", OsCommandHelper.replaceSudo("%{SUDO:key} key", null));
		assertEquals(" key", OsCommandHelper.replaceSudo("%{SUDO:key} key", OsCommandConfiguration.builder().build()));
		assertEquals(" key", OsCommandHelper.replaceSudo("%{SUDO:key} key", OsCommandConfiguration.builder().useSudo(true).build()));
		assertEquals(" key\n key", OsCommandHelper.replaceSudo("%{SUDO:key} key\n%{SUDO:key} key", OsCommandConfiguration.builder().useSudo(true).build()));

		assertEquals("sudo key", OsCommandHelper.replaceSudo(
				"%{SUDO:key} key",
				OsCommandConfiguration.builder().useSudo(true).useSudoCommands(Set.of("key")).build()));

		assertEquals("sudo key\nsudo key", OsCommandHelper.replaceSudo(
				"%{SUDO:key} key\n%{SUDO:key} key",
				OsCommandConfiguration.builder().useSudo(true).useSudoCommands(Set.of("key")).build()));
	}

	@Test
	void testRunLocalCommandKO() throws Exception {
		final String command = "cmd";

		assertThrows(IllegalArgumentException.class, () -> OsCommandHelper.runLocalCommand(null, 1, null));
		assertThrows(IllegalArgumentException.class, () -> OsCommandHelper.runLocalCommand(command, -1, null));
		assertThrows(IllegalArgumentException.class, () -> OsCommandHelper.runLocalCommand(command, 0, null));

		// case Process null Linux
		final Runtime runtime = mock(Runtime.class);
		try (final MockedStatic<LocalOsHandler> mockedLocalOSHandler = mockStatic(LocalOsHandler.class);
				final MockedStatic<Runtime> mockedRuntime = mockStatic(Runtime.class)) {
			mockedLocalOSHandler.when(LocalOsHandler::isWindows).thenReturn(false);
			mockedRuntime.when(Runtime::getRuntime).thenReturn(runtime);
			when(runtime.exec(command)).thenReturn(null);

			assertThrows(IllegalStateException.class, () -> OsCommandHelper.runLocalCommand(command, 1, null));
		}

		// case Process null Windows
		try (final MockedStatic<LocalOsHandler> mockedLocalOSHandler = mockStatic(LocalOsHandler.class);
				final MockedStatic<Runtime> mockedRuntime = mockStatic(Runtime.class)) {
			mockedLocalOSHandler.when(LocalOsHandler::isWindows).thenReturn(true);
			mockedRuntime.when(Runtime::getRuntime).thenReturn(runtime);
			when(runtime.exec("CMD.EXE /C cmd")).thenReturn(null);

			assertThrows(IllegalStateException.class, () -> OsCommandHelper.runLocalCommand(command, 1, null));
		}
	}

	@Test
	@EnabledOnOs(OS.WINDOWS)
	void testRunLocalCommandWindowsTimeout() throws Exception {
		assertThrows(TimeoutException.class, () -> OsCommandHelper.runLocalCommand("PAUSE", 1, null));
	}

	@Test
	@EnabledOnOs(OS.LINUX)
	void testRunLocalCommandLinuxTimeout() throws Exception {
		assertThrows(TimeoutException.class, () -> OsCommandHelper.runLocalCommand("sleep 5", 1, null));
	}

	@Test
	@EnabledOnOs(OS.WINDOWS)
	void testRunLocalCommandWindows() throws Exception {
		assertEquals("Test", OsCommandHelper.runLocalCommand("ECHO Test", 1, null));
	}

	@Test
	@EnabledOnOs(OS.LINUX)
	void testRunLocalCommandLinux() throws Exception {
		assertEquals("Test", OsCommandHelper.runLocalCommand("echo Test", 1, null));
	}

	@Test
	void testRunSshCommand() throws Exception {
		final String command = "cmd";
		final String hostname = "host";
		final SshConfiguration sshConfiguration = mock(SshConfiguration.class);
		final int timeout = 1000;

		assertThrows(IllegalArgumentException.class, () -> OsCommandHelper.runSshCommand(null, hostname, sshConfiguration, timeout, null, null));
		assertThrows(IllegalArgumentException.class, () -> OsCommandHelper.runSshCommand(command, null, sshConfiguration, timeout, null, null));
		assertThrows(IllegalArgumentException.class, () -> OsCommandHelper.runSshCommand(command, hostname, null, timeout, null, null));
		assertThrows(IllegalArgumentException.class, () -> OsCommandHelper.runSshCommand(command, hostname, sshConfiguration, -1, null, null));
		assertThrows(IllegalArgumentException.class, () -> OsCommandHelper.runSshCommand(command, hostname, sshConfiguration, 0, null, null));

		try (final MockedStatic<MatsyaClientsExecutor> mockedMatsyaClientsExecutor = mockStatic(MatsyaClientsExecutor.class)) {

			when(sshConfiguration.getUsername()).thenReturn("user");
			when(sshConfiguration.getPassword()).thenReturn(PASSWORD.toCharArray());

			mockedMatsyaClientsExecutor.when(() -> MatsyaClientsExecutor.runRemoteSshCommand(
					hostname,
					"user",
					PASSWORD.toCharArray(),
					null,
					command,
					timeout,
					null,
					null)).thenReturn("result");

			assertEquals("result", OsCommandHelper.runSshCommand(command, hostname, sshConfiguration, timeout, null, null));
		}
	}

	@Test
	void testGetFileNameFromSudoCommand() {
		assertThrows(IllegalArgumentException.class, () -> OsCommandHelper.getFileNameFromSudoCommand(null));
		assertEquals(Optional.empty(), OsCommandHelper.getFileNameFromSudoCommand("/usr/sbin/raidctl -S"));
		assertEquals(Optional.of("/usr/sbin/raidctl"), OsCommandHelper.getFileNameFromSudoCommand("%{SUDO:/usr/sbin/raidctl} /usr/sbin/raidctl -S"));
		assertEquals(Optional.of("NaviSecCli"), OsCommandHelper.getFileNameFromSudoCommand("%{Sudo:NaviSecCli} NaviSecCli -User %{USERNAME} -Password %{PASSWORD} -Address host -Scope 1 getagent"));
	}

	@Test
	void testToCaseInsensitiveRegex() {
		assertThrows(IllegalArgumentException.class, () -> OsCommandHelper.toCaseInsensitiveRegex(null));
		assertThrows(IllegalArgumentException.class, () -> OsCommandHelper.toCaseInsensitiveRegex(""));
		assertEquals(" ", OsCommandHelper.toCaseInsensitiveRegex(" "));
		assertEquals("(?i)\\QHost\\E", OsCommandHelper.toCaseInsensitiveRegex("Host"));
		assertEquals("(?i)\\Q%{UserName}\\E", OsCommandHelper.toCaseInsensitiveRegex("%{UserName}"));
		assertEquals("(?i)\\Q%{HOSTNAME}\\E", OsCommandHelper.toCaseInsensitiveRegex(HOSTNAME_MACRO));
	}

	@Test
	void testGetTimeout() {
		final OsCommandConfiguration osCommandConfig = new OsCommandConfiguration();
		osCommandConfig.setTimeout(2L);

		final WmiConfiguration wmiConfiguration = new WmiConfiguration();
		wmiConfiguration.setTimeout(3L);

		final SshConfiguration sshConfiguration = SshConfiguration.sshConfigurationBuilder().username("user").password(PASSWORD.toCharArray()).build();
		sshConfiguration.setTimeout(4L);

		assertEquals(1, OsCommandHelper.getTimeout(1L, osCommandConfig, sshConfiguration, 5));
		assertEquals(2, OsCommandHelper.getTimeout(null, osCommandConfig, sshConfiguration, 5));
		assertEquals(3, OsCommandHelper.getTimeout(null, null, wmiConfiguration, 5));
		assertEquals(4, OsCommandHelper.getTimeout(null, null, sshConfiguration, 5));
		assertEquals(5, OsCommandHelper.getTimeout(null, null, null, 5));
		assertEquals(30, OsCommandHelper.getTimeout(null, new OsCommandConfiguration(), sshConfiguration, 5));
		assertEquals(120, OsCommandHelper.getTimeout(null, null, new WmiConfiguration(), 5));
		assertEquals(30, OsCommandHelper.getTimeout(null, null, SshConfiguration.sshConfigurationBuilder().username("user").password(PASSWORD.toCharArray()).build(), 5));
	}

	@Test
	void testGetUsername() {
		assertEquals(Optional.empty(), OsCommandHelper.getUsername(null));
		assertEquals(Optional.empty(), OsCommandHelper.getUsername(new OsCommandConfiguration()));
		assertEquals(Optional.empty(), OsCommandHelper.getUsername(new WmiConfiguration()));
		assertEquals(Optional.empty(), OsCommandHelper.getUsername(SshConfiguration.sshConfigurationBuilder().password(PASSWORD.toCharArray()).build()));

		final WmiConfiguration wmiConfiguration = new WmiConfiguration();
		wmiConfiguration.setUsername("user");
		assertEquals(Optional.of("user"), OsCommandHelper.getUsername(wmiConfiguration));

		assertEquals(Optional.of("user"), OsCommandHelper.getUsername(SshConfiguration.sshConfigurationBuilder().username("user").password(PASSWORD.toCharArray()).build()));
	}

	@Test
	void testGetPassword() {
		assertEquals(Optional.empty(), OsCommandHelper.getPassword(null));
		assertEquals(Optional.empty(), OsCommandHelper.getPassword(new OsCommandConfiguration()));
		assertEquals(Optional.empty(), OsCommandHelper.getPassword(new WmiConfiguration()));
		assertEquals(Optional.empty(), OsCommandHelper.getPassword(SshConfiguration.sshConfigurationBuilder().username("user").build()));

		final WmiConfiguration wmiConfiguration = new WmiConfiguration();
		char[] charArrayPassword = PASSWORD.toCharArray();
		wmiConfiguration.setPassword(charArrayPassword);
		assertEquals(Optional.of(charArrayPassword), OsCommandHelper.getPassword(wmiConfiguration));

		assertEquals(Optional.of(charArrayPassword), OsCommandHelper.getPassword(SshConfiguration.sshConfigurationBuilder().username("user").password(charArrayPassword).build()));
	}

	@Test
	void testRunOsCommandCommandLineNull() {
		final TelemetryManager telemetryManager = TelemetryManager.builder().build();
		assertThrows(IllegalArgumentException.class, () -> OsCommandHelper
				.runOsCommand(
						null,
						telemetryManager,
						120L,
						false,
						false
						)
				);
	}

	@Test
	void testRunOsCommandTelemetryManagerNull() {
		assertThrows(IllegalArgumentException.class, () -> OsCommandHelper
				.runOsCommand(
						"cmd",
						null,
						120L,
						false,
						false
						)
				);
	}

	@Test
	void testRunOsCommandRemoteNoUser() {
		final SshConfiguration sshConfiguration = SshConfiguration.sshConfigurationBuilder()
				.username(" ")
				.password("pwd".toCharArray())
				.build();

		final HostConfiguration hostConfiguration = HostConfiguration
				.builder()
				.hostId("id")
				.hostname("host")
				.hostType(DeviceKind.WINDOWS)
				.configurations(Map.of(sshConfiguration.getClass(), sshConfiguration))
				.build();

		final TelemetryManager telemetryManager = TelemetryManager
				.builder()
				.hostConfiguration(hostConfiguration)
				.build();

		assertThrows(NoCredentialProvidedException.class, () -> OsCommandHelper.runOsCommand(
				"%{SUDO:naviseccli} naviseccli -User %{USERNAME} -Password %{PASSWORD} -Address %{HOSTNAME} -Scope 1 getagent",
				telemetryManager,
				120L,
				false,
				false));
	}

	@Test
	void testRunOsCommandRemoteWindowsEmbeddedFilesError() {

		final String command = 
				"copy ${file::EmbeddedFile(2)} ${file::EmbeddedFile(2)}.bat > NUL"
						+ " & ${file::EmbeddedFile(1)}"
						+ " & ${file::EmbeddedFile(2)}.bat"
						+ " & del /F /Q ${file::EmbeddedFile(1)}"
						+ " & del /F /Q ${file::EmbeddedFile(2)}.bat";

		final Map<String, EmbeddedFile> embeddedFiles = new HashMap<>();
		embeddedFiles.put("EmbeddedFile(1)", new EmbeddedFile("ECHO %OS%", "bat", 1));
		embeddedFiles.put("EmbeddedFile(2)", new EmbeddedFile("echo Hello World", null, 2));

		final WmiConfiguration wmiConfiguration = new WmiConfiguration();
		wmiConfiguration.setUsername("user");
		wmiConfiguration.setPassword("pwd".toCharArray());

		final HostConfiguration hostConfiguration = HostConfiguration
				.builder()
				.hostId("id")
				.hostname("host")
				.hostType(DeviceKind.WINDOWS)
				.configurations(Map.of(wmiConfiguration.getClass(), wmiConfiguration))
				.build();

		final TelemetryManager telemetryManager = TelemetryManager
				.builder()
				.hostConfiguration(hostConfiguration)
				.build();

		try (final MockedStatic<OsCommandHelper> mockedOsCommandHelper = mockStatic(OsCommandHelper.class)) {
			mockedOsCommandHelper.when(() -> OsCommandHelper.getUsername(wmiConfiguration)).thenCallRealMethod();
			mockedOsCommandHelper.when(() -> OsCommandHelper.findEmbeddedFiles(anyString())).thenReturn(embeddedFiles);
			mockedOsCommandHelper.when(() -> OsCommandHelper.runOsCommand(
					command,
					telemetryManager,
					120L,
					false,
					false)).thenCallRealMethod();

			mockedOsCommandHelper
			.when(() -> OsCommandHelper
					.createOsCommandEmbeddedFiles(
							command, 
							embeddedFiles, 
							null,
							TEMP_FILE_CREATOR
							)
					)
			.thenThrow(new IOException("error in file1"));

			assertThrows(IOException.class, () -> OsCommandHelper.runOsCommand(
					command,
					telemetryManager,
					120L,
					false,
					false));
		}
	}

	@Test
	@EnabledOnOs(OS.WINDOWS)
	void testRunOsCommandWindowsError() {

		final SshConfiguration sshConfiguration = SshConfiguration.sshConfigurationBuilder()
				.username("user")
				.password("pwd".toCharArray())
				.build();

		final HostConfiguration hostConfiguration = HostConfiguration
				.builder()
				.hostId("id")
				.hostname("host")
				.hostType(DeviceKind.WINDOWS)
				.configurations(Map.of(sshConfiguration.getClass(), sshConfiguration))
				.build();

		final TelemetryManager telemetryManager = TelemetryManager
				.builder()
				.hostConfiguration(hostConfiguration)
				.build();

		assertThrows(TimeoutException.class, () -> OsCommandHelper.runOsCommand(
				"PAUSE",
				telemetryManager,
				1L,
				false,
				true));
	}

	@Test
	@EnabledOnOs(OS.LINUX)
	void testRunOsCommandLinuxError() {

		final HostConfiguration hostConfiguration = HostConfiguration
				.builder()
				.hostId("id")
				.hostname("host")
				.hostType(DeviceKind.LINUX)
				.build();

		final TelemetryManager telemetryManager = TelemetryManager
				.builder()
				.hostConfiguration(hostConfiguration)
				.build();

		assertThrows(TimeoutException.class, () -> OsCommandHelper.runOsCommand(
				"sleep 5",
				telemetryManager,
				1L,
				false,
				true));
	}

	@Test
	@EnabledOnOs(OS.WINDOWS)
	void testRunOsCommandLocalWindows() throws Exception {

		final WmiConfiguration wmiConfiguration = new WmiConfiguration();
		wmiConfiguration.setUsername("user");
		wmiConfiguration.setPassword("pwd".toCharArray());

		final HostConfiguration hostConfiguration = HostConfiguration
				.builder()
				.hostId("id")
				.hostname("host")
				.hostType(DeviceKind.WINDOWS)
				.configurations(Map.of(wmiConfiguration.getClass(), wmiConfiguration))
				.build();

		final TelemetryManager telemetryManager = TelemetryManager
				.builder()
				.hostConfiguration(hostConfiguration)
				.build();

		final String command = "ECHO Test";
		final OsCommandResult expect = new OsCommandResult("Test", command);

		assertEquals(
				expect, 
				OsCommandHelper.runOsCommand(
						command,
						telemetryManager,
						120L,
						false,
						true));
	}

	@Test
	@EnabledOnOs(OS.LINUX)
	void testRunOsCommandLocalLinux() throws Exception {

		final SshConfiguration sshConfiguration = SshConfiguration.sshConfigurationBuilder()
				.username("user")
				.password("pwd".toCharArray())
				.build();

		final HostConfiguration hostConfiguration = HostConfiguration
				.builder()
				.hostId("id")
				.hostname("host")
				.hostType(DeviceKind.LINUX)
				.configurations(Map.of(sshConfiguration.getClass(), sshConfiguration))
				.build();

		final TelemetryManager telemetryManager = TelemetryManager
				.builder()
				.hostConfiguration(hostConfiguration)
				.build();

		final String command = "echo Test";
		final OsCommandResult expect = new OsCommandResult("Test", command);

		assertEquals(
				expect, 
				OsCommandHelper.runOsCommand(
						command,
						telemetryManager,
						120L,
						false,
						true));
	}

	@Test
	@EnabledOnOs(OS.WINDOWS)
	void testRunOsCommandRemoteExecutedLocallyWindows() throws Exception {

		final String command = "ECHO Test";
		final OsCommandResult expect = new OsCommandResult("Test", command);

		final HostConfiguration hostConfiguration = HostConfiguration
				.builder()
				.hostId("id")
				.hostname("host")
				.hostType(DeviceKind.WINDOWS)
				.build();

		final TelemetryManager telemetryManager = TelemetryManager
				.builder()
				.hostConfiguration(hostConfiguration)
				.build();

		assertEquals(
				expect, 
				OsCommandHelper.runOsCommand(
						command,
						telemetryManager,
						120L,
						true,
						false));
	}

	@Test
	@EnabledOnOs(OS.LINUX)
	void testRunOsCommandRemoteExecutedLocallyLinux() throws Exception {

		final String command = "echo Test";
		final OsCommandResult expect = new OsCommandResult("Test", command);

		final HostConfiguration hostConfiguration = HostConfiguration
				.builder()
				.hostId("id")
				.hostname("host")
				.hostType(DeviceKind.LINUX)
				.build();

		final TelemetryManager telemetryManager = TelemetryManager
				.builder()
				.hostConfiguration(hostConfiguration)
				.build();

		assertEquals(
				expect, 
				OsCommandHelper.runOsCommand(
						command,
						telemetryManager,
						120L,
						true,
						false));
	}

	@Test
	void testRunOsCommandRemoteWindows() throws Exception {

		final String command = 
				"copy ${file::EmbeddedFile(2)} ${file::EmbeddedFile(2)}.bat > NUL"
						+ " & ${file::EmbeddedFile(1)}"
						+ " & ${file::EmbeddedFile(2)}.bat"
						+ " & del /F /Q ${file::EmbeddedFile(1)}"
						+ " & del /F /Q ${file::EmbeddedFile(2)}.bat";

		final String result = "Windows_NT\nHello World";

		final Map<String, EmbeddedFile> embeddedFiles = new HashMap<>();
		embeddedFiles.put("EmbeddedFile(1)", new EmbeddedFile("ECHO %OS%", "bat", 1));
		embeddedFiles.put("EmbeddedFile(2)", new EmbeddedFile("echo Hello World", null, 2));

		final File file1 = mock(File.class);
		final File file2 = mock(File.class);
		final Map<String, File> embeddedTempFiles = new HashMap<>();
		embeddedTempFiles.put("EmbeddedFile(1)", file1);
		embeddedTempFiles.put("EmbeddedFile(2)", file2);

		final WmiConfiguration wmiConfiguration = new WmiConfiguration();
		wmiConfiguration.setUsername("user");
		wmiConfiguration.setPassword("pwd".toCharArray());

		final HostConfiguration hostConfiguration = HostConfiguration
				.builder()
				.hostId("id")
				.hostname("host")
				.hostType(DeviceKind.WINDOWS)
				.configurations(Map.of(wmiConfiguration.getClass(), wmiConfiguration))
				.build();

		final TelemetryManager telemetryManager = TelemetryManager
				.builder()
				.hostConfiguration(hostConfiguration)
				.build();

		try (final MockedStatic<OsCommandHelper> mockedOsCommandHelper = mockStatic(OsCommandHelper.class);
				final MockedStatic<MatsyaClientsExecutor> mockedMatsyaClientsExecutor = mockStatic(MatsyaClientsExecutor.class)) {
			mockedOsCommandHelper.when(() -> OsCommandHelper.getUsername(wmiConfiguration)).thenCallRealMethod();
			mockedOsCommandHelper.when(() -> OsCommandHelper.getTimeout(120L, null, wmiConfiguration, 300)).thenCallRealMethod();
			mockedOsCommandHelper.when(() -> OsCommandHelper.getPassword(wmiConfiguration)).thenCallRealMethod();
			mockedOsCommandHelper.when(() -> OsCommandHelper.replaceSudo(anyString(), isNull())).thenCallRealMethod();
			mockedOsCommandHelper.when(() -> OsCommandHelper.getFileNameFromSudoCommand(anyString())).thenCallRealMethod();
			mockedOsCommandHelper.when(() -> OsCommandHelper.toCaseInsensitiveRegex(anyString())).thenCallRealMethod();
			mockedOsCommandHelper.when(() -> OsCommandHelper.findEmbeddedFiles(anyString())).thenReturn(embeddedFiles);

			mockedOsCommandHelper
			.when(() -> OsCommandHelper
					.createOsCommandEmbeddedFiles(
							command, 
							embeddedFiles, 
							null,
							TEMP_FILE_CREATOR
							)
					)
			.thenReturn(embeddedTempFiles);

			final String absolutePath1 = "/tmp/SEN_Embedded_1.bat";
			final String absolutePath2 = "/tmp/SEN_Embedded_2";
			final String updatedCommand = 
					"copy ${file::/tmp/SEN_Embedded_2} ${file::/tmp/SEN_Embedded_2}.bat > NUL"
							+ " & ${file::/tmp/SEN_Embedded_1.bat}"
							+ " & ${file::/tmp/SEN_Embedded_2}.bat"
							+ " & del /F /Q ${file::/tmp/SEN_Embedded_1.bat}"
							+ " & del /F /Q ${file::/tmp/SEN_Embedded_2}.bat";

			doReturn(absolutePath1).when(file1).getAbsolutePath();
			doReturn(absolutePath2).when(file2).getAbsolutePath();

			mockedMatsyaClientsExecutor.when(() -> MatsyaClientsExecutor.executeWinRemoteCommand(
					"host",
					wmiConfiguration,
					updatedCommand,
					List.of(absolutePath1, absolutePath2))).thenReturn(result);

			mockedOsCommandHelper.when(() -> OsCommandHelper.runOsCommand(
					command,
					telemetryManager,
					120L,
					false,
					false)).thenCallRealMethod();

			final OsCommandResult expect = new OsCommandResult(result, updatedCommand);

			final OsCommandResult res = OsCommandHelper.runOsCommand(
					command,
					telemetryManager,
					120L,
					false,
					false);
			assertEquals(expect, res);
		}
	}

	@Test
	void testRunOsCommandRemoteLinuxOSCommandConfigNull() throws Exception {

		final String commandLine = "%{SUDO:naviseccli} naviseccli -User %{USERNAME} -Password %{PASSWORD} -Address %{HOSTNAME} -Scope 1 getagent";
		final String result = "Agent Rev:";

		final SshConfiguration sshConfiguration = SshConfiguration.sshConfigurationBuilder()
				.username("user")
				.password("pwd".toCharArray())
				.build();

		final HostConfiguration hostConfiguration = HostConfiguration
				.builder()
				.hostId("id")
				.hostname("host")
				.hostType(DeviceKind.LINUX)
				.configurations(Map.of(sshConfiguration.getClass(), sshConfiguration))
				.build();

		final TelemetryManager telemetryManager = TelemetryManager
				.builder()
				.hostConfiguration(hostConfiguration)
				.build();

		try (final MockedStatic<OsCommandHelper> mockedOsCommandHelper = mockStatic(OsCommandHelper.class)) {
			mockedOsCommandHelper.when(() -> OsCommandHelper.getUsername(sshConfiguration)).thenCallRealMethod();
			mockedOsCommandHelper.when(() -> OsCommandHelper.getTimeout(120L, null, sshConfiguration, 300)).thenCallRealMethod();
			mockedOsCommandHelper.when(() -> OsCommandHelper.getPassword(sshConfiguration)).thenCallRealMethod();
			mockedOsCommandHelper.when(() -> OsCommandHelper.replaceSudo(anyString(), isNull())).thenCallRealMethod();
			mockedOsCommandHelper.when(() -> OsCommandHelper.getFileNameFromSudoCommand(anyString())).thenCallRealMethod();
			mockedOsCommandHelper.when(() -> OsCommandHelper.toCaseInsensitiveRegex(anyString())).thenCallRealMethod();
			mockedOsCommandHelper.when(() -> OsCommandHelper.findEmbeddedFiles(anyString())).thenReturn(new HashMap<>());
			mockedOsCommandHelper
			.when(() -> OsCommandHelper
					.createOsCommandEmbeddedFiles(
							commandLine, 
							Collections.emptyMap(),
							null,
							TEMP_FILE_CREATOR
							)
					)
			.thenCallRealMethod();

			final String noPasswordCommand = " naviseccli -User user -Password ******** -Address host -Scope 1 getagent";

			mockedOsCommandHelper.when(() -> {
				OsCommandHelper.runSshCommand(
						" naviseccli -User user -Password pwd -Address host -Scope 1 getagent",
						"host",
						sshConfiguration,
						120,
						Collections.emptyList(),
						noPasswordCommand);
			})
			.thenReturn(result);

			mockedOsCommandHelper.when(() -> OsCommandHelper.runOsCommand(
					commandLine,
					telemetryManager,
					120L,
					false,
					false)).thenCallRealMethod();

			final OsCommandResult expect = new OsCommandResult(result, noPasswordCommand);

			assertEquals(
					expect, 
					OsCommandHelper.runOsCommand(
							commandLine,
							telemetryManager,
							120L,
							false,
							false));
		}
	}

	@Test
	void testRunOsCommandRemoteLinuxNoSudo() throws Exception {

		final String command = "%{SUDO:naviseccli} naviseccli -User %{USERNAME} -Password %{PASSWORD} -Address %{HOSTNAME} -Scope 1 getagent";
		final String result = "Agent Rev:";

		final SshConfiguration sshConfiguration = SshConfiguration.sshConfigurationBuilder()
				.username("user")
				.password("pwd".toCharArray())
				.build();

		final OsCommandConfiguration osCommandConfiguration = new OsCommandConfiguration();
		osCommandConfiguration.setUseSudoCommands(Collections.singleton("naviseccli"));

		final HostConfiguration hostConfiguration = HostConfiguration
				.builder()
				.hostId("id")
				.hostname("host")
				.hostType(DeviceKind.LINUX)
				.configurations(Map.of(sshConfiguration.getClass(), sshConfiguration, osCommandConfiguration.getClass(), osCommandConfiguration))
				.build();

		final TelemetryManager telemetryManager = TelemetryManager
				.builder()
				.hostConfiguration(hostConfiguration)
				.build();

		try (final MockedStatic<OsCommandHelper> mockedOsCommandHelper = mockStatic(OsCommandHelper.class)) {
			mockedOsCommandHelper.when(() -> OsCommandHelper.getUsername(sshConfiguration)).thenCallRealMethod();
			mockedOsCommandHelper.when(() -> OsCommandHelper.getTimeout(120L, osCommandConfiguration, sshConfiguration, 300)).thenCallRealMethod();
			mockedOsCommandHelper.when(() -> OsCommandHelper.getPassword(sshConfiguration)).thenCallRealMethod();
			mockedOsCommandHelper.when(() -> OsCommandHelper.replaceSudo(anyString(), eq(osCommandConfiguration))).thenCallRealMethod();
			mockedOsCommandHelper.when(() -> OsCommandHelper.getFileNameFromSudoCommand(anyString())).thenCallRealMethod();
			mockedOsCommandHelper.when(() -> OsCommandHelper.toCaseInsensitiveRegex(anyString())).thenCallRealMethod();
			mockedOsCommandHelper.when(() -> OsCommandHelper.findEmbeddedFiles(anyString())).thenReturn(new HashMap<>());
			mockedOsCommandHelper
			.when(() -> OsCommandHelper
					.createOsCommandEmbeddedFiles(
							command,
							Collections.emptyMap(),
							osCommandConfiguration,
							TEMP_FILE_CREATOR
							)
					)
			.thenCallRealMethod();

			final String noPasswordCommand = " naviseccli -User user -Password ******** -Address host -Scope 1 getagent";

			mockedOsCommandHelper.when(() -> OsCommandHelper.runSshCommand(
					" naviseccli -User user -Password pwd -Address host -Scope 1 getagent",
					"host",
					sshConfiguration,
					120,
					Collections.emptyList(),
					noPasswordCommand))
			.thenReturn(result);

			mockedOsCommandHelper.when(() -> OsCommandHelper.runOsCommand(
					command,
					telemetryManager,
					120L,
					false,
					false)).thenCallRealMethod();

			final OsCommandResult expect = new OsCommandResult(result, noPasswordCommand);

			assertEquals(
					expect, 
					OsCommandHelper.runOsCommand(
							command,
							telemetryManager,
							120L,
							false,
							false));
		}
	}

	@Test
	void testRunOsCommandRemoteLinuxNotInUseSudoCommands() throws Exception {

		final String command = "%{SUDO:naviseccli} naviseccli -User %{USERNAME} -Password %{PASSWORD} -Address %{HOSTNAME} -Scope 1 getagent";
		final String result = "Agent Rev:";

		final SshConfiguration sshConfiguration = SshConfiguration.sshConfigurationBuilder()
				.username("user")
				.password("pwd".toCharArray())
				.build();

		final OsCommandConfiguration osCommandConfiguration = new OsCommandConfiguration();
		osCommandConfiguration.setUseSudo(true);
		osCommandConfiguration.setUseSudoCommands(Collections.singleton("other"));

		final HostConfiguration hostConfiguration = HostConfiguration
				.builder()
				.hostId("id")
				.hostname("host")
				.hostType(DeviceKind.LINUX)
				.configurations(Map.of(sshConfiguration.getClass(), sshConfiguration, osCommandConfiguration.getClass(), osCommandConfiguration))
				.build();

		final TelemetryManager telemetryManager = TelemetryManager
				.builder()
				.hostConfiguration(hostConfiguration)
				.build();

		try (final MockedStatic<OsCommandHelper> mockedOsCommandHelper = mockStatic(OsCommandHelper.class)) {
			mockedOsCommandHelper.when(() -> OsCommandHelper.getUsername(sshConfiguration)).thenCallRealMethod();
			mockedOsCommandHelper.when(() -> OsCommandHelper.getTimeout(120L, osCommandConfiguration, sshConfiguration, 300)).thenCallRealMethod();
			mockedOsCommandHelper.when(() -> OsCommandHelper.getPassword(sshConfiguration)).thenCallRealMethod();
			mockedOsCommandHelper.when(() -> OsCommandHelper.replaceSudo(anyString(), eq(osCommandConfiguration))).thenCallRealMethod();
			mockedOsCommandHelper.when(() -> OsCommandHelper.getFileNameFromSudoCommand(anyString())).thenCallRealMethod();
			mockedOsCommandHelper.when(() -> OsCommandHelper.toCaseInsensitiveRegex(anyString())).thenCallRealMethod();
			mockedOsCommandHelper
			.when(() -> OsCommandHelper
					.createOsCommandEmbeddedFiles(
							command,
							Collections.emptyMap(),
							osCommandConfiguration,
							TEMP_FILE_CREATOR
							)
					)
			.thenCallRealMethod();

			final String noPasswordCommand = " naviseccli -User user -Password ******** -Address host -Scope 1 getagent";

			mockedOsCommandHelper.when(() -> OsCommandHelper.runSshCommand(
					" naviseccli -User user -Password pwd -Address host -Scope 1 getagent",
					"host",
					sshConfiguration,
					120,
					Collections.emptyList(),
					noPasswordCommand))
			.thenReturn(result);

			mockedOsCommandHelper.when(() -> OsCommandHelper.runOsCommand(
					command,
					telemetryManager,
					120L,
					false,
					false)).thenCallRealMethod();

			final OsCommandResult expect = new OsCommandResult(result, noPasswordCommand);

			assertEquals(
					expect, 
					OsCommandHelper.runOsCommand(
							command,
							telemetryManager,
							120L,
							false,
							false));
		}
	}

	@Test
	void testRunOsCommandRemoteLinuxWithSudoReplaced() throws Exception {

		final String command = "%{SUDO:naviseccli} naviseccli -User %{USERNAME} -Password %{PASSWORD} -Address %{HOSTNAME} -Scope 1 getagent";
		final String result = "Agent Rev:";

		final SshConfiguration sshConfiguration = SshConfiguration.sshConfigurationBuilder()
				.username("user")
				.password("pwd".toCharArray())
				.build();

		final OsCommandConfiguration osCommandConfiguration = new OsCommandConfiguration();
		osCommandConfiguration.setUseSudo(true);
		osCommandConfiguration.setUseSudoCommands(Collections.singleton("naviseccli"));

		final HostConfiguration hostConfiguration = HostConfiguration
				.builder()
				.hostId("id")
				.hostname("host")
				.hostType(DeviceKind.LINUX)
				.configurations(Map.of(sshConfiguration.getClass(), sshConfiguration, osCommandConfiguration.getClass(), osCommandConfiguration))
				.build();

		final TelemetryManager telemetryManager = TelemetryManager
				.builder()
				.hostConfiguration(hostConfiguration)
				.build();

		try (final MockedStatic<OsCommandHelper> mockedOsCommandHelper = mockStatic(OsCommandHelper.class)) {
			mockedOsCommandHelper.when(() -> OsCommandHelper.getUsername(sshConfiguration)).thenCallRealMethod();
			mockedOsCommandHelper.when(() -> OsCommandHelper.getTimeout(120L, osCommandConfiguration, sshConfiguration, 300)).thenCallRealMethod();
			mockedOsCommandHelper.when(() -> OsCommandHelper.getPassword(sshConfiguration)).thenCallRealMethod();
			mockedOsCommandHelper.when(() -> OsCommandHelper.replaceSudo(anyString(), eq(osCommandConfiguration))).thenCallRealMethod();
			mockedOsCommandHelper.when(() -> OsCommandHelper.getFileNameFromSudoCommand(anyString())).thenCallRealMethod();
			mockedOsCommandHelper.when(() -> OsCommandHelper.toCaseInsensitiveRegex(anyString())).thenCallRealMethod();
			mockedOsCommandHelper
			.when(() -> OsCommandHelper
					.createOsCommandEmbeddedFiles(
							command,
							null,
							osCommandConfiguration,
							TEMP_FILE_CREATOR
							)
					)
			.thenCallRealMethod();

			final String noPasswordCommand = "sudo naviseccli -User user -Password ******** -Address host -Scope 1 getagent";

			mockedOsCommandHelper.when(() -> {
				OsCommandHelper.runSshCommand(
						"sudo naviseccli -User user -Password pwd -Address host -Scope 1 getagent",
						"host",
						sshConfiguration,
						120,
						Collections.emptyList(),
						noPasswordCommand);
			})
			.thenReturn(result);

			mockedOsCommandHelper.when(() -> OsCommandHelper.runOsCommand(
					command,
					telemetryManager,
					120L,
					false,
					false)).thenCallRealMethod();

			final OsCommandResult expect = new OsCommandResult(result, noPasswordCommand);

			assertEquals(
					expect, 
					OsCommandHelper.runOsCommand(
							command,
							telemetryManager,
							120L,
							false,
							false));
		}
	}

	@Test
	void testRunOsCommandRemoteLinuxWithEmbeddedFilesReplaced() throws Exception {

		final String embeddedContent = 
				"# Awk (or nawk)\n" + 
						"if [ -f /usr/xpg4/bin/awk ]; then\n" + 
						"	AWK=\"/usr/xpg4/bin/awk\";\n" + 
						"elif [ -f /usr/bin/nawk ]; then\n" + 
						"	AWK=\"/usr/bin/nawk\";\n" + 
						"else\n" + 
						"	AWK=\"awk\";\n" + 
						"fi\n" + 
						"if [ -f /opt/StorMan/arcconf ]; then\n" + 
						"       STORMAN=\"/opt/StorMan\";\n" + 
						"elif [ -f /usr/StorMan/arcconf ]; then\n" + 
						"       STORMAN=\"/usr/StorMan\";\n" + 
						"else\n" + 
						"	echo No Storman Installed; exit;\n" + 
						"fi\n" + 
						"DEVICES=`%{SUDO:/[opt|usr]/StorMan/arcconf} $STORMAN/arcconf getversion | $AWK '($1 ~ /Controller/ && $2 ~ /#[0-9]/) {controller=$2;gsub(/#/,\"\",controller);print(controller)}'`\n" + 
						"for CTRL in $DEVICES\n" + 
						"                do\n" + 
						"                echo MSHWController $CTRL\n" + 
						"                %{SUDO:/[opt|usr]/StorMan/arcconf} $STORMAN/arcconf getconfig $CTRL PD\n" + 
						"                done";

		final Map<String, EmbeddedFile> embeddedFiles = Collections.singletonMap("EmbeddedFile(1)", new EmbeddedFile(embeddedContent, null, 1));

		final File localFile = mock(File.class);
		final Map<String, File> embeddedTempFiles = new HashMap<>();
		embeddedTempFiles.put("EmbeddedFile(1)", localFile);

		final OsCommandConfiguration osCommandConfiguration = new OsCommandConfiguration();
		osCommandConfiguration.setUseSudo(true);
		osCommandConfiguration.setUseSudoCommands(Collections.singleton("/[opt|usr]/StorMan/arcconf"));

		final String command = "/bin/sh ${file::EmbeddedFile(1)}";
		final String result = "Hard drive";

		final SshConfiguration sshConfiguration = SshConfiguration.sshConfigurationBuilder()
				.username("user")
				.password("pwd".toCharArray())
				.build();

		final HostConfiguration hostConfiguration = HostConfiguration
				.builder()
				.hostId("id")
				.hostname("host")
				.hostType(DeviceKind.LINUX)
				.configurations(Map.of(sshConfiguration.getClass(), sshConfiguration, osCommandConfiguration.getClass(), osCommandConfiguration))
				.build();

		final TelemetryManager telemetryManager = TelemetryManager
				.builder()
				.hostConfiguration(hostConfiguration)
				.build();

		try (final MockedStatic<OsCommandHelper> mockedOsCommandHelper = mockStatic(OsCommandHelper.class)) {
			mockedOsCommandHelper.when(() -> OsCommandHelper.getUsername(sshConfiguration)).thenCallRealMethod();
			mockedOsCommandHelper.when(() -> OsCommandHelper.getTimeout(120L, osCommandConfiguration, sshConfiguration, 300)).thenCallRealMethod();
			mockedOsCommandHelper.when(() -> OsCommandHelper.getPassword(sshConfiguration)).thenCallRealMethod();
			mockedOsCommandHelper.when(() -> OsCommandHelper.toCaseInsensitiveRegex(anyString())).thenCallRealMethod();
			mockedOsCommandHelper.when(() -> OsCommandHelper.getFileNameFromSudoCommand(anyString())).thenCallRealMethod();
			mockedOsCommandHelper.when(() -> OsCommandHelper.replaceSudo(anyString(), eq(osCommandConfiguration))).thenCallRealMethod();
			mockedOsCommandHelper.when(() -> OsCommandHelper.findEmbeddedFiles(anyString())).thenReturn(embeddedFiles);
			mockedOsCommandHelper
			.when(() -> OsCommandHelper
					.createOsCommandEmbeddedFiles(
							command, 
							embeddedFiles, 
							osCommandConfiguration,
							TEMP_FILE_CREATOR
							)
					)
			.thenReturn(embeddedTempFiles);

			doReturn("C:\\Users\\user\\AppData\\Local\\Temp\\SEN_Embedded_0001").when(localFile).getAbsolutePath();

			final String updatedCommand = "/bin/sh ${file::C:\\Users\\user\\AppData\\Local\\Temp\\SEN_Embedded_0001}";

			mockedOsCommandHelper.when(() -> {
				OsCommandHelper.runSshCommand(
						updatedCommand,
						"host",
						sshConfiguration,
						120,
						List.of(localFile), 
						updatedCommand);
			})
			.thenReturn(result);

			mockedOsCommandHelper.when(() -> OsCommandHelper.runOsCommand(
					command,
					telemetryManager,
					120L,
					false,
					false)).thenCallRealMethod();

			final OsCommandResult expect = new OsCommandResult(result, updatedCommand);
			final OsCommandResult actual = OsCommandHelper.runOsCommand(
					command,
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

		Semaphore semaphore = SshSemaphoreFactory.getInstance().createOrGetSempahore("hostname");

		assertEquals(
				7,
				OsCommandHelper
				.runControlledSshCommand(
						semaphore::availablePermits,
						"hostname",
						30
						)
				);

		assertDoesNotThrow(
				() -> OsCommandHelper
				.runControlledSshCommand(
						LocalDate.MIN::toString,
						"hostname",
						30
						)
				);

		semaphore.acquire(8);
		assertThrows(
				ControlledSshException.class,
				() -> OsCommandHelper
				.runControlledSshCommand(
						LocalDate.MIN::toString,
						"hostname",
						1
						)
				);
	}
}
