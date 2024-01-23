package org.sentrysoftware.metricshub.cli.service.protocol;

import java.io.File;
import java.util.Set;
import lombok.Data;
import org.sentrysoftware.metricshub.engine.configuration.IConfiguration;
import org.sentrysoftware.metricshub.engine.configuration.SshConfiguration;
import picocli.CommandLine.Model.CommandSpec;
import picocli.CommandLine.Option;
import picocli.CommandLine.Spec;

/**
 * This class is used by MetricsHubCliService to configure SSH protocol when using the MetricsHub CLI.
 * It create the engine's {@link SshConfiguration} object that is used to monitor a specific resource.
 */
@Data
public class SshConfigCli implements IProtocolConfigCli {

	/**
	 * Default timeout in seconds to execute an SSH operation
	 */
	public static final int DEFAULT_TIMEOUT = 30;

	@Spec
	CommandSpec spec;

	@Option(names = "--ssh", order = 1, description = "Enables SSH")
	private boolean useSsh;

	@Option(names = "--ssh-username", order = 2, paramLabel = "USER", description = "Username for SSH authentication")
	private String username;

	@Option(
		names = "--ssh-password",
		order = 3,
		paramLabel = "P4SSW0RD",
		description = "Password or SSH authentication",
		interactive = true,
		arity = "0..1"
	)
	private char[] password;

	@Option(
		names = "--ssh-privatekey",
		order = 4,
		paramLabel = "PATH",
		description = "Path to the private key file for SSH authentication"
	)
	private File privateKey;

	@Option(
		names = "--ssh-timeout",
		order = 5,
		paramLabel = "TIMEOUT",
		description = "Timeout in seconds for SSH operations (default: ${DEFAULT-VALUE} s)",
		defaultValue = "" + DEFAULT_TIMEOUT
	)
	private long timeout;

	@Option(
		names = "--ssh-usesudo-commands",
		order = 6,
		paramLabel = "COMMAND",
		description = "List of commands that requires @|italic sudo|@",
		split = ","
	)
	private Set<String> useSudoCommands;

	@Option(
		names = "--ssh-sudo-command",
		order = 7,
		paramLabel = "SUDO",
		description = "@|italic sudo|@ command (default: ${DEFAULT-VALUE})",
		defaultValue = "sudo"
	)
	private String sudoCommand;

	/**
	 * This method creates an {@link SshConfiguration} for a given username and a given password
	 *
	 * @param defaultUsername Username specified at the top level of the CLI (with the --username option)
	 * @param defaultPassword Password specified at the top level of the CLI (with the --password option)
	 * @return an {@link SshConfiguration} instance corresponding to the options specified by the user in the CLI
	 */
	@Override
	public IConfiguration toProtocol(final String defaultUsername, final char[] defaultPassword) {
		return SshConfiguration
			.sshConfigurationBuilder()
			.username(username == null ? defaultUsername : username)
			.password(username == null ? defaultPassword : password)
			.privateKey(privateKey)
			.useSudoCommands(useSudoCommands)
			.useSudo(true)
			.sudoCommand(sudoCommand)
			.timeout(timeout)
			.build();
	}
}
