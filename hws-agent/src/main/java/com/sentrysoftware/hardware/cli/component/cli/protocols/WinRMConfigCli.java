package com.sentrysoftware.hardware.cli.component.cli.protocols;

import java.util.ArrayList;
import java.util.List;

import com.sentrysoftware.matrix.engine.protocol.WinRMProtocol;
import com.sentrysoftware.matsya.winrm.service.client.auth.AuthenticationEnum;

import lombok.Data;
import picocli.CommandLine.Option;

@Data
public class WinRMConfigCli implements IProtocolConfigCli {
	public static final int DEFAULT_TIMEOUT = 30;
	public static final String DEFAULT_PROTOCOL = "HTTP";
	public static final Integer DEFAULT_HTTP_PORT = 5985;
	public static final Integer DEFAULT_HTTPS_PORT = 5986;

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
			description = "Namespace for a WinRM query"
			)
	private String namespace;

	@Option(
			names = "--winrm-command",
			order = 5,
			paramLabel = "COMMAND",
			description = "WinRM query to execute"
			)
	private String command;

	@Option(
			names = "--winrm-port",
			order = 5,
			paramLabel = "PORT",
			description = "Specific port on which execute the WinRM query. By default : 5985 for HTTP and 5986 fr HTTPS."
			)
	private Integer port;

	@Option(
			names = "--winrm-protocol",
			order = 5,
			paramLabel = "PROTOCOL",
			defaultValue = DEFAULT_PROTOCOL,
			description = "Protocol to use to execute the query (default: ${DEFAULT-VALUE})"
			)
	private String protocol;

	@Option(
			names = "--winrm-forcentlm",
			order = 5,
			paramLabel = "FORCENTLM",
			description = "Force the use of Ntlm"
			)
	private boolean forceNtlm;

	@Option(
			names = "--winrm-kerberosonly",
			order = 5,
			paramLabel = "kerberosOnly",
			description = "Force the use of kerberos only"
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

		if (port == null) {
			if ("HTTP".equals(protocol)) {
				port = 5985;
			} else {
				port = 5986;
			} 
		}

		return WinRMProtocol
				.builder()
				.username(username == null ? defaultUsername : username)
				.password(username == null ? defaultPassword : password)
				.namespace(namespace)
				.command(command)
				.port(port)
				.https(!"HTTP".equals(protocol))
				.authentications(authentications)
				.timeout(timeout)
				.build();
	}
}
