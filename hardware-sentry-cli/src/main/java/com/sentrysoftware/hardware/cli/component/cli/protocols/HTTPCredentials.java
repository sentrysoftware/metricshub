package com.sentrysoftware.hardware.cli.component.cli.protocols;

import lombok.Data;
import picocli.CommandLine.Option;

@Data
public class HTTPCredentials {

	@Option(names = "--https", defaultValue = "false", description = "Use HTTPS, default : false.")
	boolean https;

	@Option(names = "--http-port", defaultValue = "8080", description = "HTTP(S) Port, default : 8080.")
	int port;

	@Option(names = "--http-timeout", defaultValue = "120", description = "HTTP(S) Timeout, unit: seconds, default: 120.")
	long timeout;

	@Option(names = "--http-username", description = "Username.")
	String username;

	@Option(names = "--http-password", description = "Password.")
	String password;
}
