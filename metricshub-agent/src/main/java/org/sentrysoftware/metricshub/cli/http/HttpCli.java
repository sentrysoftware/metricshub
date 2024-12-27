package org.sentrysoftware.metricshub.cli.http;

/*-
 * ╱╲╱╲╱╲╱╲╱╲╱╲╱╲╱╲╱╲╱╲╱╲╱╲╱╲╱╲╱╲╱╲╱╲╱╲╱╲╱╲
 * MetricsHub Agent
 * ჻჻჻჻჻჻
 * Copyright 2023 - 2024 Sentry Software
 * ჻჻჻჻჻჻
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Affero General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU Affero General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 * ╲╱╲╱╲╱╲╱╲╱╲╱╲╱╲╱╲╱╲╱╲╱╲╱╲╱╲╱╲╱╲╱╲╱╲╱╲╱╲╱
 */

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.node.BooleanNode;
import com.fasterxml.jackson.databind.node.IntNode;
import com.fasterxml.jackson.databind.node.JsonNodeFactory;
import com.fasterxml.jackson.databind.node.ObjectNode;
import com.fasterxml.jackson.databind.node.TextNode;
import java.io.IOException;
import java.io.PrintWriter;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.Callable;
import lombok.Data;
import org.fusesource.jansi.Ansi;
import org.fusesource.jansi.AnsiConsole;
import org.sentrysoftware.metricshub.cli.service.CliExtensionManager;
import org.sentrysoftware.metricshub.cli.service.ConsoleService;
import org.sentrysoftware.metricshub.cli.service.MetricsHubCliService;
import org.sentrysoftware.metricshub.cli.service.MetricsHubCliService.CliPasswordReader;
import org.sentrysoftware.metricshub.cli.service.PrintExceptionMessageHandlerService;
import org.sentrysoftware.metricshub.engine.common.IQuery;
import org.sentrysoftware.metricshub.engine.configuration.IConfiguration;
import picocli.CommandLine;
import picocli.CommandLine.Command;
import picocli.CommandLine.Model.CommandSpec;
import picocli.CommandLine.Option;
import picocli.CommandLine.ParameterException;
import picocli.CommandLine.Spec;

/**
 * CLI for executing HTTP requests with validation and support for various operations.
 */
@Data
@Command(name = "http.exe", description = "\nList of valid options: \n", footer = HttpCli.FOOTER, usageHelpWidth = 180)
public class HttpCli implements IQuery, Callable<Integer> {

	/**
	 * The HTTP protocol identifier.
	 */
	public static final String HTTP = "http";
	/**
	 * The HTTPS protocol identifier.
	 */
	public static final String HTTPS = "https";
	/**
	 * The default port for HTTP connections.
	 */
	public static final int DEFAULT_HTTP_PORT = 80;
	/**
	 * The default port for HTTPS connections.
	 */
	public static final int DEFAULT_HTTPS_PORT = 443;
	/**
	 * The set of supported HTTP methods.
	 */
	public static final Set<String> HTTP_METHODS = Set.of("GET", "POST", "PUT", "DELETE");

	/**
	 * Footer regrouping HTTP CLI examples
	 */
	public static final String FOOTER =
		"""

		Examples:

		http --method <GET|POST|PUT|DELETE> --url <URL> --username <USERNAME> --password <PASSWORD> [--body <BODY> or --body-file <BODY FILE PATH>] \
		[--header <HEADER> --header <HEADER> or --header-file <HEADER FILE PATH>] --token <TOKEN> --timeout <TIMEOUT>

		@|green # HTTP GET request with a body and two headers.|@
		http --method get --url https://dev-01:443/users --username username --password password --header="Content-Type:application/xml" \
		--header="Accept:application/json" --body="<aaaLogin inName="username" inPassword="password" />" --token="AF65B4SG44AHJUE5R" --timeout 2m

		@|green # HTTP POST request with a header file and a body file.|@
		http --method post --url https://dev-01:443/users --username admin --password pass --header-file="/opt/metricshub/header.txt" \
		--body-file="/opt/metricshub/body.txt" --token="AF65B4SG44AHJUE5R" --timeout 2m

		Note: If --password is not provided, you will be prompted interactively.
		""";

