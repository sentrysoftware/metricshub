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

import static org.sentrysoftware.metricshub.engine.common.helpers.MacroType.AUTHENTICATIONTOKEN;
import static org.sentrysoftware.metricshub.engine.common.helpers.MacroType.HOSTNAME;
import static org.sentrysoftware.metricshub.engine.common.helpers.MacroType.PASSWORD;
import static org.sentrysoftware.metricshub.engine.common.helpers.MacroType.USERNAME;
import static org.sentrysoftware.metricshub.engine.common.helpers.StringHelper.protectCaseInsensitiveRegex;

import java.io.File;
import java.io.IOException;
import java.util.HashMap;
import java.util.Map;
import java.util.Optional;
import java.util.regex.Matcher;
import java.util.stream.Collectors;
import lombok.NonNull;
import lombok.RequiredArgsConstructor;
import org.sentrysoftware.metricshub.engine.common.exception.ClientException;
import org.sentrysoftware.metricshub.engine.common.exception.NoCredentialProvidedException;
import org.sentrysoftware.metricshub.engine.common.helpers.MacrosUpdater;
import org.sentrysoftware.metricshub.engine.connector.model.common.EmbeddedFile;
import org.sentrysoftware.metricshub.engine.strategy.utils.EmbeddedFileHelper;
import org.sentrysoftware.metricshub.engine.strategy.utils.OsCommandHelper;
import org.sentrysoftware.metricshub.engine.strategy.utils.OsCommandResult;

/**
 * Provides a service for executing Windows commands through Windows Management Instrumentation (WMI).
 */
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
	public Optional<String> getUsername(final IWinConfiguration configuration) {
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
	public Optional<char[]> getPassword(final IWinConfiguration configuration) {
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
	 * @param commandLine            The command Line. (mandatory)
	 * @param hostname               The hostname of the remote device where the WMI or WinRm service is running. (mandatory)
	 * @param configuration          Windows Protocol configuration (credentials, timeout). E.g. WMI or WinRm.
	 * @param connectorEmbeddedFiles All the embedded files defined by the connector object. (mandatory)
	 *
	 * @return The command execution return and the command with password masked (if present).
	 * @throws IOException                   When an I/O error occurred on local command execution or embedded file creation.
	 * @throws ClientException               When an error occurred on remote execution.
	 * @throws NoCredentialProvidedException When there's no user provided for a remote command.
	 */
	public OsCommandResult runOsCommand(
		@NonNull final String commandLine,
		@NonNull final String hostname,
		final IWinConfiguration configuration,
		@NonNull Map<Integer, EmbeddedFile> connectorEmbeddedFiles
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
			EmbeddedFileHelper.findEmbeddedFiles(commandLine, connectorEmbeddedFiles),
			OsCommandHelper.TEMP_FILE_CREATOR
		);

		// Create the macrosToUpdate map and configure it to update only the username macro
		final Map<String, Boolean> macrosToUpdate = new HashMap<>(
			Map.of(USERNAME.name(), true, PASSWORD.name(), false, HOSTNAME.name(), false, AUTHENTICATIONTOKEN.name(), false)
		);

		final String updatedUserCommand = maybeUsername
			.map(username -> MacrosUpdater.update(commandLine, username, null, null, hostname, false, macrosToUpdate))
			.orElse(commandLine);

		// Modify macrosToUpdate map to update only the host name macro
		macrosToUpdate.put(USERNAME.name(), false);
		macrosToUpdate.put(HOSTNAME.name(), true);

		final String updatedHostnameCommand = MacrosUpdater.update(
			updatedUserCommand,
			null,
			null,
			null,
			hostname,
			false,
			macrosToUpdate
		);

		final String updatedSudoCommand = OsCommandHelper.replaceSudo(updatedHostnameCommand, null);

		final String updatedEmbeddedFilesCommand = embeddedTempFiles
			.entrySet()
			.stream()
			.reduce(
				updatedSudoCommand,
				(s, entry) ->
					s.replaceAll(
						protectCaseInsensitiveRegex(entry.getKey()),
						Matcher.quoteReplacement(entry.getValue().getAbsolutePath())
					),
				(s1, s2) -> null
			);

		// Modify macrosToUpdate map to update only the password macro
		macrosToUpdate.put(PASSWORD.name(), true);
		macrosToUpdate.put(HOSTNAME.name(), false);

		final String command = maybePassword
			.map(password ->
				MacrosUpdater.update(updatedEmbeddedFilesCommand, null, password, null, hostname, false, macrosToUpdate)
			)
			.orElse(updatedEmbeddedFilesCommand);

		final String noPasswordCommand = maybePassword
			.map(password ->
				MacrosUpdater.update(updatedEmbeddedFilesCommand, null, password, null, hostname, true, macrosToUpdate)
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
