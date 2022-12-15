package com.sentrysoftware.matrix.engine.strategy.utils;

import static org.springframework.util.Assert.isTrue;
import static org.springframework.util.Assert.notNull;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.IOException;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.StringJoiner;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

import com.sentrysoftware.matrix.common.exception.MatsyaException;
import com.sentrysoftware.matrix.common.exception.NoCredentialProvidedException;
import com.sentrysoftware.matrix.common.helpers.HardwareConstants;
import com.sentrysoftware.matrix.common.helpers.LocalOsHandler;
import com.sentrysoftware.matrix.connector.model.common.EmbeddedFile;
import com.sentrysoftware.matrix.engine.EngineConfiguration;
import com.sentrysoftware.matrix.engine.protocol.IProtocolConfiguration;
import com.sentrysoftware.matrix.engine.protocol.IWinProtocol;
import com.sentrysoftware.matrix.engine.protocol.OsCommandConfig;
import com.sentrysoftware.matrix.engine.protocol.SshProtocol;
import com.sentrysoftware.matrix.engine.strategy.matsya.MatsyaClientsExecutor;

import com.sentrysoftware.matrix.engine.host.HostType;
import io.opentelemetry.instrumentation.annotations.SpanAttribute;
import io.opentelemetry.instrumentation.annotations.WithSpan;
import lombok.NonNull;

public class OsCommandHelper {

	private OsCommandHelper() {
	}

	private static final Pattern SUDO_PATTERN = Pattern.compile("%\\{SUDO:([^\\}]*)\\}", Pattern.CASE_INSENSITIVE);

	private static final Pattern EMBEDDEDFILE_PATTERN = Pattern.compile("%EmbeddedFile\\((\\d+)\\)%", Pattern.CASE_INSENSITIVE);

	private static final String EMBEDDED_TEMP_FILE_PREFIX = "SEN_Embedded_";

	/**
	 * Create the temporary embedded files in the given command line.
	 * 
	 * @param commandLine The command line we wish to process.
	 * @param embeddedFiles The embedded files of the connector.
	 * @param osCommandConfig The OS Command Configuration.
	 * @return a Map with key, the EmbeddedFile tag and value a File for the temporary embedded file created.
	 * @throws IOException If an error occurred in the temp file creation.
	 */
	public static Map<String, File> createOsCommandEmbeddedFiles(
			@NonNull
			final String commandLine, 
			final Map<Integer, EmbeddedFile> embeddedFiles,
			final OsCommandConfig osCommandConfig) throws IOException {

		if (embeddedFiles == null) {
			return Collections.emptyMap();
		}

		final Map<String, File> embeddedTempFiles = new HashMap<>();
		try {
			final Matcher matcher = EMBEDDEDFILE_PATTERN.matcher(commandLine);
			while (matcher.find()) {
				// EmbeddedFile(index)
				final int index = Integer.parseInt(matcher.group(1));
				final String key = String.format("%%EmbeddedFile(%d)%%", index);

				embeddedTempFiles.computeIfAbsent(
						key,
						k -> {
							// The embedded file is available in the connector
							final EmbeddedFile embeddedFile = embeddedFiles.get(index);

							// This means there is a design problem or the HDF developer indicated a wrong embedded file
							notNull(embeddedFile, () -> "Cannot get the EmbeddedFile from the Connector. EmbeddedFile Index: " + index);
							final String content = embeddedFile.getContent();

							// This means there is a design problem, the content can never be null
							notNull(content, () -> "EmbeddedFile content is null. EmbeddedFile Index: " + index);

							try {
								return createEmbeddedFile(embeddedFile, osCommandConfig);
							} catch (final IOException e) {
								throw new RuntimeException(e);
							}
						});
			}
			return Collections.unmodifiableMap(embeddedTempFiles);

		} catch (final Exception e) {
			//noinspection ResultOfMethodCallIgnored
			embeddedTempFiles.values().forEach(File::delete);
			if (e instanceof RuntimeException && e.getCause() instanceof IOException) {
				throw (IOException) e.getCause();
			}
			throw e;
		}
	}

	/**
	 * Create a temporary file with the content of the embeddedFile.
	 * 
	 * @param embeddedFile (mandatory)
	 * @param osCommandConfig The OS Command Configuration.
	 * @return The File.
	 * @throws IOException
	 */
	static File createEmbeddedFile(
			final EmbeddedFile embeddedFile,
			final OsCommandConfig osCommandConfig) throws IOException {

		final String extension = embeddedFile.getType() != null ? 
				"." + embeddedFile.getType() : 
					HardwareConstants.EMPTY;

		final File tempFile = File.createTempFile(EMBEDDED_TEMP_FILE_PREFIX, extension);

		try (final BufferedWriter bufferedWriter = Files.newBufferedWriter(
				Paths.get(tempFile.getAbsolutePath()),
				StandardCharsets.UTF_8)) {
			bufferedWriter.write(
					replaceSudo(embeddedFile.getContent(), osCommandConfig));
		}
		return tempFile;
	}

