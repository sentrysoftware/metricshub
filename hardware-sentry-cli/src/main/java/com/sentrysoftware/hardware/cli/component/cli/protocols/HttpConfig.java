package com.sentrysoftware.hardware.cli.component.cli.protocols;

import com.sentrysoftware.matrix.engine.protocol.HTTPProtocol;

import lombok.Data;
import lombok.Getter;
import picocli.CommandLine.ArgGroup;
import picocli.CommandLine.Option;

@Data
public class HttpConfig implements IProtocolConfig {

	public static final int DEFAULT_TIMEOUT = 30;

	@ArgGroup(
			exclusive = true,
			multiplicity = "0..1"
	)
	HttpOrHttps httpOrHttps;

	public static class HttpOrHttps {
		@Getter
		@Option(
				names = "--http",
				order = 1,
				description = "Enables HTTP",
				required = true
		)
		private boolean http;
		@Getter
		@Option(
				names = "--https",
				order = 2,
				description = "Enables HTTPS",
				required = true
		)
		private boolean https;
	}

	@Option(
			names = "--http-port",
			order = 3,
			description = "Port for HTTP/HTTPS connections (default: 80 for HTTP, 443 for HTTPS)"
	)
	private Integer port;

	@Option(
			names = "--http-username",
			order = 4,
			description = "Username for HTTP authentication"
	)
	private String username;

	@Option(
			names = "--http-password",
			order = 5,
			description = "Password for the HTTP protocol",
			arity = "0..1",
			interactive = true
	)
	private char[] password;

	@Option(
			names = "--http-timeout",
			order = 6,
			defaultValue = "" + DEFAULT_TIMEOUT,
			description = "Timeout in seconds for HTTP operations (default: ${DEFAULT-VALUE} s)"
	)
	private long timeout;

	/**
	 * @param defaultUsername Username specified at the top level of the CLI (with the --username option)
	 * @param defaultPassword Password specified at the top level of the CLI (with the --password option)
	 * @return an HTTPProtocol instance corresponding to the options specified by the user in the CLI
	 */
	@Override
	public HTTPProtocol toProtocol(String defaultUsername, char[] defaultPassword) {
		return HTTPProtocol
				.builder()
				.https(httpOrHttps.https)
				.port(port != null ? port : httpOrHttps.https ? 443 : 80)
				.username(username == null ? defaultUsername : username)
				.password(username == null ? defaultPassword : password)
				.timeout(timeout)
				.build();
	}

}
