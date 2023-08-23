package com.sentrysoftware.matrix.strategy.utils;

import static com.sentrysoftware.matrix.common.helpers.MatrixConstants.DEFAULT_LOCK_TIMEOUT;
import static com.sentrysoftware.matrix.common.helpers.MatrixConstants.EMPTY;
import static com.sentrysoftware.matrix.common.helpers.MatrixConstants.HOSTNAME_MACRO;
import static com.sentrysoftware.matrix.common.helpers.MatrixConstants.NEW_LINE;
import static com.sentrysoftware.matrix.common.helpers.MatrixConstants.PASSWORD_MACRO;
import static com.sentrysoftware.matrix.common.helpers.MatrixConstants.USERNAME_MACRO;
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
import java.util.concurrent.Semaphore;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;
import java.util.function.Function;
import java.util.function.Supplier;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import com.sentrysoftware.matrix.common.exception.ControlledSshException;
import com.sentrysoftware.matrix.common.exception.MatsyaException;
import com.sentrysoftware.matrix.common.exception.MatsyaRuntimeException;
import com.sentrysoftware.matrix.common.exception.NoCredentialProvidedException;
import com.sentrysoftware.matrix.common.helpers.LocalOsHandler;
import com.sentrysoftware.matrix.configuration.IConfiguration;
import com.sentrysoftware.matrix.configuration.IWinConfiguration;
import com.sentrysoftware.matrix.configuration.OsCommandConfiguration;
import com.sentrysoftware.matrix.configuration.SshConfiguration;
import com.sentrysoftware.matrix.connector.model.common.DeviceKind;
import com.sentrysoftware.matrix.connector.model.common.EmbeddedFile;
import com.sentrysoftware.matrix.matsya.MatsyaClientsExecutor;
import com.sentrysoftware.matrix.telemetry.SshSemaphoreFactory;
import com.sentrysoftware.matrix.telemetry.TelemetryManager;

import io.opentelemetry.instrumentation.annotations.SpanAttribute;
import io.opentelemetry.instrumentation.annotations.WithSpan;
import lombok.AccessLevel;
import lombok.NoArgsConstructor;
import lombok.NonNull;

@NoArgsConstructor(access = AccessLevel.PRIVATE)
public class OsCommandHelper {

	static final Function<String, File> TEMP_FILE_CREATOR = OsCommandHelper::createEmbeddedTempFile;

	private static final Pattern SUDO_PATTERN = Pattern.compile("%\\{SUDO:([^\\}]*)\\}", Pattern.CASE_INSENSITIVE);

	private static final Pattern EMBEDDEDFILE_PATTERN = Pattern.compile("%EmbeddedFile\\((\\d+)\\)%", Pattern.CASE_INSENSITIVE);

	private static final String EMBEDDED_TEMP_FILE_PREFIX = "SEN_Embedded_";

