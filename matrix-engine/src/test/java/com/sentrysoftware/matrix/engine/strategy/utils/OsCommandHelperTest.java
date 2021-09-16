package com.sentrysoftware.matrix.engine.strategy.utils;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.junit.jupiter.api.condition.OS.LINUX;
import static org.junit.jupiter.api.condition.OS.WINDOWS;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.isNull;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.mockStatic;
import static org.mockito.Mockito.when;

import java.io.File;
import java.io.FilenameFilter;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.text.SimpleDateFormat;
import java.util.Collections;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.concurrent.TimeoutException;
import java.util.stream.Collectors;

import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.condition.EnabledOnOs;
import org.mockito.MockedStatic;

import com.sentrysoftware.matrix.common.helpers.HardwareConstants;
import com.sentrysoftware.matrix.common.helpers.LocalOSHandler;
import com.sentrysoftware.matrix.connector.model.Connector;
import com.sentrysoftware.matrix.connector.model.common.EmbeddedFile;
import com.sentrysoftware.matrix.engine.protocol.OSCommandConfig;
import com.sentrysoftware.matrix.engine.protocol.SSHProtocol;
import com.sentrysoftware.matrix.engine.protocol.WMIProtocol;
import com.sentrysoftware.matrix.engine.strategy.matsya.MatsyaClientsExecutor;

class OsCommandHelperTest {

	private static final Date TODAY = new Date();

	private static final char[] PASSWORD = {'p', 'w', 'd'};

	private static final String EMBEDDED_TEMP_FILE_PREFIX = "SEN_Embedded_";

	@AfterAll
	static void cleanTempEmbeddedFiles() {
		for (final File file : getTempEmbeddedFiles()) {
			file.delete();
		}
	}

	private static File[] getTempEmbeddedFiles() {
		final File tempDirectory = new File(System.getProperty("java.io.tmpdir"));

		return  tempDirectory.listFiles(new FilenameFilter() {
			@Override
			public boolean accept(final File directory, final String fileName) {
				return fileName.startsWith(EMBEDDED_TEMP_FILE_PREFIX);
			}
		});
	}

	private static void checkNoTempEmbeddedFileExist() {
		assertEquals(0, getTempEmbeddedFiles().length);
	}

	@Test
	void testUpdateOsCommandEmbeddedFile() {
		final String commandLine = "copy %EmbeddedFile(1)% %EmbeddedFile(1)%.bat > NUL & %EmbeddedFile(1)%.bat %{USERNAME} %{PASSWORD} %{HOSTNAME} & del /F /Q %EmbeddedFile(1)%.bat";

		assertThrows(IllegalArgumentException.class, () -> OsCommandHelper.updateOsCommandEmbeddedFile(null, mock(Connector.class)));
		assertThrows(IllegalArgumentException.class, () -> OsCommandHelper.updateOsCommandEmbeddedFile(commandLine, null));

		assertEquals("", OsCommandHelper.updateOsCommandEmbeddedFile("", mock(Connector.class)));
		assertEquals("cmd", OsCommandHelper.updateOsCommandEmbeddedFile("cmd", mock(Connector.class)));

		// case embeddedFile not found
		{
			final Connector connector = mock(Connector.class);
			final EmbeddedFile embeddedFile = mock(EmbeddedFile.class);
			final Map<Integer, EmbeddedFile> embeddedFiles = Collections.singletonMap(2, embeddedFile);

			when(connector.getEmbeddedFiles()).thenReturn(embeddedFiles);

			assertThrows(IllegalArgumentException.class, () -> OsCommandHelper.updateOsCommandEmbeddedFile(commandLine, connector));
		}

		// case embeddedFile content null
		{
			final Connector connector = mock(Connector.class);
			final EmbeddedFile embeddedFile = mock(EmbeddedFile.class);
			final Map<Integer, EmbeddedFile> embeddedFiles = Collections.singletonMap(1, embeddedFile);

			when(connector.getEmbeddedFiles()).thenReturn(embeddedFiles);
			when(embeddedFile.getContent()).thenReturn(null);

			assertThrows(IllegalArgumentException.class, () -> OsCommandHelper.updateOsCommandEmbeddedFile(commandLine, connector));
		}

		// case OK
		{
			final Connector connector = mock(Connector.class);
			final EmbeddedFile embeddedFile = mock(EmbeddedFile.class);
			final Map<Integer, EmbeddedFile> embeddedFiles = Collections.singletonMap(1, embeddedFile);

			when(connector.getEmbeddedFiles()).thenReturn(embeddedFiles);
			when(embeddedFile.getContent()).thenReturn("file");

			assertEquals(
					"copy file file.bat > NUL & file.bat %{USERNAME} %{PASSWORD} %{HOSTNAME} & del /F /Q file.bat",
					OsCommandHelper.updateOsCommandEmbeddedFile(commandLine, connector));
		}
	}

