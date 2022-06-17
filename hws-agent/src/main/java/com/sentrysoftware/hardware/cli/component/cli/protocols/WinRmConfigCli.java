package com.sentrysoftware.hardware.cli.component.cli.protocols;

import java.util.List;

import com.sentrysoftware.hardware.cli.component.cli.converters.TransportProtocolConverter;
import com.sentrysoftware.matrix.engine.protocol.TransportProtocols;
import com.sentrysoftware.matrix.engine.protocol.WinRmProtocol;
import com.sentrysoftware.matsya.winrm.service.client.auth.AuthenticationEnum;

import lombok.Data;
import picocli.CommandLine.Option;

@Data
public class WinRmConfigCli implements IProtocolConfigCli {
	public static final int DEFAULT_TIMEOUT = 30;
	public static final Integer DEFAULT_HTTP_PORT = 5985;
	public static final Integer DEFAULT_HTTPS_PORT = 5986;

	@Option(
		names = "--winrm",
		order = 1,
		description = "Enables WinRM"
	)
	private boolean useWinRm;

	@Option(
		names = "--winrm-transport",
		order = 2,
		paramLabel = "HTTP|HTTPS",
		defaultValue = "HTTP",
		description = "Transport protocol for WinRM (default: ${DEFAULT-VALUE})",
		converter = TransportProtocolConverter.class
	)
	private TransportProtocols protocol;

	@Option(
		names = "--winrm-port",
		order = 3,
		paramLabel = "PORT",
		description = "Port of the WinRM service. By default : 5985 for HTTP and 5986 for HTTPS"
	)
	private Integer port;

	@Option(
		names = "--winrm-username",
		order = 4,
		paramLabel = "USER",
		description = "Username for WinRM authentication"
	)
	private String username;

	@Option(
		names = "--winrm-password",
		order = 5,
		paramLabel = "P4SSW0RD",
		description = "Password for WinRM authentication",
		interactive = true,
		arity = "0..1"
	)
	private char[] password;

	@Option(
		names = "--winrm-timeout",
		order = 6,
		paramLabel = "TIMEOUT",
		defaultValue = "" + DEFAULT_TIMEOUT,
		description = "Timeout in seconds for WinRM operations (default: ${DEFAULT-VALUE} s)"
	)
	private Long timeout;

	@Option(
		names = "--winrm-auth",
		description = "Comma-separated ordered list of authentication schemes."
				+ " Possible values are NTLM and KERBEROS. By default, only NTLM is used",
		order = 7,
		paramLabel = "AUTH",
		split = ","
	)
	private List<AuthenticationEnum> authentications;

	@Option(
		names = "--winrm-force-namespace",
		order = 8,
		paramLabel = "NAMESPACE",
		description = "Force a specific namespace for connectors that perform namespace auto-detection (advanced)"
	)
	private String namespace;

	/**
	 * @param defaultUsername Username specified at the top level of the CLI (with the --username option)
	 * @param defaultPassword Password specified at the top level of the CLI (with the --password option)
	 * @return an WinRMProtocol instance corresponding to the options specified by the user in the CLI
	 */
	@Override
	public WinRmProtocol toProtocol(String defaultUsername, char[] defaultPassword) {
		if (port == null) {
			if (TransportProtocols.HTTP.equals(protocol)) {
				port = 5985;
			} else {
				port = 5986;
			} 
		}

		return WinRmProtocol
			.builder()
			.username(username == null ? defaultUsername : username)
			.password(username == null ? defaultPassword : password)
			.namespace(namespace)
			.port(port)
			.protocol(protocol)
			.authentications(authentications)
			.timeout(timeout)
			.build();
	}
}