	/**
	 * Create the temporary embedded files in the given command line.
	 *
	 * @param commandLine     The command line we wish to process.
	 * @param embeddedFiles   The embedded files of the connector.
	 * @param osCommandConfiguration The OS Command Configuration.
	 * @param tempFileCreator The function that creates a temporary file.
	 * @return a Map with key, the EmbeddedFile tag and value a File for the temporary embedded file created.
	 * @throws IOException If an error occurred in the temp file creation.
	 */
	public static Map<String, File> createOsCommandEmbeddedFiles(
			@NonNull final String commandLine,
			final Map<Integer, EmbeddedFile> embeddedFiles,
			final OsCommandConfiguration osCommandConfiguration,
			final Function<String, File> tempFileCreator
	) throws IOException {

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
								return createTempFileWithEmbeddedFileContent(embeddedFile, osCommandConfiguration, tempFileCreator);
							} catch (final IOException e) {
								throw new TempFileCreationException(e);
							}
						});
			}
			return Collections.unmodifiableMap(embeddedTempFiles);

		} catch (final Exception e) {
			//noinspection ResultOfMethodCallIgnored
			embeddedTempFiles.values().forEach(File::delete);
			if (e instanceof TempFileCreationException) {
				throw (IOException) e.getCause();
			}
			throw e;
		}
	}

	/**
	 * Create a temporary file with the content of the embeddedFile.
	 *
	 * @param embeddedFile    {@link EmbeddedFile} instance used to write the file content (mandatory)
	 * @param osCommandConfig The OS Command Configuration.
	 * @param tempFileCreator The function that creates a temporary file.
	 * @return The File.
	 * @throws IOException
	 */
	static File createTempFileWithEmbeddedFileContent(
			final EmbeddedFile embeddedFile,
			final OsCommandConfiguration osCommandConfig,
			Function<String, File> tempFileCreator
	) throws IOException {

		final String extension = embeddedFile.getType() != null ?
				"." + embeddedFile.getType() :
				EMPTY;

		final File tempFile = tempFileCreator.apply(extension);

		try (final BufferedWriter bufferedWriter = Files.newBufferedWriter(
				Paths.get(tempFile.getAbsolutePath()),
				StandardCharsets.UTF_8)) {
			bufferedWriter.write(
					replaceSudo(embeddedFile.getContent(), osCommandConfig));
		}
		return tempFile;
	}

	/**
	 * Create a temporary file with the given extension.<br>
	 * The temporary file name is prefixed with {@value #EMBEDDED_TEMP_FILE_PREFIX}
	 *
	 * @param extension File's name suffix (e.g. .bat)
	 * @return {@link File} instance
	 */
	static File createEmbeddedTempFile(final String extension) {
		try {
			return File.createTempFile(EMBEDDED_TEMP_FILE_PREFIX, extension);
		} catch (IOException e) {
			throw new TempFileCreationException(e);
		}
	}

	/**
	 * Replace the %{SUDO:xxx}% tag in the text.
	 * With xxx the sudo Command in OS Command configuration if useSudo is configured or Empty otherwise..
	 *
	 * @param text
	 * @param osCommandConfiguration
	 * @return
	 */
	static String replaceSudo(
			final String text,
			final OsCommandConfiguration osCommandConfiguration) {
		if (text == null || text.isBlank()) {
			return text;
		}

		final Optional<String> maybeSudoFile = getFileNameFromSudoCommand(text);

		final String sudoReplace =
				maybeSudoFile.isPresent() &&
						osCommandConfiguration != null &&
						osCommandConfiguration.isUseSudo() &&
						osCommandConfiguration.getUseSudoCommands().contains(maybeSudoFile.get()) ?
						osCommandConfiguration.getSudoCommand() :
						EMPTY;

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
			@NonNull final String command,
			@SpanAttribute("OSCommand.timeout") final long timeout,
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
				final StringJoiner stringJoiner = new StringJoiner(NEW_LINE);
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
			final Throwable cause = e.getCause();
			if (cause instanceof IOException ioException) {
				throw ioException;
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
	 * @param sshConfiguration
	 * @param timeout The timeout in seconds.
	 * @param localFiles
	 * @param noPasswordCommand
	 * @return
	 * @throws MatsyaException
	 * @throws InterruptedException
	 */
	public static String runSshCommand(
			@NonNull final String command,
			@NonNull final String hostname,
			@NonNull final SshConfiguration sshConfiguration,
			final long timeout,
			final List<File> localFiles,
			final String noPasswordCommand) throws MatsyaException, InterruptedException, ControlledSshException {
		isTrue(timeout > 0, "timeout mustn't be negative nor zero.");

		try {

			return runControlledSshCommand(() -> {
						try {
							return MatsyaClientsExecutor
									.runRemoteSshCommand(
											hostname,
											sshConfiguration.getUsername(),
											sshConfiguration.getPassword(),
											sshConfiguration.getPrivateKey(),
											command,
											timeout,
											localFiles,
											noPasswordCommand
									);
						} catch (MatsyaException e) {
							throw new MatsyaRuntimeException(e);
						}
					},
					hostname,
					DEFAULT_LOCK_TIMEOUT
			);
		}
		catch (final MatsyaRuntimeException e) {
			throw (MatsyaException) e.getCause();
		}
	}

	/**
	 * Run controlled SSH command. Check if we can obtain a permit from the semaphore before running the command
	 *
	 * @param <T>
	 * @param executable
	 * @param hostname
	 * @param timeout
	 * @return The executable result (e.g. a {@link String} value)
	 * @throws InterruptedException
	 * @throws ControlledSshException
	 */
	static <T> T runControlledSshCommand(
			Supplier<T> executable,
			String hostname,
			int timeout) throws InterruptedException, ControlledSshException {

		final Semaphore semaphore = SshSemaphoreFactory.getInstance().createOrGetSempahore(hostname);

		try {
			if (semaphore.tryAcquire(timeout, TimeUnit.SECONDS)) {

				return executable.get();

			}

			final String message = String.format(
					"Failed to run SSH command on %s. Timed out trying to get ssh semaphore permit.",
					hostname
			);

			throw new ControlledSshException(message);

		} finally {
			semaphore.release();
		}
	}

	/**
	 * <p>Get the file name of the sudo pattern from the command.</p>
	 * <p>Example:</p>
	 * <p>"%{SUDO:fileName}" return "fileName"</p>
	 * @param command The command.
	 * @return An Optional with The file name if found otherwise an empty optional.
	 */
	static Optional<String> getFileNameFromSudoCommand(@NonNull final String command) {
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
	 * @param osCommandConfiguration
	 * @param configuration
	 * @param defaultTimeout The default timeout in seconds.
	 * @return The timeout in seconds.
	 */
	public static long getTimeout(
			final Long commandTimeout,
			final OsCommandConfiguration osCommandConfiguration,
			final IConfiguration configuration,
			final long defaultTimeout) {
		if (commandTimeout != null) {
			return commandTimeout.intValue();
		}
		if (osCommandConfiguration != null && osCommandConfiguration.getTimeout() != null) {
			return osCommandConfiguration.getTimeout().intValue();
		}
		if (configuration == null) {
			return defaultTimeout;
		}
		final Long timeout = configuration instanceof IWinConfiguration winConfiguration ?
			winConfiguration.getTimeout() :
			((SshConfiguration) configuration).getTimeout();
		return timeout != null ? timeout : defaultTimeout;
	}

	/**
	 * Get the username from the IWinProtocol or the SSHProtocol.
	 * @param configuration IWinProtocol or SSHProtocol.
	 * @return An optional with the username if found. An empty optional otherwise.
	 */
	public static Optional<String> getUsername(final IConfiguration configuration) {
		if (configuration == null) {
			return Optional.empty();
		}
		if (configuration instanceof IWinConfiguration winConfiguration) {
			return Optional.ofNullable(winConfiguration.getUsername());
		}
		if (configuration instanceof SshConfiguration sshConfiguration) {
			return Optional.ofNullable(sshConfiguration.getUsername());
		}
		return Optional.empty();
	}

	/**
	 * Get the password from the IWinProtocol or the SSHProtocol.
	 * @param protocolConfiguration IWinProtocol or SSHProtocol.
	 * @return An optional with the password if found. An empty optional otherwise.
	 */
	public static Optional<char[]> getPassword(final IConfiguration protocolConfiguration) {
		if (protocolConfiguration == null) {
			return Optional.empty();
		}
		if (protocolConfiguration instanceof IWinConfiguration winConfiguration) {
			return Optional.ofNullable(winConfiguration.getPassword());
		}
		if (protocolConfiguration instanceof SshConfiguration sshConfiguration) {
			return Optional.ofNullable(sshConfiguration.getPassword());
		}
		return Optional.empty();
	}

	/**
	 * <p>Run the OS Command on:
	 * <li>Local (use java Process)</li>
	 * <li>Remote windows (use WMI/WinRm command)</li>
	 * <li>Remote Linux (use SSH)<:li>
	 * <p>It replaces Host name, User name, Password, Sudo, Embedded files macros in the command line.</p>
	 * <p>If necessary, it creates embedded files and deletes them after the command execution.</p>
	 * </p>
	 *
	 * @param commandLine The command Line. (mandatory)
	 * @param telemetryManager The engine configuration and host properties. (mandatory)
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
	 * @throws ControlledSshException
	 */
	public static OsCommandResult runOsCommand(
			@NonNull final String commandLine,
			@NonNull final TelemetryManager telemetryManager,
			final Map<Integer, EmbeddedFile> embeddedFiles,
			final Long commandTimeout,
			final boolean isExecuteLocally,
			final boolean isLocalhost)
			throws IOException,
			MatsyaException,
			InterruptedException,
			TimeoutException,
			NoCredentialProvidedException, ControlledSshException {

		final IConfiguration configuration;
		if(!isLocalhost && telemetryManager.getHostConfiguration().getHostType() == DeviceKind.WINDOWS) {
			configuration = telemetryManager.getWinConfiguration();
		} else {
			configuration = telemetryManager.getHostConfiguration().getConfigurations().get(SshConfiguration.class);
		}

		final Optional<String> maybeUsername = getUsername(configuration);

		// If remote command and no username
		if ((maybeUsername.isEmpty() || maybeUsername.get().trim().isEmpty()) &&
				!isExecuteLocally && !isLocalhost) {
			throw new NoCredentialProvidedException();
		}

		final Optional<char[]> maybePassword = getPassword(configuration);

		final String hostname = telemetryManager.getHostConfiguration().getHostname();

		final OsCommandConfiguration osCommandConfiguration =
				(OsCommandConfiguration) telemetryManager.getHostConfiguration().getConfigurations().get(OsCommandConfiguration.class);

		final Map<String, File> embeddedTempFiles = createOsCommandEmbeddedFiles(
				commandLine,
				embeddedFiles,
				osCommandConfiguration,
				TEMP_FILE_CREATOR
		);

		final String updatedUserCommand = maybeUsername
				.map(username -> commandLine.replaceAll(
						toCaseInsensitiveRegex(USERNAME_MACRO), username))
				.orElse(commandLine);

		final String updatedHostnameCommand = updatedUserCommand.replaceAll(
				toCaseInsensitiveRegex(HOSTNAME_MACRO), hostname);

		final String updatedSudoCommand = replaceSudo(updatedHostnameCommand, osCommandConfiguration);

		final String updatedEmbeddedFilesCommand = embeddedTempFiles.entrySet().stream()
				.reduce(
						updatedSudoCommand,
						(s, entry) -> s.replaceAll(
								toCaseInsensitiveRegex(entry.getKey()),
								Matcher.quoteReplacement(entry.getValue().getAbsolutePath())),
						(s1, s2) -> null);

		final String command = maybePassword
				.map(password -> updatedEmbeddedFilesCommand.replaceAll(
						toCaseInsensitiveRegex(PASSWORD_MACRO), String.valueOf(password)))
				.orElse(updatedEmbeddedFilesCommand);

		final String noPasswordCommand = maybePassword
				.map(password -> updatedEmbeddedFilesCommand.replaceAll(
						toCaseInsensitiveRegex(PASSWORD_MACRO), "********"))
				.orElse(updatedEmbeddedFilesCommand);

		try {
			final long timeout = getTimeout(
					commandTimeout,
					osCommandConfiguration,
					configuration,
					telemetryManager.getHostConfiguration().getStrategyTimeout());

			final String commandResult;

			// Case local execution or command intended for a remote host but executed locally
			if (isLocalhost || isExecuteLocally) {
				final String localCommandResult = runLocalCommand(
						command,
						timeout,
						noPasswordCommand);
				commandResult = localCommandResult != null ?
						localCommandResult :
						EMPTY;

				// Case Windows Remote
			} else if (DeviceKind.WINDOWS.equals(telemetryManager.getHostConfiguration().getHostType())) {
				commandResult = MatsyaClientsExecutor.executeWinRemoteCommand(
					command,
					configuration,
					hostname,
					embeddedTempFiles.values().stream().map(File::getAbsolutePath).toList()
				);

				// Case others (Linux) Remote
			} else {
				commandResult = runSshCommand(
					command,
					hostname,
					(SshConfiguration) configuration,
					timeout,
					new ArrayList<>(embeddedTempFiles.values()),
					noPasswordCommand
				);
			}

			return new OsCommandResult(commandResult, noPasswordCommand);
		} finally {
			//noinspection ResultOfMethodCallIgnored
			embeddedTempFiles.values().forEach(File::delete);
		}
	}

	/**
	 * This class is used to manage exceptions that can be thrown by the functional interface
	 * implementations used to create embedded temporary files.
	 */
	static class TempFileCreationException extends RuntimeException {
		private static final long serialVersionUID = 1L;

		public TempFileCreationException(final IOException cause) {
			super(cause);
		}
	}

}
