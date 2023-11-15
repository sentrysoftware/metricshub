package com.sentrysoftware.metricshub.cli.service.protocol;

import com.sentrysoftware.metricshub.engine.configuration.HttpConfiguration;
import com.sentrysoftware.metricshub.engine.configuration.IConfiguration;
import lombok.Data;
import lombok.Getter;
import picocli.CommandLine.ArgGroup;
import picocli.CommandLine.Option;

@Data
public class HttpConfigCli implements IProtocolConfigCli {

	public static final int DEFAULT_TIMEOUT = 30;

	@ArgGroup(exclusive = true, multiplicity = "0..1")
	HttpOrHttps httpOrHttps;

	public static class HttpOrHttps {

		@Getter
		@Option(names = "--http", order = 1, description = "Enables HTTP", required = true)
		boolean http;

		@Getter
		@Option(names = "--https", order = 2, description = "Enables HTTPS", required = true)
		boolean https;
	}

	@Option(
		names = "--http-port",
		order = 3,
		paramLabel = "PORT",
		description = "Port for HTTP/HTTPS connections (default: 80 for HTTP, 443 for HTTPS)"
	)
	private Integer port;

	@Option(
		names = { "--http-username" },
		order = 4,
		paramLabel = "USER",
		description = "Username for HTTP authentication"
	)
	private String username;

	@Option(
		names = { "--http-password" },
		order = 5,
		paramLabel = "P4SSW0RD",
		description = "Password for the HTTP protocol",
		arity = "0..1",
		interactive = true
	)
	private char[] password;

	@Option(
		names = "--http-timeout",
		order = 6,
		paramLabel = "TIMEOUT",
		defaultValue = "" + DEFAULT_TIMEOUT,
		description = "Timeout in seconds for HTTP operations (default: ${DEFAULT-VALUE} s)"
	)
	private long timeout;

	/**
	 * @param defaultUsername Username specified at the top level of the CLI (with the --username option)
	 * @param defaultPassword Password specified at the top level of the CLI (with the --password option)
	 * @return an HttpProtocol instance corresponding to the options specified by the user in the CLI
	 */
	@Override
	public IConfiguration toProtocol(String defaultUsername, char[] defaultPassword) {
		return HttpConfiguration
			.builder()
			.https(httpOrHttps.https)
			.port(getOrDeducePortNumber())
			.username(username == null ? defaultUsername : username)
			.password(username == null ? defaultPassword : password)
			.timeout(timeout)
			.build();
	}

	/**
	 * Get or deduce the port number based on the HTTP transport protocol (secured or unsecured)
	 *
	 * @return int value
	 */
	int getOrDeducePortNumber() {
		if (port != null) {
			return port;
		} else if (httpOrHttps.https) {
			return 443;
		}
		return 80;
	}
}
