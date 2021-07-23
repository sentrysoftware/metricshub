package com.sentrysoftware.hardware.cli.component.cli.protocols;

import lombok.Data;
import picocli.CommandLine.Option;

@Data
public class IPMICredentials {

	@Option(names = "--ipmi-timeout", defaultValue = "120", description = "IPMI-over-LAN Timeout, unit: seconds, default: 120.")
	long timeout;

	@Option(names = "--ipmi-username", description = "Username.")
	String username;

	@Option(names = "--ipmi-password", description = "Password.")
	String password;

	@Option(names = "--ipmi-bmc-key", description = "BMC key that should be provided if the two-key authentication is enabled.")
	String bmcKey;

	@Option(names = "--ipmi-skip-auth", defaultValue =  "false", description = "Whether the IPMI client should skip the authentication or not.")
	boolean skipAuth;
}
