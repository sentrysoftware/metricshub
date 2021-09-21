package com.sentrysoftware.hardware.cli.component.cli.protocols;

import com.sentrysoftware.matrix.engine.protocol.WMIProtocol;

import lombok.Data;
import picocli.CommandLine.Option;

@Data
public class WmiConfigCli implements IProtocolConfigCli {

	public static final int DEFAULT_TIMEOUT = 30;

	@Option(
			names = "--wmi",
			order = 1,
			description = "Enables WMI"
	)
	private boolean useWmi;

	@Option(
			names = "--wmi-username",
			order = 2,
			paramLabel = "USER",
			description = "Username for WMI authentication"
	)
	private String username;

	@Option(
			names = "--wmi-password",
			order = 3,
			paramLabel = "P4SSW0RD",
			description = "Password for WMI authentication",
			interactive = true,
			arity = "0..1"
	)
	private char[] password;

	@Option(
			names = "--wmi-timeout",
			order = 4,
			paramLabel = "TIMEOUT",
			defaultValue = "" + DEFAULT_TIMEOUT,
			description = "Timeout in seconds for WBEM operations (default: ${DEFAULT-VALUE} s)"
	)
	private Long timeout;

	@Option(
			names = "--wmi-force-namespace",
			order = 5,
			paramLabel = "NAMESPACE",
			description = "Force a specific namespace for connectors that perform namespace auto-detection (advanced)"
	)
	private String namespace;

	/**
	 * @param defaultUsername Username specified at the top level of the CLI (with the --username option)
	 * @param defaultPassword Password specified at the top level of the CLI (with the --password option)
	 * @return an WMIProtocol instance corresponding to the options specified by the user in the CLI
	 */
	@Override
	public WMIProtocol toProtocol(String defaultUsername, char[] defaultPassword) {
		return WMIProtocol
				.builder()
				.username(username == null ? defaultUsername : username)
				.password(username == null ? defaultPassword : password)
				.namespace(namespace)
				.timeout(timeout)
				.build();

	}

}