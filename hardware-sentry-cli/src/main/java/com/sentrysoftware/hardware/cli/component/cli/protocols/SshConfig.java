package com.sentrysoftware.hardware.cli.component.cli.protocols;

import java.io.File;
import java.util.Set;

import com.sentrysoftware.matrix.engine.protocol.IProtocolConfiguration;
import com.sentrysoftware.matrix.engine.protocol.SSHProtocol;

import picocli.CommandLine.Option;
import picocli.CommandLine.Spec;
import picocli.CommandLine.Model.CommandSpec;
import lombok.Data;

@Data
public class SshConfig implements IProtocolConfig {

	@Spec
	CommandSpec spec;

	@Option(
			names = "--ssh",
			order = 1,
			description = "Enables SSH"
	)
	private boolean useSsh;

	@Option(
			names = "--ssh-username",
			description = "Username for SSH authentication"
	)
	private String username;

	@Option(
			names = "--ssh-password",
			description = "Password or SSH authentication",
			interactive = true,
			arity = "0..1"
	)
	private char[] password;

	@Option(
			names = "--ssh-privatekey",
			description = "Path to the private key file for SSH authentication"
	)
	private File privateKey;

	@Option(
			names = "--ssh-timeout",
			description = "Timeout in seconds for HTTP operations (default: ${DEFAULT-VALUE} s)",
			defaultValue ="120"
	)
	private long timeout;

	@Option(
			names = "--ssh-usesudo-commands",
			description = "List of commands for which @|italic sudo|@ is required",
			split = ","
	)
	private Set<String> useSudoCommands;

	@Option(
			names = "--ssh-sudo-command",
			description = "@|italic sudo|@ command (default: ${DEFAULT-VALUE})",
			defaultValue ="sudo"
	)
	private String sudoCommand;

	@Override
	public IProtocolConfiguration toProtocol(String defaultUsername, char[] defaultPassword) {
		return SSHProtocol
				.builder()
				.username(username == null ? defaultUsername : username)
				.password(username == null ? defaultPassword : password)
				.privateKey(privateKey)
				.useSudoCommands(useSudoCommands)
				.sudoCommand(sudoCommand)
				.timeout(timeout)
				.build();
	}
}
