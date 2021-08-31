package com.sentrysoftware.matrix.engine.strategy.utils;

import static org.springframework.util.Assert.notNull;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.Arrays;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

import com.sentrysoftware.matrix.connector.model.Connector;
import com.sentrysoftware.matrix.connector.model.common.EmbeddedFile;
import com.sentrysoftware.matrix.engine.protocol.SSHProtocol;
import com.sentrysoftware.matrix.engine.strategy.matsya.MatsyaClientsExecutor;

public class OsCommandHelper {

	private OsCommandHelper() {
	}

	private static final Pattern EMBEDDEDFILE_REPLACEMENT_PATTERN = Pattern.compile("%EmbeddedFile\\((\\d+)\\)%", Pattern.CASE_INSENSITIVE);

	/**
	 * Whether we're running on Windows or not
	 */
	private static final boolean IS_WINDOWS =
			System.getProperty("os.name").toLowerCase().startsWith("windows");

	/**
	 * Replace the content of the EmbeddedFiles in the given command line
	 * 
	 * @param commandLine The command line we wish to process
	 * @param connector   The connector instance which defines the embedded files.
	 * @return Updated command line value
	 */
	public static String updateOsCommandEmbeddedFile(final String commandLine, final Connector connector) {

		notNull(commandLine, "commandLine cannot be null.");
		notNull(connector, "connector cannot be null.");

		final Matcher matcher = EMBEDDEDFILE_REPLACEMENT_PATTERN.matcher(commandLine);

		final StringBuilder sb = new StringBuilder();
		while (matcher.find()) {
			// EmbeddedFile(embeddedFileIndex)
			final Integer embeddedFileIndex = Integer.parseInt(matcher.group(1));

			// The embedded file is available in the connector
			final EmbeddedFile embeddedFile = connector.getEmbeddedFiles().get(embeddedFileIndex);

			// This means there is a design problem or the HDF developer indicated a wrong embedded file
			notNull(embeddedFile, () -> "Cannot get the EmbeddedFile from the Connector. EmbeddedFile Index: " + embeddedFileIndex);
			final String embeddedFileContent = embeddedFile.getContent();

			// This means there is a design problem, the content can never be null
			notNull(embeddedFileContent, () -> "EmbeddedFile content is null. EmbeddedFile Index: " + embeddedFileIndex);
			matcher.appendReplacement(sb, embeddedFileContent);
		}

		matcher.appendTail(sb);

		return sb.toString();
	}


	/**
	 * @return whether current system is Windows or not
	 */
	public static boolean isWindows() {
		return IS_WINDOWS;
	}

	/**
	 * Run the given command on localhost machine
	 * @param command
	 * @return
	 * @throws InterruptedException
	 * @throws IOException
	 */
	public static String runLocalCommand(String command)
			throws InterruptedException, IOException {

		if (command == null) {
			return null;
		}
		Process process = null;

		if (IS_WINDOWS) {
			command = "CMD.EXE /C " + command;
		}

		process = Runtime.getRuntime().exec(command);

		if (process != null) {
			process.waitFor();
			BufferedReader reader = new BufferedReader(new InputStreamReader(process.getInputStream()));

			return reader.lines().collect(Collectors.joining("\n")).trim();
		}

		return null;
	}
	

	/**
	 * Run SSH command. Check if we can execute on localhost or remote
	 * 
	 * @param ipmitoolCommand
	 * @param hostname
	 * @param sshProtocol
	 * @param timeout
	 * @param matsyaClientsExecutor 
	 * @return
	 * @throws InterruptedException
	 * @throws IOException
	 */
	public static String runSshCommand(String ipmitoolCommand, 
			final String hostname, 
			final SSHProtocol sshProtocol,
			final int timeout, 
			MatsyaClientsExecutor matsyaClientsExecutor) throws IOException {
		String result;

		if (ipmitoolCommand == null || sshProtocol == null || matsyaClientsExecutor == null) {
			return null;
		}
		String keyFilePath = sshProtocol.getPrivateKey() == null ? null : sshProtocol.getPrivateKey().getAbsolutePath();
		result = matsyaClientsExecutor.runRemoteSshCommand(hostname, sshProtocol.getUsername(),
				Arrays.toString(sshProtocol.getPassword()), keyFilePath, ipmitoolCommand, timeout);

		return result;
	}

}