	/**
	 * Replace the %{SUDO:xxx}% tag in the text.
	 * With xxx the sudo Command in OS Command configuration if useSudo is configured or Empty otherwise..
	 * 
	 * @param text
	 * @param osCommandConfig
	 * @return
	 */
	static String replaceSudo(
			final String text, 
			final OsCommandConfig osCommandConfig) {
		if (text == null || text.isBlank()) {
			return text;
		}

		final Optional<String> maybeSudoFile = getFileNameFromSudoCommand(text);

		final String sudoReplace = 
				maybeSudoFile.isPresent() && 
				osCommandConfig != null && 
				osCommandConfig.isUseSudo() &&
				osCommandConfig.getUseSudoCommands().contains(maybeSudoFile.get()) ?
						osCommandConfig.getSudoCommand() :
							HardwareConstants.EMPTY;

		return maybeSudoFile
				.map(fileName -> text.replaceAll(
						toCaseInsensitiveRegex(String.format("%%{SUDO:%s}", fileName)),
						sudoReplace))
				.orElse(text);
	}

	/**
	 * Run the given command on localhost machine.
	 *
	 * @param command The command.
	 * @param timeout The timeout in seconds.
	 * @param noPasswordCommand The command with the password masked.
	 * @return
	 * @throws InterruptedException
	 * @throws IOException
	 * @throws TimeoutException
	 */
	@WithSpan("OS Command")
	public static String runLocalCommand(
			@NonNull
			final String command,
			@SpanAttribute("OSCommand.timeout") final int timeout,
			@SpanAttribute("OSCommand.command") final String noPasswordCommand)
					throws InterruptedException, IOException, TimeoutException {
		isTrue(timeout > 0, "timeout mustn't be negative nor zero.");

		final String cmd = LocalOsHandler.isWindows() ? "CMD.EXE /C " + command : command;

		final Process process = Runtime.getRuntime().exec(cmd);
		if (process == null) {
			throw new IllegalStateException("Local command Process is null.");
		}

		final ExecutorService executor = Executors.newSingleThreadExecutor();

		final Future<String> future = executor.submit(() -> {

			try (final InputStreamReader inputStreamReader = new InputStreamReader(process.getInputStream());
					final BufferedReader bufferedReader = new BufferedReader(inputStreamReader)) {
				final StringJoiner stringJoiner = new StringJoiner(HardwareConstants.NEW_LINE);
				String line;
				while ((line = bufferedReader.readLine()) != null) {
					stringJoiner.add(line);
				}

				process.waitFor();

				return stringJoiner.toString();
			}
		});

		try {
			return future.get(timeout, TimeUnit.SECONDS);

		} catch (final TimeoutException e) {
			future.cancel(true);

			throw new TimeoutException(
					String.format("Command \"%s\" execution has timed out after %d s",
					noPasswordCommand != null ? noPasswordCommand : command,
					timeout));

		} catch (final ExecutionException e) {
			if (e.getCause() instanceof IOException) {
				throw (IOException) e.getCause();
			}
			return null;

		} finally {
			executor.shutdownNow();
		}
	}

	/**
	 * Run SSH command. Check if we can execute on localhost or remote
	 *
	 * @param command
	 * @param hostname
	 * @param sshProtocol
	 * @param timeout The timeout in seconds.
	 * @param localFiles
	 * @param noPasswordCommand
	 * @return
	 * @throws MatsyaException
	 */
	public static String runSshCommand(
			@NonNull
			final String command,
			@NonNull
			final String hostname,
			@NonNull
			final SshProtocol sshProtocol,
			final int timeout,
			final List<File> localFiles,
			final String noPasswordCommand) throws MatsyaException {
		isTrue(timeout > 0, "timeout mustn't be negative nor zero.");

		return MatsyaClientsExecutor.runRemoteSshCommand(
				hostname,
				sshProtocol.getUsername(),
				sshProtocol.getPassword(),
				sshProtocol.getPrivateKey(),
				command,
				timeout, 
				localFiles, 
				noPasswordCommand);
	}

