package com.sentrysoftware.hardware.cli.component.cli.protocols;

import com.sentrysoftware.matrix.engine.protocol.WBEMProtocol;

import lombok.Data;
import picocli.CommandLine.Option;
import com.sentrysoftware.hardware.cli.component.cli.converters.WbemTransportProtocolConverter;

@Data
public class WbemConfig {

	@Option(
			names = "--wbem-transport",
			defaultValue = "HTTPS",
			description = "Transport protocol for WBEM (default: ${DEFAULT-VALUE})",
			converter = WbemTransportProtocolConverter.class
	)
	WBEMProtocol.WBEMProtocols protocol;

	@Option(
			names = "--wbem-port",
			defaultValue = "5989",
			description = "Port of the WBEM server (default : ${DEFAULT-VALUE})"
	)
	int port;

	@Option(
			names = "--wbem-force-namespace",
			description = "Force a specific namespace for connectors that perform namespace auto-detection (advanced)"
	)
	String namespace;

	@Option(
			names = "--wbem-timeout",
			defaultValue = "120",
			description = "Timeout in seconds for WBEM operations (default: ${DEFAULT-VALUE} s)")
	long timeout;

	@Option(
			names = "--wbem-username",
			description = "Username for WBEM authentication"
	)
	String username;

	@Option(
			names = "--wbem-password",
			description = "Password for WBEM authentication",
			interactive = true,
			arity = "0..1"
	)
	char[] password;

}