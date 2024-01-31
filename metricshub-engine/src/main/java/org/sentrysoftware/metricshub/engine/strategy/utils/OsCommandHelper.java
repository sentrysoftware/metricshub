package org.sentrysoftware.metricshub.engine.strategy.utils;

/*-
 * ╱╲╱╲╱╲╱╲╱╲╱╲╱╲╱╲╱╲╱╲╱╲╱╲╱╲╱╲╱╲╱╲╱╲╱╲╱╲╱╲
 * MetricsHub Engine
 * ჻჻჻჻჻჻
 * Copyright 2023 - 2024 Sentry Software
 * ჻჻჻჻჻჻
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Affero General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU Affero General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 * ╲╱╲╱╲╱╲╱╲╱╲╱╲╱╲╱╲╱╲╱╲╱╲╱╲╱╲╱╲╱╲╱╲╱╲╱╲╱╲╱
 */

import static org.sentrysoftware.metricshub.engine.common.helpers.MetricsHubConstants.DEFAULT_LOCK_TIMEOUT;
import static org.sentrysoftware.metricshub.engine.common.helpers.MetricsHubConstants.EMPTY;
import static org.sentrysoftware.metricshub.engine.common.helpers.MetricsHubConstants.FILE_PATTERN;
import static org.sentrysoftware.metricshub.engine.common.helpers.MetricsHubConstants.HOSTNAME_MACRO;
import static org.sentrysoftware.metricshub.engine.common.helpers.MetricsHubConstants.NEW_LINE;
import static org.sentrysoftware.metricshub.engine.common.helpers.MetricsHubConstants.PASSWORD_MACRO;
import static org.sentrysoftware.metricshub.engine.common.helpers.MetricsHubConstants.USERNAME_MACRO;
import static org.springframework.util.Assert.isTrue;
import static org.springframework.util.Assert.state;

import io.opentelemetry.instrumentation.annotations.SpanAttribute;
import io.opentelemetry.instrumentation.annotations.WithSpan;
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
import java.util.stream.Collectors;
import lombok.AccessLevel;
import lombok.NoArgsConstructor;
import lombok.NonNull;
import org.sentrysoftware.metricshub.engine.client.ClientsExecutor;
import org.sentrysoftware.metricshub.engine.common.exception.ClientException;
import org.sentrysoftware.metricshub.engine.common.exception.ClientRuntimeException;
import org.sentrysoftware.metricshub.engine.common.exception.ControlledSshException;
import org.sentrysoftware.metricshub.engine.common.exception.NoCredentialProvidedException;
import org.sentrysoftware.metricshub.engine.common.helpers.LocalOsHandler;
import org.sentrysoftware.metricshub.engine.configuration.IConfiguration;
import org.sentrysoftware.metricshub.engine.configuration.IWinConfiguration;
import org.sentrysoftware.metricshub.engine.configuration.OsCommandConfiguration;
import org.sentrysoftware.metricshub.engine.configuration.SshConfiguration;
import org.sentrysoftware.metricshub.engine.connector.model.common.DeviceKind;
import org.sentrysoftware.metricshub.engine.connector.model.common.EmbeddedFile;
import org.sentrysoftware.metricshub.engine.telemetry.SshSemaphoreFactory;
import org.sentrysoftware.metricshub.engine.telemetry.TelemetryManager;

/**
 * Utility class for handling OS commands, including local and remote execution.
 */
@NoArgsConstructor(access = AccessLevel.PRIVATE)
public class OsCommandHelper {

	private static final String NEGATIVE_TIMEOUT = "timeout mustn't be negative nor zero.";
	private static final Pattern SUDO_COMMAND_PATTERN = Pattern.compile(
		"%\\{SUDO:([^\\}]*)\\}",
		Pattern.CASE_INSENSITIVE
	);
	static final Function<String, File> TEMP_FILE_CREATOR = OsCommandHelper::createEmbeddedTempFile;

