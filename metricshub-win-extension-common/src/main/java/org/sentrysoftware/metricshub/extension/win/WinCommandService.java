package org.sentrysoftware.metricshub.extension.win;

/*-
 * ╱╲╱╲╱╲╱╲╱╲╱╲╱╲╱╲╱╲╱╲╱╲╱╲╱╲╱╲╱╲╱╲╱╲╱╲╱╲╱╲
 * MetricsHub Win Extension Common
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

import static org.sentrysoftware.metricshub.engine.common.helpers.MetricsHubConstants.HOSTNAME_MACRO;
import static org.sentrysoftware.metricshub.engine.common.helpers.MetricsHubConstants.PASSWORD_MACRO;
import static org.sentrysoftware.metricshub.engine.common.helpers.MetricsHubConstants.USERNAME_MACRO;

import java.io.File;
import java.io.IOException;
import java.util.Map;
import java.util.Optional;
import java.util.regex.Matcher;
import java.util.stream.Collectors;
import lombok.NonNull;
import lombok.RequiredArgsConstructor;
import org.sentrysoftware.metricshub.engine.common.exception.ClientException;
import org.sentrysoftware.metricshub.engine.common.exception.NoCredentialProvidedException;
import org.sentrysoftware.metricshub.engine.strategy.utils.EmbeddedFileHelper;
import org.sentrysoftware.metricshub.engine.strategy.utils.OsCommandHelper;
import org.sentrysoftware.metricshub.engine.strategy.utils.OsCommandResult;

@RequiredArgsConstructor
public class WinCommandService {

	@NonNull
	private IWinRequestExecutor winRequestExecutor;

	/**
	 * Get the username from the IWinConfiguration.
	 *
	 * @param configuration IWinConfiguration instance.
	 * @return An optional with the username if found. An empty optional otherwise.
	 */
	public static Optional<String> getUsername(final IWinConfiguration configuration) {
		if (configuration == null) {
			return Optional.empty();
		}
		return Optional.ofNullable(configuration.getUsername());
	}

	/**
	 * Get the password from the IWinConfiguration.
	 *
	 * @param configuration IWinConfiguration instance.
	 * @return An optional with the password if found. An empty optional otherwise.
	 */
	public static Optional<char[]> getPassword(final IWinConfiguration configuration) {
		if (configuration == null) {
			return Optional.empty();
		}
		return Optional.ofNullable(configuration.getPassword());
	}

	/**
	 * Run the OS Command on Remote windows (use WMI/WinRm command)
	 * <p>It replaces Host name, User name, Password, Sudo, Embedded files macros in the command line.</p>
	 * <p>If necessary, it creates embedded files and deletes them after the command execution.</p>
	 *
	 * @param commandLine      The command Line. (mandatory)
	 * @param hostname         The hostname of the remote device where the WMI or WinRm service is running. (mandatory)
	 * @param commandTimeout   The CommandLine criterion/source parameter for the timeout.
	 *
	 * @return The command execution return and the command with password masked (if present).
	 * @throws IOException                   When an I/O error occurred on local command execution or embedded file creation.
	 * @throws ClientException               When an error occurred on remote execution.
	 * @throws NoCredentialProvidedException When there's no user provided for a remote command.
	 */
	public OsCommandResult runOsCommand(
		@NonNull final String commandLine,
		@NonNull final String hostname,
		final IWinConfiguration configuration
	) throws IOException, ClientException, NoCredentialProvidedException {
		final Optional<String> maybeUsername = getUsername(configuration);

		// Remote command and no username
		if ((maybeUsername.isEmpty() || maybeUsername.get().isBlank())) {
			throw new NoCredentialProvidedException();
		}

		final Optional<char[]> maybePassword = getPassword(configuration);

		final Map<String, File> embeddedTempFiles = OsCommandHelper.createOsCommandEmbeddedFiles(
			commandLine,
			null,
			EmbeddedFileHelper.findEmbeddedFiles(commandLine),
			OsCommandHelper.TEMP_FILE_CREATOR
		);

		final String updatedUserCommand = maybeUsername
			.map(username -> commandLine.replaceAll(OsCommandHelper.toCaseInsensitiveRegex(USERNAME_MACRO), username))
			.orElse(commandLine);

		final String updatedHostnameCommand = updatedUserCommand.replaceAll(
			OsCommandHelper.toCaseInsensitiveRegex(HOSTNAME_MACRO),
			hostname
		);

		final String updatedSudoCommand = OsCommandHelper.replaceSudo(updatedHostnameCommand, null);

		final String updatedEmbeddedFilesCommand = embeddedTempFiles
			.entrySet()
			.stream()
			.reduce(
				updatedSudoCommand,
				(s, entry) ->
					s.replaceAll(
						OsCommandHelper.toCaseInsensitiveRegex(entry.getKey()),
						Matcher.quoteReplacement(entry.getValue().getAbsolutePath())
					),
				(s1, s2) -> null
			);

		final String command = maybePassword
			.map(password ->
				updatedEmbeddedFilesCommand.replaceAll(
					OsCommandHelper.toCaseInsensitiveRegex(PASSWORD_MACRO),
					String.valueOf(password)
				)
			)
			.orElse(updatedEmbeddedFilesCommand);

		final String noPasswordCommand = maybePassword
			.map(password ->
				updatedEmbeddedFilesCommand.replaceAll(OsCommandHelper.toCaseInsensitiveRegex(PASSWORD_MACRO), "********")
			)
			.orElse(updatedEmbeddedFilesCommand);

		try {
			final String commandResult = winRequestExecutor.executeWinRemoteCommand(
				hostname,
				configuration,
				command,
				embeddedTempFiles.values().stream().map(File::getAbsolutePath).collect(Collectors.toList()) // NOSONAR
			);

			return new OsCommandResult(commandResult, noPasswordCommand);
		} finally {
			//noinspection ResultOfMethodCallIgnored
			embeddedTempFiles.values().forEach(File::delete);
		}
	}
}
