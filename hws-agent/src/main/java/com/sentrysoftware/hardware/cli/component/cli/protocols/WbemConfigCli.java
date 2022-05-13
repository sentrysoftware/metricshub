package com.sentrysoftware.hardware.cli.component.cli.protocols;

import com.sentrysoftware.hardware.cli.component.cli.converters.WbemTransportProtocolConverter;
import com.sentrysoftware.matrix.engine.protocol.WbemProtocol;
import com.sentrysoftware.matrix.engine.protocol.WbemProtocol.WbemProtocols;

import lombok.Data;
import picocli.CommandLine.Option;

@Data
public class WbemConfigCli implements IProtocolConfigCli {

	public static final int DEFAULT_TIMEOUT = 30;

	@Option(
			names = "--wbem",
			order = 1,
			description = "Enables WBEM"
	)
	private boolean useWbem;

	@Option(
			names = "--wbem-transport",
			order = 2,
			defaultValue = "HTTPS",
			paramLabel = "HTTP|HTTPS",
			description = "Transport protocol for WBEM (default: ${DEFAULT-VALUE})",
			converter = WbemTransportProtocolConverter.class
	)
	WbemProtocol.WbemProtocols protocol;

	@Option(
			names = "--wbem-port",
			order = 3,
			paramLabel = "PORT",
			description = "Port of the WBEM server (default: 5988 for HTTP, 5989 for HTTPS)"
	)
	Integer port;

	@Option(
			names = "--wbem-username",
			order = 4,
			paramLabel = "USER",
			description = "Username for WBEM authentication"
	)
	String username;

	@Option(
			names = "--wbem-password",
			order = 5,
			paramLabel = "P4SSW0RD",
			description = "Password for WBEM authentication",
			interactive = true,
			arity = "0..1"
	)
	char[] password;

	@Option(
			names = "--wbem-timeout",
			order = 6,
			defaultValue = "" + DEFAULT_TIMEOUT,
			paramLabel = "TIMEOUT",
			description = "Timeout in seconds for WBEM operations (default: ${DEFAULT-VALUE} s)")
	long timeout;

	@Option(
			names = "--wbem-force-namespace",
			order = 7,
			paramLabel = "NAMESPACE",
			description = "Force a specific namespace for connectors that perform namespace auto-detection (advanced)"
	)
	String namespace;

	/**
	 * @param defaultUsername Username specified at the top level of the CLI (with the --username option)
	 * @param defaultPassword Password specified at the top level of the CLI (with the --password option)
	 * @return an WBEMProtocol instance corresponding to the options specified by the user in the CLI
	 */
	@Override
	public WbemProtocol toProtocol(String defaultUsername, char[] defaultPassword) {
		return WbemProtocol
				.builder()
				.protocol(protocol)
				.port(port != null ? port : protocol == WbemProtocols.HTTP ? 5988 : 5989)
				.username(username == null ? defaultUsername : username)
				.password(username == null ? defaultPassword : password)
				.namespace(namespace)
				.timeout(timeout)
				.build();
	}

}