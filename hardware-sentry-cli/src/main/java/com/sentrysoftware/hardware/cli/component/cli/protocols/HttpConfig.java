package com.sentrysoftware.hardware.cli.component.cli.protocols;

import lombok.Data;
import picocli.CommandLine.ArgGroup;
import picocli.CommandLine.Option;

@Data
public class HttpConfig {

	@ArgGroup(
			exclusive = true,
			multiplicity = "0..1"
	)
	HttpOrHttps httpOrHttps;
	@Data
	public static class HttpOrHttps {
		@Option(names = "--http", description = "Use HTTP") boolean http;
		@Option(names = "--https", description = "Use HTTPS") boolean https;
	}

	@Option(
			names = "--http-port",
			defaultValue = "443",
			description = "Port for HTTP(S) connections (${DEFAULT-VALUE})"
	)
	int port;

	@Option(
			names = "--http-timeout",
			defaultValue = "120",
			description = "Timeout in seconds for HTTP operations (default: ${DEFAULT-VALUE} s)"
	)
	long timeout;

	@Option(
			names = "--http-username",
			description = "Username for HTTP authentication"
	)
	String username;

	@Option(
			names = "--http-password",
			description = "Password for the HTTP protocol",
			arity = "0..1",
			interactive = true
	)
	char[] password;
}