	/**
	 * <p>Get the file name of the sudo pattern from the command.</p>
	 * <p>Example:</p>
	 * <p>"%{SUDO:fileName}" return "fileName"</p>
	 * @param command The command.
	 * @return An Optional with The file name if found otherwise an empty optional.
	 */
	static Optional<String> getFileNameFromSudoCommand(
			@NonNull
			final String command) {
		final Matcher matcher = SUDO_PATTERN.matcher(command);
		return matcher.find() ? 
				Optional.ofNullable(matcher.group(1)) :
					Optional.empty();
	}

	/**
	 * Convert a string to be searched in a case insensitive regex.
	 * 
	 * @param host The string to searched. (mandatory)
	 * @return The case insensitive regex for this string.
	 */
	public static String toCaseInsensitiveRegex(final String host) {
		isTrue(host != null && !host.isEmpty(), "host cannot be null nor empty.");
		return host.isBlank() ?
				host :
					"(?i)" + Pattern.quote(host);
	}

	/**
	 * <p>Get the timeout from:
	 * <li>First, the command timeout</li>
	 * <li>Then, the command configuration timeout</li>
	 * <li>Then, the protocol configuration timeout</li>
	 * <li>Finally, the default timeout</li>
	 * </p>
	 * @param commandTimeout The os command timeout in seconds.
	 * @param osCommandConfig
	 * @param protocolConfiguration
	 * @param defaultTimeout The default timeout in seconds.
	 * @return The timeout in seconds.
	 */
	public static int getTimeout(
			final Long commandTimeout,
			final OsCommandConfig osCommandConfig,
			final IProtocolConfiguration protocolConfiguration,
			final int defaultTimeout) {
		if (commandTimeout != null) {
			return commandTimeout.intValue();
		}
		if (osCommandConfig != null && osCommandConfig.getTimeout() != null) {
			return osCommandConfig.getTimeout().intValue();
		}
		if (protocolConfiguration == null) {
			return defaultTimeout;
		}
		final Long timeout = protocolConfiguration instanceof IWinProtocol ?
				((IWinProtocol) protocolConfiguration).getTimeout() :
					((SshProtocol) protocolConfiguration).getTimeout();
		return timeout != null ? timeout.intValue() : defaultTimeout;
	}

	/**
	 * Get the username from the IWinProtocol or the SSHProtocol.
	 * @param protocolConfiguration IWinProtocol or SSHProtocol.
	 * @return An optional with the username if found. An empty optional otherwise.
	 */
	public static Optional<String> getUsername(final IProtocolConfiguration protocolConfiguration) {
		if (protocolConfiguration == null) {
			return Optional.empty();
		}
		if (protocolConfiguration instanceof IWinProtocol) {
			return Optional.ofNullable(((IWinProtocol) protocolConfiguration).getUsername());
		}
		if (protocolConfiguration instanceof SshProtocol) {
			return Optional.ofNullable(((SshProtocol) protocolConfiguration).getUsername());
		}
		return Optional.empty();
	}

	/**
	 * Get the password from the IWinProtocol or the SSHProtocol.
	 * @param protocolConfiguration IWinProtocol or SSHProtocol.
	 * @return An optional with the password if found. An empty optional otherwise.
	 */
	public static Optional<char[]> getPassword(final IProtocolConfiguration protocolConfiguration) {
		if (protocolConfiguration == null) {
			return Optional.empty();
		}
		if (protocolConfiguration instanceof IWinProtocol) {
			return Optional.ofNullable(((IWinProtocol) protocolConfiguration).getPassword());
		}
		if (protocolConfiguration instanceof SshProtocol) {
			return Optional.ofNullable(((SshProtocol) protocolConfiguration).getPassword());
		}
		return Optional.empty();
	}