	@Test
	void testCreateOsCommandEmbeddedFiles() throws Exception {

		final String commandLine = "copy %EmbeddedFile(1)% %EmbeddedFile(1)%.bat > NUL & %EmbeddedFile(2)%.bat %{USERNAME} %{PASSWORD} %{HOSTNAME} & del /F /Q %EmbeddedFile(1)%.bat & del /F /Q %EmbeddedFile(2)%.bat ";

		checkNoTempEmbeddedFileExist();

		assertThrows(IllegalArgumentException.class, () -> OsCommandHelper.createOsCommandEmbeddedFiles(null, Collections.emptyMap(), null));

		assertEquals(Collections.emptyMap(), OsCommandHelper.createOsCommandEmbeddedFiles(commandLine, null, null));
		assertEquals(Collections.emptyMap(), OsCommandHelper.createOsCommandEmbeddedFiles("", Collections.emptyMap(), null));
		assertEquals(Collections.emptyMap(), OsCommandHelper.createOsCommandEmbeddedFiles("cmd", Collections.emptyMap(), null));

		// case embeddedFile not found
		assertThrows(IllegalArgumentException.class, () -> OsCommandHelper.createOsCommandEmbeddedFiles(commandLine, Collections.singletonMap(3, new EmbeddedFile()), null));

		// case embeddedFile content null
		assertThrows(IllegalArgumentException.class, () -> OsCommandHelper.createOsCommandEmbeddedFiles(commandLine, Collections.singletonMap(1, new EmbeddedFile()), null));

		checkNoTempEmbeddedFileExist();

		// case IOException in temp file creation
		try (final MockedStatic<OsCommandHelper> mockedOsCommandHelper = mockStatic(OsCommandHelper.class)) {

			final Map<Integer, EmbeddedFile> embeddedFiles = new HashMap<>();
			embeddedFiles.put(1, new EmbeddedFile("ECHO %OS%", "bat"));
			embeddedFiles.put(2, new EmbeddedFile("echo Hello World", null));

			mockedOsCommandHelper.when(() -> OsCommandHelper.createEmbeddedFile(any(EmbeddedFile.class), isNull())).thenThrow(IOException.class);
			mockedOsCommandHelper.when(() -> OsCommandHelper.createOsCommandEmbeddedFiles(commandLine, embeddedFiles, null)).thenCallRealMethod();

			assertThrows(IOException.class, () -> OsCommandHelper.createOsCommandEmbeddedFiles(commandLine, embeddedFiles, null));
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

			final Map<Integer, EmbeddedFile> embeddedFiles = new HashMap<>();
			embeddedFiles.put(1, new EmbeddedFile("ECHO %OS%", "bat"));
			embeddedFiles.put(2, new EmbeddedFile(embeddedContent, null));

			final Map<String, File> embeddedTempFiles = OsCommandHelper.createOsCommandEmbeddedFiles(
					commandLine,
					embeddedFiles,
					OSCommandConfig.builder()
						.useSudo(true)
						.useSudoCommands(Set.of("/[opt|usr]/StorMan/arcconf"))
						.build());

			assertEquals(2, embeddedTempFiles.size());

			{
				final File file = embeddedTempFiles.get("%EmbeddedFile(1)%");
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

				final File file = embeddedTempFiles.get("%EmbeddedFile(2)%");
				assertNotNull(file);
				assertTrue(file.exists());
				assertTrue(file.getName().matches(EMBEDDED_TEMP_FILE_PREFIX+"\\w+"));
				assertEquals(
						expectedContent.replaceAll("[\r\n]", HardwareConstants.EMPTY),
						Files.readAllLines(Paths.get(file.getAbsolutePath())).stream().collect(Collectors.joining()));
				file.delete();
			}
		}
	}

