package org.sentrysoftware.metricshub.engine.configuration;

import java.io.File;
import java.util.Set;
import lombok.Builder;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;

/**
 * Configuration class for SSH connections. Extends {@link OsCommandConfiguration}.
 */

/**
 * The SshConfiguration class represents the configuration for SSH (Secure Shell) connections
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
}
