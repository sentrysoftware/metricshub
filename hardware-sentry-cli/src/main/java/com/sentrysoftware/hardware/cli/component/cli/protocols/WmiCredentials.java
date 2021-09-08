package com.sentrysoftware.hardware.cli.component.cli.protocols;


import lombok.Data;
import picocli.CommandLine.Option;

@Data
public class WmiCredentials {

	@Option(names = "--wmi-username",
			description = "Username to establish the connection with the device.")
	private String username;

	@Option(names = "--wmi-password",
			description = "Password to establish the connection with the device.")
	private String password;

	@Option(names = "--wmi-namespace",
			description = "WMI Namespace, leave blank to let the solution detect the proper namespace, default: ${DEFAULT-VALUE}.",
			defaultValue = "root/cimv2")
	private String namespace;

	@Option(names = "--wmi-timeout",
			defaultValue = "120",
			description = "WMI Timeout, unit: seconds, default: ${DEFAULT-VALUE}.")
	private Long timeout;

}