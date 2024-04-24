package org.sentrysoftware.metricshub.extension.oscommand;

/*-
 * ╱╲╱╲╱╲╱╲╱╲╱╲╱╲╱╲╱╲╱╲╱╲╱╲╱╲╱╲╱╲╱╲╱╲╱╲╱╲╱╲
 * MetricsHub OsCommand Extension
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

import static org.springframework.util.Assert.isTrue;

import io.opentelemetry.instrumentation.annotations.SpanAttribute;
import io.opentelemetry.instrumentation.annotations.WithSpan;
import java.io.File;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.util.List;
import lombok.NonNull;
import lombok.extern.slf4j.Slf4j;
import org.sentrysoftware.metricshub.engine.common.exception.ClientException;
import org.sentrysoftware.metricshub.engine.common.helpers.LoggingHelper;
import org.sentrysoftware.metricshub.engine.strategy.utils.OsCommandHelper;
import org.sentrysoftware.ssh.SshClient;

/**
 * Provides functionality to execute remote SSH commands, manage SSH authentication, and transfer files to remote hosts.
 */
@Slf4j
public class OsCommandRequestExecutor {

	private static final String SSH_FILE_MODE = "0700";
	private static final String SSH_REMOTE_DIRECTORY = "/var/tmp/";

	/**
	 * Use ssh-client in order to run ssh command.
	 *
	 * @param hostname         The hostname or IP address to connect to.
	 * @param username         The SSH username.
	 * @param password         The SSH password as a character array.
	 * @param keyFilePath      The path to the SSH key file.
	 * @param command          The SSH command to execute.
	 * @param timeout          The timeout for the command execution in seconds.
	 * @param localFiles       List of local files to be transferred to the remote host.
	 * @param noPasswordCommand The command to execute without password.
	 * @return The result of the SSH command.
	 * @throws ClientException If an error occurs during the SSH command execution.
	 */
	@WithSpan("SSH")
	public static String runRemoteSshCommand(
		@NonNull @SpanAttribute("host.hostname") final String hostname,
		@NonNull @SpanAttribute("ssh.username") final String username,
		final char[] password,
		@SpanAttribute("ssh.key_file_path") final File keyFilePath,
		final String command,
		@SpanAttribute("ssh.timeout") final long timeout,
		@SpanAttribute("ssh.local_files") final List<File> localFiles,
		@SpanAttribute("ssh.command") final String noPasswordCommand
	) throws ClientException {
		LoggingHelper.trace(() ->
			log.trace(
				"Executing Remote SSH command:\n- hostname: {}\n- username: {}\n- key-file-path: {}\n" + // NOSONAR
				"- command: {}\n- timeout: {} s\n- local-files: {}\n",
				hostname,
				username,
				keyFilePath,
				command,
				timeout,
				localFiles
			)
		);

		isTrue(command != null && !command.trim().isEmpty(), "Command cannot be null nor empty.");
		isTrue(timeout > 0, "Timeout cannot be negative nor zero.");
		final long timeoutInMilliseconds = timeout * 1000;

		final String updatedCommand = updateCommandWithLocalList(command, localFiles);

		final String noPasswordUpdatedCommand = noPasswordCommand == null
			? updatedCommand
			: updateCommandWithLocalList(noPasswordCommand, localFiles);

		// We have a command: execute it
		try (SshClient sshClient = createSshClientInstance(hostname)) {
			sshClient.connect((int) timeoutInMilliseconds);

			if (password == null) {
				log.warn("Hostname {} - Password could not be read. Using an empty password instead.", hostname);
			}

			authenticateSsh(sshClient, hostname, username, password, keyFilePath);

			if (localFiles != null && !localFiles.isEmpty()) {
				// copy all local files using SCP
				for (final File file : localFiles) {
					sshClient.scp(file.getAbsolutePath(), file.getName(), SSH_REMOTE_DIRECTORY, SSH_FILE_MODE);
				}
			}

			final long startTime = System.currentTimeMillis();

			final SshClient.CommandResult commandResult = sshClient.executeCommand(
				updatedCommand,
				(int) timeoutInMilliseconds
			);

			final long responseTime = System.currentTimeMillis() - startTime;

			if (!commandResult.success) {
				final String message = String.format(
					"Hostname %s - Command \"%s\" failed with result %s.",
					hostname,
					noPasswordUpdatedCommand,
					commandResult.result
				);
				log.error(message);
				throw new ClientException(message);
			}

			String result = commandResult.result;

			LoggingHelper.trace(() ->
				log.trace(
					"Executed Remote SSH command:\n- Hostname: {}\n- Username: {}\n- Key-file-path: {}\n" + // NOSONAR
					"- Command: {}\n- Timeout: {} s\n- Local-files: {}\n- Result:\n{}\n- response-time: {}\n",
					hostname,
					username,
					keyFilePath,
					command,
					timeout,
					localFiles,
					result,
					responseTime
				)
			);
			return result;
		} catch (final ClientException e) {
			throw e;
		} catch (final Exception e) {
			final String message = String.format(
				"Failed to run SSH command \"%s\" as %s on %s.",
				noPasswordUpdatedCommand,
				username,
				hostname
			);
			log.error("Hostname {} - {}. Exception : {}.", hostname, message, e.getMessage());
			throw new ClientException(message, (Exception) e.getCause());
		}
	}

