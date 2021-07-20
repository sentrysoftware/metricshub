package com.sentrysoftware.hardware.cli.component.cli.protocols;

import lombok.Data;
import picocli.CommandLine.Option;

@Data
public class WBEMCredentials {
	@Option(names = "--wbem-protocol", defaultValue = "HTTPS", description = "WBEM Protocol, default : https")
	String protocol;

	@Option(names = "--wbem-port", defaultValue = "5989", description = "WBEM Port, default : 5989.")
	int port;

	@Option(names = "--wbem-namespace", defaultValue = "root/cimv2", description = "WBEM namespace, default : root/cimv2.")
	String namespace;

	@Option(names = "--wbem-timeout", defaultValue = "120", description = "WBEM Timeout, unit: seconds, default: 120.")
	long timeout;

	@Option(names = "--wbem-username", description = "Username.")
	String username;

	@Option(names = "--wbem-password", description = "Password.")
	String password;

}