	/**
	 * <p>Run the OS Command on:
	 * <li>Local (use java Process)</li>
	 * <li>Remote windows (use WMI/WinRm command)</li>
	 * <li>Remote Linux (use SSH)<:li>
	 * <p>It replace Host name, User name, Password, Sudo, Embedded files macros in the command line.</p>
	 * <p>If necessary, it create embedded files and delete them after the command execution.</p>
	 * </p>
	 * 
	 * @param commandLine The command Line. (mandatory)
	 * @param engineConfiguration The engine configuration. (mandatory)
	 * @param embeddedFiles Embedded files.
	 * @param commandTimeout The OS command parameter for the timeout.
	 * @param isExecuteLocally The OS command parameter to indicate if the command should be execute locally.
	 * @param isLocalhost The parameter in Host Monitoring to indicate if the command is execute locally.
	 * @return The command execution return and the command with password masked (if present).
	 * @throws IOException When an I/O error occurred on local command execution or embedded file creation.
	 * @throws MatsyaException When an error occurred on a remote execution.
	 * @throws InterruptedException When the local command execution is interrupted.
	 * @throws TimeoutException When the local command execution ended in timeout.
	 * @throws NoCredentialProvidedException When there's no user provided for a remote command.
	 */
	public static OsCommandResult runOsCommand(
			@NonNull
			final String commandLine,
			@NonNull
			final EngineConfiguration engineConfiguration,
			final Map<Integer, EmbeddedFile> embeddedFiles,
			final Long commandTimeout,
			final boolean isExecuteLocally,
			final boolean isLocalhost) 
					throws IOException, 
					MatsyaException, 
					InterruptedException, 
					TimeoutException,
					NoCredentialProvidedException {

		final IProtocolConfiguration protocolConfiguration;
		if(!isLocalhost && engineConfiguration.getHost().getType() == HostType.MS_WINDOWS) {
			protocolConfiguration = engineConfiguration.getWinProtocol();
		} else {
			protocolConfiguration = engineConfiguration.getProtocolConfigurations().get(SshProtocol.class);
		}

		final Optional<String> maybeUsername = getUsername(protocolConfiguration);

		// If remote command and no username
		if ((maybeUsername.isEmpty() || maybeUsername.get().trim().isEmpty()) &&
				!isExecuteLocally && !isLocalhost) {
			throw new NoCredentialProvidedException();
		}

		final Optional<char[]> maybePassword = getPassword(protocolConfiguration);

		final String hostname = engineConfiguration.getHost().getHostname();

		final OsCommandConfig osCommandConfig = 
				(OsCommandConfig) engineConfiguration.getProtocolConfigurations().get(OsCommandConfig.class);

		final Map<String, File> embeddedTempFiles = createOsCommandEmbeddedFiles(
				commandLine, 
				embeddedFiles,
				osCommandConfig);

		final String updatedUserCommand = maybeUsername
				.map(username -> commandLine.replaceAll(
						toCaseInsensitiveRegex(HardwareConstants.USERNAME_MACRO), username))
				.orElse(commandLine);

		final String updatedHostnameCommand = updatedUserCommand.replaceAll(
				toCaseInsensitiveRegex(HardwareConstants.HOSTNAME_MACRO), hostname);

		final String updatedSudoCommand = replaceSudo(updatedHostnameCommand, osCommandConfig);

		final String updatedEmbeddedFilesCommand = embeddedTempFiles.entrySet().stream()
				.reduce(
						updatedSudoCommand,
						(s, entry) -> s.replaceAll(
								toCaseInsensitiveRegex(entry.getKey()),
								Matcher.quoteReplacement(entry.getValue().getAbsolutePath())),
						(s1, s2) -> null);

		final String command = maybePassword
				.map(password -> updatedEmbeddedFilesCommand.replaceAll(
						toCaseInsensitiveRegex(HardwareConstants.PASSWORD_MACRO), String.valueOf(password)))
				.orElse(updatedEmbeddedFilesCommand);

		final String noPasswordCommand = maybePassword
				.map(password -> updatedEmbeddedFilesCommand.replaceAll(
						toCaseInsensitiveRegex(HardwareConstants.PASSWORD_MACRO), "********"))
				.orElse(updatedEmbeddedFilesCommand);

		try {
			final int timeout = getTimeout(
					commandTimeout,
					osCommandConfig,
					protocolConfiguration,
					engineConfiguration.getOperationTimeout());

			final String commandResult;

			// Case local execution or command intended for a remote host but executed locally
			if (isLocalhost || isExecuteLocally) {
				final String localCommandResult = runLocalCommand(
						command,
						timeout,
						noPasswordCommand);
				commandResult = localCommandResult != null ?
						localCommandResult :
							HardwareConstants.EMPTY;

			// Case Windows Remote
			} else if (engineConfiguration.getHost().getType() == HostType.MS_WINDOWS) {
				commandResult = MatsyaClientsExecutor.executeWinRemoteCommand(
						command,
						protocolConfiguration,
						hostname,
						embeddedTempFiles.values().stream().map(File::getAbsolutePath).collect(Collectors.toList()));

			// Case others (Linux) Remote
			} else {
				commandResult = runSshCommand(
						command,
						hostname,
						(SshProtocol) protocolConfiguration,
						timeout,
						new ArrayList<>(embeddedTempFiles.values()),
						noPasswordCommand);
			}

			return new OsCommandResult(commandResult, noPasswordCommand);
		} finally {
			//noinspection ResultOfMethodCallIgnored
			embeddedTempFiles.values().forEach(File::delete);
		}
	}
}
