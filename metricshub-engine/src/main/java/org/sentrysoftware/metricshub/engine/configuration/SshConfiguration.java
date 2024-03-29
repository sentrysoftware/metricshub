package org.sentrysoftware.metricshub.engine.configuration;

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

import java.io.File;
import java.util.Set;
import lombok.Builder;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;
import org.sentrysoftware.metricshub.engine.common.exception.InvalidConfigurationException;

/**
 * Configuration class for SSH connections. It represents the configuration for SSH (Secure Shell) connections
 * in the MetricsHub engine.
 */
@Data
@EqualsAndHashCode(callSuper = true)
@NoArgsConstructor
public class SshConfiguration extends OsCommandConfiguration {

	private String username;
	private char[] password;
	private File privateKey;

	/**
	 * Builder for creating instances of {@link SshConfiguration}.
	 *
	 * @param useSudo           Flag indicating whether to use sudo for commands.
	 * @param useSudoCommands   Set of sudo commands to be used.
	 * @param sudoCommand       The sudo command to execute.
	 * @param timeout           The timeout for command execution in seconds.
	 * @param username          The SSH username for authentication.
	 * @param password          The SSH password for authentication.
	 * @param privateKey        The private key file for SSH key-based authentication.
	 */
	@Builder(builderMethodName = "sshConfigurationBuilder")
	public SshConfiguration(
		final boolean useSudo,
		final Set<String> useSudoCommands,
		final String sudoCommand,
		final Long timeout,
		final String username,
		final char[] password,
		final File privateKey
	) {
		super(useSudo, useSudoCommands, sudoCommand, timeout);
		this.username = username;
		this.password = password;
		this.privateKey = privateKey;
	}

	@Override
	public String toString() {
		String description = "SSH";
		if (username != null) {
			description = description + " as " + username;
		}
		return description;
	}

	@Override
	public void validateConfiguration(String resourceKey) throws InvalidConfigurationException {
		// TODO implement the validation
	}
}