	/**
	 * Create the temporary embedded files in the given command line.
	 *
	 * @param commandLine              The command line to process.
	 * @param osCommandConfiguration   The OS Command Configuration.
	 * @param commandLineEmbeddedFiles A map of embedded files referenced in the command line.
	 * @param tempFileCreator          The function that creates a temporary file.
	 * @return A map with EmbeddedFile tags as keys and corresponding temporary File objects.
	 * @throws IOException If an error occurs during temp file creation.
	 */
	public static Map<String, File> createOsCommandEmbeddedFiles(
		@NonNull final String commandLine,
		final OsCommandConfiguration osCommandConfiguration,
		@NonNull final Map<String, EmbeddedFile> commandLineEmbeddedFiles,
		final Function<String, File> tempFileCreator
	) throws IOException {
		final Map<String, File> embeddedTempFiles = new HashMap<>();
		try {
			final Matcher matcher = FILE_PATTERN.matcher(commandLine);
			while (matcher.find()) {
				// ${file::file-absolute-path} fileName is file-absolute-path // NOSONAR on comment
				final String fileName = matcher.group(1);

				// Example: ${file::file-absolute-path} // NOSONAR on comment
				final String fileNameRef = matcher.group();

				embeddedTempFiles.computeIfAbsent(
					fileNameRef,
					k -> {
						// The embedded file is available in the connector
						final EmbeddedFile embeddedFile = commandLineEmbeddedFiles.get(fileNameRef);

						// This means there is a design problem or the HDF developer indicated a wrong embedded file
						state(embeddedFile != null, () -> "Cannot get the EmbeddedFile from the Connector. File name: " + fileName);
						final String content = embeddedFile.getContent();

						// This means there is a design problem, the content can never be null
						state(content != null, () -> "EmbeddedFile content is null. File name: " + fileName);

						try {
							return createTempFileWithEmbeddedFileContent(embeddedFile, osCommandConfiguration, tempFileCreator);
						} catch (final IOException e) {
							throw new TempFileCreationException(e);
						}
					}
				);
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
	 * @param embeddedFile           {@link EmbeddedFile} instance used to write the file content (mandatory)
	 * @param osCommandConfiguration The OS Command Configuration.
	 * @param tempFileCreator        The function that creates a temporary file.
	 * @return The File.
	 * @throws IOException
	 */
	static File createTempFileWithEmbeddedFileContent(
		final EmbeddedFile embeddedFile,
		final OsCommandConfiguration osCommandConfiguration,
		Function<String, File> tempFileCreator
	) throws IOException {
		final String extension = embeddedFile.getType() != null ? "." + embeddedFile.getType() : EMPTY;

		final File tempFile = tempFileCreator.apply(extension);

		try (
			BufferedWriter bufferedWriter = Files.newBufferedWriter(
				Paths.get(tempFile.getAbsolutePath()),
				StandardCharsets.UTF_8
			)
		) {
			bufferedWriter.write(replaceSudo(embeddedFile.getContent(), osCommandConfiguration));
		}
		return tempFile;
	}

	/**
	 * Create a temporary file with the given extension.<br>
	 * The temporary file name is prefixed with "SEN_Embedded_"
	 *
	 * @param extension File's name suffix (e.g. .bat)
	 * @return {@link File} instance
	 */
	static File createEmbeddedTempFile(final String extension) {
		try {
			return File.createTempFile("SEN_Embedded_", extension);
		} catch (IOException e) {
			throw new TempFileCreationException(e);
		}
	}

	/**
	 * Replace the %{SUDO:xxx}% tag in the given text with the sudo command.
	 * The replacement is based on the configuration in the provided OsCommandConfiguration.
	 * If the useSudo configuration is enabled and the sudo command is associated with the specified file,
	 * it replaces the tag with the sudo command; otherwise, it replaces it with an empty string.
	 *
	 * @param text                    The text containing %{SUDO:xxx}% tags to be replaced.
	 * @param osCommandConfiguration The configuration for OS commands.
	 * @return The text with %{SUDO:xxx}% tags replaced with the sudo command or empty string.
	 */
	static String replaceSudo(final String text, final OsCommandConfiguration osCommandConfiguration) {
		if (text == null || text.isBlank()) {
			return text;
		}

		final Optional<String> maybeSudoFile = getFileNameFromSudoCommand(text);

		final String sudoReplace = maybeSudoFile.isPresent() &&
			osCommandConfiguration != null &&
			osCommandConfiguration.isUseSudo() &&
			osCommandConfiguration.getUseSudoCommands().contains(maybeSudoFile.get())
			? osCommandConfiguration.getSudoCommand()
			: EMPTY;

		return maybeSudoFile
			.map(fileName -> text.replaceAll(toCaseInsensitiveRegex(String.format("%%{SUDO:%s}", fileName)), sudoReplace))
			.orElse(text);
	}

	/**
	 * Run the given command on the localhost machine.
	 *
	 * @param command           The command to be executed.
	 * @param timeout           The timeout for the command execution in seconds.
	 * @param noPasswordCommand The command with the password masked (if present).
	 * @return The result of the local command execution.
	 * @throws InterruptedException When the thread is interrupted during execution.
	 * @throws IOException          When an I/O error occurs during command execution.
	 * @throws TimeoutException     When the command execution times out.
	 */
	@WithSpan("OS Command")
	public static String runLocalCommand(
		@NonNull final String command,
		@SpanAttribute("OSCommand.timeout") final long timeout,
		@SpanAttribute("OSCommand.command") final String noPasswordCommand
	) throws InterruptedException, IOException, TimeoutException {
		isTrue(timeout > 0, NEGATIVE_TIMEOUT);

		final String cmd = LocalOsHandler.isWindows() ? "CMD.EXE /C " + command : command;

		final Process process = Runtime.getRuntime().exec(cmd);
		if (process == null) {
			throw new IllegalStateException("Local command Process is null.");
		}

		final ExecutorService executor = Executors.newSingleThreadExecutor();

		final Future<String> future = executor.submit(() -> {
			try (
				InputStreamReader inputStreamReader = new InputStreamReader(process.getInputStream());
				BufferedReader bufferedReader = new BufferedReader(inputStreamReader)
			) {
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
		} catch (final TimeoutException exception) {
			future.cancel(true);

			throw new TimeoutException(
				String.format(
					"Command \"%s\" execution has timed out after %d s",
					noPasswordCommand != null ? noPasswordCommand : command,
					timeout
				)
			);
		} catch (final ExecutionException exception) {
			if (exception.getCause() instanceof IOException ioException) {
				throw ioException;
			}
			return null;
		} finally {
			executor.shutdownNow();
		}
	}

	/**
	 * Run an SSH command, checking if it can be executed on localhost or remotely.
	 *
	 * @param command           The SSH command to be executed.
	 * @param hostname          The hostname of the remote machine.
	 * @param sshConfiguration  The SSH configuration including username, password, and private key.
	 * @param timeout           The timeout for the command execution in seconds.
	 * @param localFiles        List of local files required for the remote execution.
	 * @param noPasswordCommand The command with password masked (if present).
	 * @return The result of the SSH command execution.
	 * @throws ClientException          When an error occurs during Client execution.
	 * @throws InterruptedException    When the thread is interrupted during execution.
	 * @throws ControlledSshException   When there's an issue with controlled SSH execution.
	 */
	public static String runSshCommand(
		@NonNull final String command,
		@NonNull final String hostname,
		@NonNull final SshConfiguration sshConfiguration,
		final long timeout,
		final List<File> localFiles,
		final String noPasswordCommand
	) throws ClientException, InterruptedException, ControlledSshException {
		isTrue(timeout > 0, NEGATIVE_TIMEOUT);

		try {
			return runControlledSshCommand(
				() -> {
					try {
						return ClientsExecutor.runRemoteSshCommand(
							hostname,
							sshConfiguration.getUsername(),
							sshConfiguration.getPassword(),
							sshConfiguration.getPrivateKey(),
							command,
							timeout,
							localFiles,
							noPasswordCommand
						);
					} catch (ClientException e) {
						throw new ClientRuntimeException(e);
					}
				},
				hostname,
				DEFAULT_LOCK_TIMEOUT
			);
		} catch (final ClientRuntimeException e) {
			throw (ClientException) e.getCause();
		}
	}

	/**
	 * Run a controlled SSH command, checking if a permit can be obtained from the semaphore before running the command.
	 *
	 * @param <T>         The type of result expected from the executable.
	 * @param executable  The executable to be run.
	 * @param hostname    The hostname of the remote machine.
	 * @param timeout     The timeout for obtaining a semaphore permit in seconds.
	 * @return The result of the executable.
	 * @throws InterruptedException    When the thread is interrupted during execution.
	 * @throws ControlledSshException   When there's an issue with controlled SSH execution.
	 */
	static <T> T runControlledSshCommand(Supplier<T> executable, String hostname, int timeout)
		throws InterruptedException, ControlledSshException {
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
		final Matcher matcher = SUDO_COMMAND_PATTERN.matcher(command);
		return matcher.find() ? Optional.ofNullable(matcher.group(1)) : Optional.empty();
	}

	/**
	 * Convert a string to be searched in a case insensitive regex.
	 *
	 * @param host The string to searched. (mandatory)
	 * @return The case insensitive regex for this string.
	 */
	public static String toCaseInsensitiveRegex(final String host) {
		isTrue(host != null && !host.isEmpty(), "host cannot be null nor empty.");
		return host.isBlank() ? host : "(?i)" + Pattern.quote(host);
	}

	/**
	 * Get the timeout from:
	 * <ul>
	 * 	<li>First, the command timeout</li>
	 * 	<li>Then, the command configuration timeout</li>
	 * 	<li>Then, the protocol configuration timeout</li>
	 * 	<li>Finally, the default timeout</li>
	 * </ul>
	 *
	 * @param commandTimeout         The OS command timeout in seconds.
	 * @param osCommandConfiguration The configuration specific to OS command execution.
	 * @param configuration          The general configuration, either an instance of {@link IWinConfiguration} or {@link SshConfiguration}.
	 * @param defaultTimeout         The default timeout in seconds.
	 * @return The timeout in seconds.
	 */
	public static long getTimeout(
		final Long commandTimeout,
		final OsCommandConfiguration osCommandConfiguration,
		final IConfiguration configuration,
		final long defaultTimeout
	) {
		if (commandTimeout != null) {
			return commandTimeout.intValue();
		}

		if (osCommandConfiguration != null && osCommandConfiguration.getTimeout() != null) {
			return osCommandConfiguration.getTimeout().intValue();
		}

		if (configuration == null) {
			return defaultTimeout;
		}

		final Long timeout = configuration instanceof IWinConfiguration winConfiguration
			? winConfiguration.getTimeout()
			: ((SshConfiguration) configuration).getTimeout();

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
	 * Run the OS Command on:
	 * <ul>
	 *   <li>Local (use java Process)</li>
	 *   <li>Remote windows (use WMI/WinRm command)</li>
	 *   <li>Remote Linux (use SSH)</li>
	 * </ul>
	 * <p>It replaces Host name, User name, Password, Sudo, Embedded files macros in the command line.</p>
	 * <p>If necessary, it creates embedded files and deletes them after the command execution.</p>
	 *
	 * @param commandLine         The command Line. (mandatory)
	 * @param telemetryManager    The engine configuration and host properties. (mandatory)
	 * @param commandTimeout      The OS command parameter for the timeout.
	 * @param isExecuteLocally    The OS command parameter to indicate if the command should be executed locally.
	 * @param isLocalhost         The parameter in Host Monitoring to indicate if the command is executed locally.
	 * @return The command execution return and the command with password masked (if present).
	 * @throws IOException                   When an I/O error occurred on local command execution or embedded file creation.
	 * @throws ClientException               When an error occurred on remote execution.
	 * @throws InterruptedException          When the local command execution is interrupted.
	 * @throws TimeoutException              When the local command execution ends in timeout.
	 * @throws NoCredentialProvidedException When there's no user provided for a remote command.
	 * @throws ControlledSshException        When an error occurs during controlled SSH execution.
	 */
	public static OsCommandResult runOsCommand(
		@NonNull final String commandLine,
		@NonNull final TelemetryManager telemetryManager,
		final Long commandTimeout,
		final boolean isExecuteLocally,
		final boolean isLocalhost
	)
		throws IOException, ClientException, InterruptedException, TimeoutException, NoCredentialProvidedException, ControlledSshException {
		final IConfiguration configuration;

		if (!isLocalhost && telemetryManager.getHostConfiguration().getHostType() == DeviceKind.WINDOWS) {
			configuration = telemetryManager.getWinConfiguration();
		} else {
			configuration = telemetryManager.getHostConfiguration().getConfigurations().get(SshConfiguration.class);
		}

		final Optional<String> maybeUsername = getUsername(configuration);

		// If remote command and no username
		if ((maybeUsername.isEmpty() || maybeUsername.get().isBlank()) && !isExecuteLocally && !isLocalhost) {
			throw new NoCredentialProvidedException();
		}

		final Optional<char[]> maybePassword = getPassword(configuration);

		final String hostname = telemetryManager.getHostConfiguration().getHostname();

		final OsCommandConfiguration osCommandConfiguration = (OsCommandConfiguration) telemetryManager
			.getHostConfiguration()
			.getConfigurations()
			.get(OsCommandConfiguration.class);

		final Map<String, File> embeddedTempFiles = createOsCommandEmbeddedFiles(
			commandLine,
			osCommandConfiguration,
			EmbeddedFileHelper.findEmbeddedFiles(commandLine),
			TEMP_FILE_CREATOR
		);

		final String updatedUserCommand = maybeUsername
			.map(username -> commandLine.replaceAll(toCaseInsensitiveRegex(USERNAME_MACRO), username))
			.orElse(commandLine);

		final String updatedHostnameCommand = updatedUserCommand.replaceAll(
			toCaseInsensitiveRegex(HOSTNAME_MACRO),
			hostname
		);

		final String updatedSudoCommand = replaceSudo(updatedHostnameCommand, osCommandConfiguration);

		final String updatedEmbeddedFilesCommand = embeddedTempFiles
			.entrySet()
			.stream()
			.reduce(
				updatedSudoCommand,
				(s, entry) ->
					s.replaceAll(
						toCaseInsensitiveRegex(entry.getKey()),
						Matcher.quoteReplacement(entry.getValue().getAbsolutePath())
					),
				(s1, s2) -> null
			);

		final String command = maybePassword
			.map(password ->
				updatedEmbeddedFilesCommand.replaceAll(toCaseInsensitiveRegex(PASSWORD_MACRO), String.valueOf(password))
			)
			.orElse(updatedEmbeddedFilesCommand);

		final String noPasswordCommand = maybePassword
			.map(password -> updatedEmbeddedFilesCommand.replaceAll(toCaseInsensitiveRegex(PASSWORD_MACRO), "********"))
			.orElse(updatedEmbeddedFilesCommand);

		try {
			final long timeout = getTimeout(
				commandTimeout,
				osCommandConfiguration,
				configuration,
				telemetryManager.getHostConfiguration().getStrategyTimeout()
			);

			final String commandResult;

			// Case local execution or command intended for a remote host but executed locally
			if (isLocalhost || isExecuteLocally) {
				final String localCommandResult = runLocalCommand(command, timeout, noPasswordCommand);
				commandResult = localCommandResult != null ? localCommandResult : EMPTY;
				// Case Windows Remote
			} else if (DeviceKind.WINDOWS.equals(telemetryManager.getHostConfiguration().getHostType())) {
				commandResult =
					ClientsExecutor.executeWinRemoteCommand(
						hostname,
						configuration,
						command,
						embeddedTempFiles.values().stream().map(File::getAbsolutePath).collect(Collectors.toList()) // NOSONAR
					);
				// Case others (Linux) Remote
			} else {
				commandResult =
					runSshCommand(
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

		TempFileCreationException(final IOException cause) {
			super(cause);
		}
	}
}