	/**
	 * Authenticate SSH with:
	 * <ul>
	 * 	<li>username, privateKey and password first</li>
	 * 	<li>username and password</li>
	 * 	<li>username only</li>
	 * </ul>
	 *
	 * @param sshClient  The SSH client
	 * @param hostname   The hostname
	 * @param username   The username
	 * @param password   The password
	 * @param privateKey The private key file
	 * @throws ClientException If an error occurred.
	 */
	static void authenticateSsh(
		final SshClient sshClient,
		final String hostname,
		final String username,
		final char[] password,
		final File privateKey
	) throws ClientException {
		final boolean authenticated;
		try {
			if (privateKey != null) {
				authenticated = sshClient.authenticate(username, privateKey, password);
			} else if (password != null && password.length > 0) {
				authenticated = sshClient.authenticate(username, password);
			} else {
				authenticated = sshClient.authenticate(username);
			}
		} catch (final Exception e) {
			final String message = String.format(
				"Hostname %s - Authentication as %s has failed with %s.",
				hostname,
				username,
				privateKey != null ? privateKey.getAbsolutePath() : null
			);
			log.error("Hostname {} - {}. Exception : {}.", hostname, message, e.getMessage());
			throw new ClientException(message, e);
		}

		if (!authenticated) {
			final String message = String.format(
				"Hostname %s - Authentication as %s has failed with %s.",
				hostname,
				username,
				privateKey != null ? privateKey.getAbsolutePath() : null
			);
			log.error(message);
			throw new ClientException(message);
		}
	}

	/**
	 * Replace in the SSH command all the local files path with their remote path.
	 *
	 * @param command    The SSH command.
	 * @param localFiles The local files list.
	 * @return The updated command.
	 */
	static String updateCommandWithLocalList(final String command, final List<File> localFiles) {
		return localFiles == null || localFiles.isEmpty()
			? command
			: localFiles
				.stream()
				.reduce(
					command,
					(s, file) ->
						command.replaceAll(
							OsCommandHelper.toCaseInsensitiveRegex(file.getAbsolutePath()),
							SSH_REMOTE_DIRECTORY + file.getName()
						),
					(s1, s2) -> null
				);
	}

	/**
	 * Creates a new instance of the {@link SshClient}.
	 *
	 * @param hostname The hostname.
	 * @return A {@link SshClient} instance.
	 */
	public static SshClient createSshClientInstance(final String hostname) {
		return new SshClient(hostname, StandardCharsets.UTF_8);
	}

	/**
	 * Connect to the SSH terminal. For that:
	 * <ul>
	 * 	<li>Create an SSH Client instance.</li>
	 * 	<li>Connect to SSH.</li>
	 * 	<li>Open a SSH session.</li>
	 * 	<li>Open a terminal.</li>
	 * </ul>
	 *
	 * @param hostname   The hostname (mandatory)
	 * @param username   The username (mandatory)
	 * @param password   The password
	 * @param privateKey The private key file
	 * @param timeout    The timeout (>0) in seconds
	 * @return The SSH client
	 * @throws ClientException If a Client error occurred.
	 */
	public static SshClient connectSshClientTerminal(
		@NonNull final String hostname,
		@NonNull final String username,
		final char[] password,
		final File privateKey,
		final int timeout
	) throws ClientException {
		isTrue(timeout > 0, "timeout must be > 0");

		final SshClient sshClient = createSshClientInstance(hostname);

		try {
			sshClient.connect(timeout * 1000);

			authenticateSsh(sshClient, hostname, username, password, privateKey);

			sshClient.openSession();

			sshClient.openTerminal();

			return sshClient;
		} catch (final IOException e) {
			sshClient.close();
			throw new ClientException(e);
		}
	}
}