	@Test
	void testReplaceSudo() {

		assertNull(OsCommandHelper.replaceSudo(null, null));
		assertNull(OsCommandHelper.replaceSudo(null, OSCommandConfig.builder().build()));

		assertEquals("", OsCommandHelper.replaceSudo("", null));
		assertEquals("", OsCommandHelper.replaceSudo("", OSCommandConfig.builder().build()));
		assertEquals(" ", OsCommandHelper.replaceSudo(" ", null));
		assertEquals(" ", OsCommandHelper.replaceSudo(" ", OSCommandConfig.builder().build()));

		assertEquals("text", OsCommandHelper.replaceSudo("text", null));
		assertEquals("text", OsCommandHelper.replaceSudo("text", OSCommandConfig.builder().build()));

		// Check replace sudo tag with empty string.
		assertEquals(" key", OsCommandHelper.replaceSudo("%{SUDO:key} key", null));
		assertEquals(" key", OsCommandHelper.replaceSudo("%{SUDO:key} key", OSCommandConfig.builder().build()));
		assertEquals(" key", OsCommandHelper.replaceSudo("%{SUDO:key} key", OSCommandConfig.builder().useSudo(true).build()));
		assertEquals(" key\n key", OsCommandHelper.replaceSudo("%{SUDO:key} key\n%{SUDO:key} key", OSCommandConfig.builder().useSudo(true).build()));

		assertEquals("sudo key", OsCommandHelper.replaceSudo(
				"%{SUDO:key} key",
				OSCommandConfig.builder().useSudo(true).useSudoCommands(Set.of("key")).build()));

		assertEquals("sudo key\nsudo key", OsCommandHelper.replaceSudo(
				"%{SUDO:key} key\n%{SUDO:key} key",
				OSCommandConfig.builder().useSudo(true).useSudoCommands(Set.of("key")).build()));
	}

	@Test
	void testRunLocalCommandKO() throws Exception {
		final String command = "cmd";

		assertThrows(IllegalArgumentException.class, () -> OsCommandHelper.runLocalCommand(null, 1, null));
		assertThrows(IllegalArgumentException.class, () -> OsCommandHelper.runLocalCommand(command, -1, null));
		assertThrows(IllegalArgumentException.class, () -> OsCommandHelper.runLocalCommand(command, 0, null));

		// case Process null Linux
		final Runtime runtime = mock(Runtime.class);
		try (final MockedStatic<LocalOSHandler> mockedLocalOSHandler = mockStatic(LocalOSHandler.class);
				final MockedStatic<Runtime> mockedRuntime = mockStatic(Runtime.class)) {
			mockedLocalOSHandler.when(LocalOSHandler::isWindows).thenReturn(false);
			mockedRuntime.when(Runtime::getRuntime).thenReturn(runtime);
			when(runtime.exec(command)).thenReturn(null);

			assertThrows(IllegalStateException.class, () -> OsCommandHelper.runLocalCommand(command, 1, null));
		}

		// case Process null Windows
		try (final MockedStatic<LocalOSHandler> mockedLocalOSHandler = mockStatic(LocalOSHandler.class);
				final MockedStatic<Runtime> mockedRuntime = mockStatic(Runtime.class)) {
			mockedLocalOSHandler.when(LocalOSHandler::isWindows).thenReturn(true);
			mockedRuntime.when(Runtime::getRuntime).thenReturn(runtime);
			when(runtime.exec("CMD.EXE /C cmd")).thenReturn(null);

			assertThrows(IllegalStateException.class, () -> OsCommandHelper.runLocalCommand(command, 1, null));
		}
	}

	@Test
	@EnabledOnOs(WINDOWS)
	void testRunLocalCommandWindowsTimeout() throws Exception {
		assertThrows(TimeoutException.class, () -> OsCommandHelper.runLocalCommand("PAUSE", 1, null));
	}

	@Test
	@EnabledOnOs(LINUX)
	void testRunLocalCommandLinuxTimeout() throws Exception {
		assertThrows(TimeoutException.class, () -> OsCommandHelper.runLocalCommand("sleep 5", 1, null));
	}

	@Test
	@EnabledOnOs(WINDOWS)
	void testRunLocalCommandWindows() throws Exception {
		assertTrue(OsCommandHelper.runLocalCommand("echo test", 1, null).matches("^test$"));
	}

	@Test
	@EnabledOnOs(LINUX)
	void testRunLocalCommandLinux() throws Exception {
		assertEquals(
				"\"" + new SimpleDateFormat("ddMMyy").format(TODAY) + "\"",
				OsCommandHelper.runLocalCommand("date +\"%d%m%y\"", 1, null));
	}

