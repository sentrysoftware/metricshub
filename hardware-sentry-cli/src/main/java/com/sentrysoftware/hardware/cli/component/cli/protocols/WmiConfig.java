package com.sentrysoftware.hardware.cli.component.cli.protocols;


import lombok.Data;
import picocli.CommandLine.Option;

@Data
public class WmiConfig {

	@Option(
			names = "--wmi",
			description = "Enables WMI"
	)
	private boolean useWmi;

	@Option(
			names = "--wmi-username",
			description = "Username for WMI authentication"
	)
	private String username;

	@Option(
			names = "--wmi-password",
			description = "Password for WMI authentication",
			interactive = true,
			arity = "0..1"
	)
	private char[] password;

	@Option(
			names = "--wmi-force-namespace",
			description = "Force a specific namespace for connectors that perform namespace auto-detection (advanced)"
	)
	private String namespace;

	@Option(
			names = "--wmi-timeout",
			defaultValue = "120",
			description = "Timeout in seconds for WBEM operations (default: ${DEFAULT-VALUE} s)"
	)
	private Long timeout;

}