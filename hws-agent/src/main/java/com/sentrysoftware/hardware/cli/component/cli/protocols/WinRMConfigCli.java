package com.sentrysoftware.hardware.cli.component.cli.protocols;

import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;

import com.sentrysoftware.matrix.engine.protocol.WinRMProtocol;
import com.sentrysoftware.matsya.winrm.service.client.auth.AuthenticationEnum;

import lombok.Data;
import picocli.CommandLine.Option;

@Data
public class WinRMConfigCli implements IProtocolConfigCli {
	public static final int DEFAULT_TIMEOUT = 30;

	@Option(
			names = "--winrm",
			order = 1,
			description = "Enables WinRM"
	)
	private boolean useWinRM;

	@Option(
			names = "--winrm-username",
			order = 2,
			paramLabel = "USER",
			description = "Username for WinRM authentication"
	)
	private String username;

	@Option(
			names = "--winrm-password",
			order = 3,
			paramLabel = "P4SSW0RD",
			description = "Password for WinRM authentication",
			interactive = true,
			arity = "0..1"
	)
	private char[] password;

	@Option(
			names = "--winrm-timeout",
			order = 4,
			paramLabel = "TIMEOUT",
			defaultValue = "" + DEFAULT_TIMEOUT,
			description = "Timeout in seconds for WinRM operations (default: ${DEFAULT-VALUE} s)"
	)
	private Long timeout;

	@Option(
			names = "--winrm-namespace",
			order = 5,
			paramLabel = "NAMESPACE",
			description = "Force a specific namespace for connectors that perform namespace auto-detection (advanced)"
	)
	private String namespace;

	@Option(
			names = "--winrm-command",
			order = 5,
			paramLabel = "COMMAND",
			description = "Force a specific command for connectors that perform command auto-detection (advanced)"
	)
	private String command;

	@Option(
			names = "--winrm-workingdirectory",
			order = 5,
			paramLabel = "WORKINGDIRECTORY",
			description = "Force a specific workingDirectory for connectors that perform workingDirectory auto-detection (advanced)"
	)
	private String workingDirectory;

	@Option(
			names = "--winrm-force-port",
			order = 5,
			paramLabel = "PORT",
			description = "Force a specific port for connectors that perform port auto-detection (advanced)"
	)
	private Integer port;

	@Option(
			names = "--winrm-protocol",
			order = 5,
			paramLabel = "PROTOCOL",
			description = "Force a specific protocol for connectors that perform protocol auto-detection (advanced)"
	)
	private String protocol;

	@Option(
			names = "--winrm-ticketcache",
			order = 5,
			paramLabel = "TICKETCACHE",
			description = "Force a specific ticketCache for connectors that perform ticketCache auto-detection (advanced)"
	)
	private Path ticketCache;

	@Option(
			names = "--winrm-forcentlm",
			order = 5,
			paramLabel = "FORCENTLM",
			description = "Force Ntlm"
	)
	private boolean forceNtlm;

	@Option(
			names = "--winrm-kerberosonly",
			order = 5,
			paramLabel = "kerberosOnly",
			description = "Force kerberos only"
	)
	private boolean kerberosOnly;

	/**
	 * @param defaultUsername Username specified at the top level of the CLI (with the --username option)
	 * @param defaultPassword Password specified at the top level of the CLI (with the --password option)
	 * @return an WinRMProtocol instance corresponding to the options specified by the user in the CLI
	 */
	@Override
	public WinRMProtocol toProtocol(String defaultUsername, char[] defaultPassword) {
		List<AuthenticationEnum> authentications = new ArrayList<>();
		if (kerberosOnly) {
			authentications.add(AuthenticationEnum.KERBEROS);
		} else if (forceNtlm) {
			authentications.add(AuthenticationEnum.NTLM);
		} else {
			authentications = null;
		}
		
		return WinRMProtocol
		.builder()
		.username(username == null ? defaultUsername : username)
		.password(username == null ? defaultPassword : password)
		.namespace(namespace)
		.command(command)
		.workingDirectory(workingDirectory)
		.port(port)
		.protocol(protocol)
		.ticketCache(ticketCache)
		.authentications(authentications)
		.timeout(timeout)
		.build();

	}
}