	@Spec
	CommandSpec spec;

	@Option(names = "--url", order = 1, paramLabel = "URL", description = "Url for HTTP request.")
	private String url;

	@Option(names = "--method", order = 2, paramLabel = "METHOD", description = "HTTP request type (GET|POST|PUT|DELETE)")
	private String method;

	@Option(
		names = "--header",
		order = 3,
		paramLabel = "HEADER",
		split = ":",
		description = "Headers to be added to the HTTP request. Repeatable for multiple headers."
	)
	private Map<String, String> headers;

	@Option(
		names = { "--header-file", "--headerfile" },
		order = 4,
		paramLabel = "HEADERFILE",
		description = "Path of the file containing header to be added to the HTTP request."
	)
	private String headerFile;

	@Option(names = "--body", order = 5, paramLabel = "BODY", description = "Body of the HTTP request.")
	private String body;

	@Option(
		names = { "--body-file", "--bodyfile" },
		order = 6,
		paramLabel = "BODYFILE",
		description = "Path of the file containing the HTTP request body."
	)
	private String bodyFile;

	@Option(
		names = { "--authentication-token", "--token" },
		order = 7,
		paramLabel = "TOKEN",
		description = " The authentication token for the HTTP request."
	)
	private String authenticationToken;

	@Option(names = { "--username" }, order = 8, paramLabel = "USER", description = "Username for HTTP authentication")
	private String username;

	@Option(
		names = { "--password" },
		order = 9,
		paramLabel = "P4SSW0RD",
		description = "Password for the HTTP protocol",
		arity = "0..1",
		interactive = true
	)
	private char[] password;

	@Option(
		names = "--timeout",
		order = 10,
		paramLabel = "TIMEOUT",
		defaultValue = "120",
		description = "Timeout in seconds for HTTP operations (default: ${DEFAULT-VALUE} s)"
	)
	private String timeout;

	@Option(
		names = { "-h", "-?", "--help" },
		order = 11,
		usageHelp = true,
		description = "Shows this help message and exits"
	)
	boolean usageHelpRequested;

	@Option(names = "-v", order = 12, description = "Verbose mode (repeat the option to increase verbosity)")
	boolean[] verbose;

	java.net.URL parsedUrl;

	PrintWriter printWriter;

	@Override
	public JsonNode getQuery() {
		final ObjectNode queryNode = JsonNodeFactory.instance.objectNode();

		queryNode.set("method", new TextNode(method != null ? method.toUpperCase() : "GET"));

		queryNode.set("url", new TextNode(url));

		try {
			final String headerContent = getHeaderContent();
			if (headerContent != null) {
				queryNode.set("header", new TextNode(headerContent));
			}
		} catch (IOException e) {
			throw new IllegalStateException("Could not read the header.", e);
		}

		try {
			final String bodyContent = getBodyContent();
			if (bodyContent != null) {
				queryNode.set("body", new TextNode(bodyContent));
			}
		} catch (IOException e) {
			throw new IllegalStateException("Could not read the body.", e);
		}

		queryNode.set("resultContent", new TextNode("all"));

		if (authenticationToken != null) {
			queryNode.set("authenticationToken", new TextNode(authenticationToken));
		}

		return queryNode;
	}

	/**
	 * Retrieves the body content for the HTTP request.
	 * If the body is specified directly, it is returned.
	 * If a body file is specified, its contents are read and returned.
	 *
	 * @return the body content as a string, or null if neither body nor body file is set.
	 * @throws IOException if an error occurs while reading the bodyFile.
	 */
	public String getBodyContent() throws IOException {
		if (body != null) {
			return body;
		} else if (bodyFile != null) {
			return Files.readString(Path.of(bodyFile), StandardCharsets.UTF_8);
		} else {
			return null;
		}
	}

