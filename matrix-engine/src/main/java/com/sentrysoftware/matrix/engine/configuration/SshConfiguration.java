package com.sentrysoftware.matrix.engine.configuration;

import lombok.Builder;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;

import java.io.File;
import java.util.Set;

@Data
@EqualsAndHashCode(callSuper = true)
@NoArgsConstructor
public class SshConfiguration extends OsCommandConfiguration {

	private String username;
	private char[] password;
	private File privateKey;

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
