package com.sentrysoftware.hardware.cli.component.cli.protocols;

import lombok.Data;
import picocli.CommandLine.Option;

@Data
public class IpmiConfig {

	@Option(
			names = "--ipmi-timeout",
			defaultValue = "120",
			description = "IPMI-over-LAN timeout, in seconds (default: 120)"
	)
	long timeout;

	@Option(
			names = "--ipmi-username",
			description = "Username for IPMI-over-LAN authentication"
	)
	String username;

	@Option(
			names = "--ipmi-password",
			description = "Password for IPMI-over-LAN authentication",
			interactive = true,
			arity = "0..1"
	)
	char[] password;

	@Option(
			names = "--ipmi-bmc-key",
			description = "BMC key for IPMI-over-LAN two-key authentication (if enabled)"
	)
	String bmcKey;

	@Option(
			names = "--ipmi-skip-auth",
			defaultValue =  "false",
			description = "Whether to skip IPMI-over-LAN authentication"
	)
	boolean skipAuth;
}