	/**
	 * Retrieves the header content for the HTTP request.
	 * If headers are specified directly, they are formatted and returned.
	 * If a header file is specified, its contents are read and returned.
	 *
	 * @return the header content as a string, or null if neither headers nor a header file is set.
	 * @throws IOException if an error occurs while reading the headerFile.
	 */
	public String getHeaderContent() throws IOException {
		StringBuilder header = new StringBuilder();

		if (headers != null) {
			headers.forEach((key, value) -> header.append(String.format("%s: %s%n", key, value)));
			return header.toString();
		} else if (headerFile != null) {
			header.append(Files.readString(Path.of(headerFile), StandardCharsets.UTF_8));
			return header.toString();
		} else {
			return null;
		}
	}

	/**
	 * Try to start the interactive mode to request and set HTTP password
	 *
	 * @param passwordReader password reader which displays the prompt text and wait for user's input
	 */
	void tryInteractivePassword(final CliPasswordReader<char[]> passwordReader) {
		if (username != null && password == null) {
			password = (passwordReader.read("%s password for HTTP: ", username));
		}
	}

	/**
	 * Validates the HTTP request configuration and parameters.
	 * Ensures required fields are set and checks for conflicts in headers and body configuration.
	 *
	 * @throws ParameterException if validation fails due to missing or conflicting parameters.
	 */
	void validate() throws ParameterException {
		// Can we ask for passwords interactively?
		final boolean interactive = ConsoleService.hasConsole();

		// Password
		if (interactive) {
			tryInteractivePassword(System.console()::readPassword);
		}

		// Validating HTTP Url
		validateUrl();

		// Validating HTTP methods
		if (method != null && !HTTP_METHODS.contains(method.toUpperCase())) {
			throw new ParameterException(spec.commandLine(), String.format("Unknown HTTP request method: %s.", method));
		}

		// Validating headers
		if (headers != null && headerFile != null) {
			throw new ParameterException(
				spec.commandLine(),
				"Conflict - Two headers have been configured: --header and --header-file."
			);
		} else if (headerFile != null) {
			try {
				getHeaderContent();
			} catch (IOException e) {
				throw new ParameterException(
					spec.commandLine(),
					String.format("Error while reading header file %s : %s", headerFile, e)
				);
			}
		}

		// Validating body
		if (body != null && bodyFile != null) {
			throw new ParameterException(
				spec.commandLine(),
				"Conflict - Two bodies have been configured: --body and --body-file."
			);
		} else if (bodyFile != null) {
			try {
				getBodyContent();
			} catch (IOException e) {
				throw new ParameterException(
					spec.commandLine(),
					String.format("Error while reading body file %s : %s", bodyFile, e)
				);
			}
		}
	}

	/**
	 * Validates the URL's syntax using Java's URL and URI classes.
	 *
	 * @throws ParameterException if the URL is invalid
	 */
	void validateUrl() {
		try {
			// Performing a basic validation of the URL format
			parsedUrl = new java.net.URL(url);
			// Enforces stricter compliance, catching invalid characters.
			parsedUrl.toURI();
		} catch (Exception e) {
			throw new ParameterException(spec.commandLine(), "Invalid URL: " + e.getMessage(), e);
		}
	}

	/**
	 * Resolves the port number from the URL or returns a default value.
	 *
	 * @return the resolved or default port number
	 * @throws ParameterException if the URL is invalid
	 */
	int resolvePortFromUrl() {
		try {
			// Check if the port is explicitly specified
			int port = parsedUrl.getPort();
			if (port != -1) {
				return port; // Port found in the URL
			}

			// Determine the default port based on protocol
			String protocol = parsedUrl.getProtocol();
			if (HTTP.equalsIgnoreCase(protocol)) {
				return DEFAULT_HTTP_PORT;
			} else if (HTTPS.equalsIgnoreCase(protocol)) {
				return DEFAULT_HTTPS_PORT;
			}
		} catch (Exception e) {
			throw new ParameterException(spec.commandLine(), "Invalid URL: " + e.getMessage(), e);
		}

		// Default to 443 if no protocol or port is found
		return DEFAULT_HTTPS_PORT;
	}

