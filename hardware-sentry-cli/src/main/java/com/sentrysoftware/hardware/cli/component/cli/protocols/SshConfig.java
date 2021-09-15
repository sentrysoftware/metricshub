package com.sentrysoftware.hardware.cli.component.cli.protocols;

import java.io.File;
import java.util.Set;

import picocli.CommandLine.Option;

import lombok.Data;

@Data
public class SshConfig {
	@Option(
			names = "--ssh-username",
			description = "Username")
	private String username;

	@Option(
			names = "--ssh-password",
			description = "Password",
			interactive = true,
			arity = "0..1")
	private char[] password;

	@Option(
			names = "--ssh-privatekey",
			description = "SSH PrivateKey")
	private File privateKey;

	@Option(
			names = "--ssh-timeout",
			description = "SSH Timeout (default: ${DEFAULT-VALUE} s)",
			defaultValue ="120")
	private long timeout;

	@Option(
			names = "--ssh-usesudo",
			description = "Use SUDO")
	private boolean useSudo;

	@Option(
			names = "--ssh-usesudo-commands",
			description = "Use SUDO commands",
			split = ",")
	private Set<String> useSudoCommands;

	@Option(
			names = "--ssh-sudo-command",
			description = "SUDO command (default: ${DEFAULT-VALUE})",
			defaultValue ="sudo")
	private String sudoCommand;
}