	@Test
	void testRunSshCommand() throws Exception {
		final String command = "cmd";
		final String hostname = "host";
		final SSHProtocol sshProtocol = mock(SSHProtocol.class);
		final int timeout = 1000;

		assertThrows(IllegalArgumentException.class, () -> OsCommandHelper.runSshCommand(null, hostname, sshProtocol, timeout, null, null));
		assertThrows(IllegalArgumentException.class, () -> OsCommandHelper.runSshCommand(command, null, sshProtocol, timeout, null, null));
		assertThrows(IllegalArgumentException.class, () -> OsCommandHelper.runSshCommand(command, hostname, null, timeout, null, null));
		assertThrows(IllegalArgumentException.class, () -> OsCommandHelper.runSshCommand(command, hostname, sshProtocol, -1, null, null));
		assertThrows(IllegalArgumentException.class, () -> OsCommandHelper.runSshCommand(command, hostname, sshProtocol, 0, null, null));

		try (final MockedStatic<MatsyaClientsExecutor> mockedMatsyaClientsExecutor = mockStatic(MatsyaClientsExecutor.class)) {

			when(sshProtocol.getUsername()).thenReturn("user");
			when(sshProtocol.getPassword()).thenReturn(PASSWORD);

			mockedMatsyaClientsExecutor.when(() -> MatsyaClientsExecutor.runRemoteSshCommand(
					hostname,
					"user",
					String.valueOf(PASSWORD),
					null,
					command,
					timeout,
					null,
					null)).thenReturn("result");

			assertEquals("result", OsCommandHelper.runSshCommand(command, hostname, sshProtocol, timeout, null, null));
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
		assertEquals("(?i)\\QTarget\\E", OsCommandHelper.toCaseInsensitiveRegex("Target"));
		assertEquals("(?i)\\Q%{UserName}\\E", OsCommandHelper.toCaseInsensitiveRegex("%{UserName}"));
		assertEquals("(?i)\\Q%{HOSTNAME}\\E", OsCommandHelper.toCaseInsensitiveRegex(HardwareConstants.HOSTNAME_MACRO));
	}

	@Test
	void testGetTimeout() {
		final OSCommandConfig osCommandConfig = new OSCommandConfig();
		osCommandConfig.setTimeout(2L);

		final WMIProtocol wmiProtocol = new WMIProtocol();
		wmiProtocol.setTimeout(3L);

		final SSHProtocol sshProtocol = SSHProtocol.builder().username("user").password(PASSWORD).build();
		sshProtocol.setTimeout(4L);

		assertEquals(1, OsCommandHelper.getTimeout(1L, osCommandConfig, sshProtocol, 5));
		assertEquals(2, OsCommandHelper.getTimeout(null, osCommandConfig, sshProtocol, 5));
		assertEquals(3, OsCommandHelper.getTimeout(null, null, wmiProtocol, 5));
		assertEquals(4, OsCommandHelper.getTimeout(null, null, sshProtocol, 5));
		assertEquals(5, OsCommandHelper.getTimeout(null, null, null, 5));
		assertEquals(30, OsCommandHelper.getTimeout(null, new OSCommandConfig(), sshProtocol, 5));
		assertEquals(120, OsCommandHelper.getTimeout(null, null, new WMIProtocol(), 5));
		assertEquals(30, OsCommandHelper.getTimeout(null, null, SSHProtocol.builder().username("user").password(PASSWORD).build(), 5));
	}

	@Test
	void testGetUsername() {
		assertEquals(Optional.empty(), OsCommandHelper.getUsername(null));
		assertEquals(Optional.empty(), OsCommandHelper.getUsername(new OSCommandConfig()));
		assertEquals(Optional.empty(), OsCommandHelper.getUsername(new WMIProtocol()));
		assertEquals(Optional.empty(), OsCommandHelper.getUsername(SSHProtocol.builder().password(PASSWORD).build()));

		final WMIProtocol wmiProtocol = new WMIProtocol();
		wmiProtocol.setUsername("user");
		assertEquals(Optional.of("user"), OsCommandHelper.getUsername(wmiProtocol));

		assertEquals(Optional.of("user"), OsCommandHelper.getUsername(SSHProtocol.builder().username("user").password(PASSWORD).build()));
	}

	@Test
	void testGetPassword() {
		assertEquals(Optional.empty(), OsCommandHelper.getPassword(null));
		assertEquals(Optional.empty(), OsCommandHelper.getPassword(new OSCommandConfig()));
		assertEquals(Optional.empty(), OsCommandHelper.getPassword(new WMIProtocol()));
		assertEquals(Optional.empty(), OsCommandHelper.getPassword(SSHProtocol.builder().username("user").build()));

		final WMIProtocol wmiProtocol = new WMIProtocol();
		wmiProtocol.setPassword(PASSWORD);
		assertEquals(Optional.of(PASSWORD), OsCommandHelper.getPassword(wmiProtocol));

		assertEquals(Optional.of(PASSWORD), OsCommandHelper.getPassword(SSHProtocol.builder().username("user").password(PASSWORD).build()));
	}
}