	/**
	 * Entry point for the HTTP CLI application. Initializes necessary configurations,
	 * processes command line arguments, and executes the CLI.
	 *
	 * @param args The command line arguments passed to the application.
	 */
	public static void main(String[] args) {
		System.setProperty("log4j2.configurationFile", "log4j2-cli.xml");

		// Enable colors on Windows terminal
		AnsiConsole.systemInstall();

		final CommandLine cli = new CommandLine(new HttpCli());

		// Keep the below line commented for future reference
		// Using JAnsi on Windows breaks the output of Unicode (UTF-8) chars
		// It can be fixed using the below line... when running in Windows Terminal
		// and not CMD.EXE.
		// As this is poorly documented, we keep this for future improvement.
		// cli.setOut(new PrintWriter(AnsiConsole.out(), true, StandardCharsets.UTF_8)); // NOSONAR on commented code

		// Set the exception handler
		cli.setExecutionExceptionHandler(new PrintExceptionMessageHandlerService());

		// Allow case insensitive enum values
		cli.setCaseInsensitiveEnumValuesAllowed(true);

		// Allow case insensitive options
		cli.setOptionsCaseInsensitive(true);

		// Execute the command
		final int exitCode = cli.execute(args);

		// Cleanup Windows terminal settings
		AnsiConsole.systemUninstall();

		System.exit(exitCode);
	}

	@Override
	public Integer call() throws Exception {
		// Validate the entries
		validate();
		// Gets the output writer from the command line spec.
		printWriter = spec.commandLine().getOut();
		// Set the logger level
		MetricsHubCliService.setLogLevel(verbose);
		// Find an extension to execute the query
		CliExtensionManager
			.getExtensionManagerSingleton()
			.findExtensionByType(HTTP)
			.ifPresent(extension -> {
				try {
					// Create and fill in a configuration ObjectNode
					final ObjectNode configurationNode = JsonNodeFactory.instance.objectNode();

					configurationNode.set(HTTPS, BooleanNode.valueOf(parsedUrl.getProtocol().equals(HTTPS)));
					configurationNode.set("port", new IntNode(resolvePortFromUrl()));
					configurationNode.set("timeout", new TextNode(timeout));
					configurationNode.set("username", new TextNode(username));
					configurationNode.set("password", new TextNode(String.valueOf(password)));

					// Build an IConfiguration from the configuration ObjectNode
					IConfiguration configuration = extension.buildConfiguration(HTTP, configurationNode, null);
					configuration.setHostname(parsedUrl.getHost());

					// display the request
					displayRequest();
					// Execute the HTTP query
					final String result = extension.executeQuery(configuration, getQuery());
					// display the returned result
					displayResult(result);
				} catch (Exception e) {
					throw new IllegalStateException(String.format("Failed to execute HTTP %s query.%n", method), e);
				}
			});
		return CommandLine.ExitCode.OK;
	}

	/**
	 * Displays the details of an HTTP request in a formatted manner.
	 */
	void displayRequest() {
		printWriter.println(
			String.format("Hostname %s - Executing %s %s request:", parsedUrl.getHost(), parsedUrl.getProtocol(), method)
		);

		printWriter.println(Ansi.ansi().a("Url: ").fgBrightBlack().a(url).reset().toString());

		try {
			final String headerContent = getHeaderContent();
			if (headerContent != null) {
				printWriter.println(Ansi.ansi().a("Header: ").fgBrightBlack().a(headerContent).reset().toString());
			}
		} catch (Exception e) {
			throw new ParameterException(spec.commandLine(), String.format("Error while reading the header: %s", e));
		}

		try {
			final String bodyContent = getBodyContent();
			if (bodyContent != null) {
				printWriter.println(Ansi.ansi().a("Body: ").fgBrightBlack().a(bodyContent).reset().toString());
			}
		} catch (Exception e) {
			throw new ParameterException(spec.commandLine(), String.format("Error while reading the body: %s", e));
		}

		if (authenticationToken != null) {
			printWriter.println(
				Ansi.ansi().a("AuthenticationToken: ").fgBrightBlack().a(authenticationToken).reset().toString()
			);
		}
		printWriter.flush();
	}

	/**
	 * Prints the query result.
	 *
	 * @param result      the query result
	 */
	void displayResult(String result) {
		printWriter.println(Ansi.ansi().fgBlue().bold().a("Result: \n").reset().a(result).toString());
		printWriter.flush();
	}
}
