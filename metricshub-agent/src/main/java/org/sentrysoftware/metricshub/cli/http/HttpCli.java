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
import org.fusesource.jansi.AnsiConsole;
import org.sentrysoftware.metricshub.cli.service.CliExtensionManager;
import org.sentrysoftware.metricshub.cli.service.PrintExceptionMessageHandlerService;
import org.sentrysoftware.metricshub.cli.service.protocol.HttpConfigCli;
import org.sentrysoftware.metricshub.engine.common.IQuery;
import org.sentrysoftware.metricshub.engine.configuration.IConfiguration;
import org.sentrysoftware.metricshub.engine.connector.model.common.ResultContent;
import picocli.CommandLine;
import picocli.CommandLine.ArgGroup;
import picocli.CommandLine.Command;
import picocli.CommandLine.Model.CommandSpec;
import picocli.CommandLine.Option;
import picocli.CommandLine.ParameterException;
import picocli.CommandLine.Parameters;
import picocli.CommandLine.Spec;

@Data
@Command(name = "httpcli", description = "HTTP client for testing purposes.")
public class HttpCli implements IQuery, Callable<Integer> {

	@Parameters(index = "0", paramLabel = "HOSTNAME", description = "Hostname or IP address of the host to monitor")
	String hostname;

	@Spec
	CommandSpec spec;

	@Option(names = "--http-url", order = 1, paramLabel = "URL", description = "Url for HTTP request.")
	private String url;

	@Option(names = "--http-path", order = 2, paramLabel = "PATH", description = "PATH for HTTP request.")
	private String path;

	@Option(
		names = "--http-method",
		order = 3,
		paramLabel = "METHOD",
		description = "HTTP request type (GET|POST|PUT|DELETE)"
	)
	private String method;

	@Option(
		names = "--http-header",
		order = 4,
		paramLabel = "HEADER",
		split = ",",
		description = "Headers to be added to the HTTP request."
	)
	private Map<String, String> headers;

	@Option(
		names = "--http-header-file",
		order = 5,
		paramLabel = "HEADERFILE",
		description = "Path of the file containing header to be added to the HTTP request."
	)
	private String headerFile;

	@Option(names = "--http-body", order = 6, paramLabel = "BODY", description = "Body of the HTTP request.")
	private String body;

	@Option(
		names = "--http-body-file",
		order = 7,
		paramLabel = "BODYFILE",
		description = "Path of the file containing the HTTP request body."
	)
	private String bodyFile;

	@Option(
		names = "--http-authentication-token",
		order = 8,
		paramLabel = "TOKEN",
		description = " The authentication token for the HTTP request."
	)
	private String authenticationToken;

	@Option(
		names = "--http-result-content",
		order = 9,
		paramLabel = "RESULTCONTENT",
		description = "The type of content to retrieve from the HTTP response."
	)
	private String resultContent;

	@ArgGroup(exclusive = false, heading = "%n@|bold,underline HTTP Options|@:%n")
	HttpConfigCli httpConfigCli;

	@Option(names = { "-h", "-?", "--help" }, usageHelp = true, description = "Shows this help message and exits")
	boolean usageHelpRequested;

	@Option(names = "-v", order = 7, description = "Verbose mode (repeat the option to increase verbosity)")
	boolean[] verbose;

	static Set<String> httpMethods = Set.of("GET", "POST", "PUT", "DELETE");

	@Override
	public JsonNode getQuery() {
		final ObjectNode queryNode = JsonNodeFactory.instance.objectNode();

		if (method != null) {
			queryNode.set("method", new TextNode(method.toUpperCase()));
		}

		if (url != null) {
			queryNode.set("url", new TextNode(url));
		}

		if (path != null) {
			queryNode.set("path", new TextNode(path));
		}

		try {
			final String headerContent = getHeaderContent();
			if (headerContent != null) {
				queryNode.set("header", new TextNode(headerContent));
			}
		} catch (IOException e) {
			
		}

		try {
			final String bodyContent = getBodyContent();
			if (bodyContent != null) {
				queryNode.set("body", new TextNode(bodyContent));
			}
		} catch (IOException e) {
			
		}

		if (resultContent != null) {
			queryNode.set("resultContent", new TextNode(resultContent));
		}

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
	 * @param printWriter the PrintWriter to log any errors during file reading
	 * @return the body content as a string, or null if neither body nor body file is set.
	 * @throws IOException if an error occurs while reading the bodyFile.
	 */
	public String getBodyContent() throws IOException{
		if (body != null) {
			return body;
		} else {
			return Files.readString(Path.of(bodyFile), StandardCharsets.UTF_8);
		}
	}

	/**
	 * Retrieves the header content for the HTTP request.
	 * If headers are specified directly, they are formatted and returned.
	 * If a header file is specified, its contents are read and returned.
	 * 
	 * @param printWriter the PrintWriter to log any errors during file reading
	 * @return the header content as a string, or null if neither headers nor a header file is set.
	 * @throws IOException if an error occurs while reading the headerFile.
	 */
	public String getHeaderContent() throws IOException {
		StringBuilder header = new StringBuilder();

		if (headers != null) {
			headers.forEach((key, value) -> header.append(String.format("%s: %s%n", key, value)));
			return header.toString();
		} else {
			header.append(Files.readString(Path.of(headerFile), StandardCharsets.UTF_8));
			return header.toString();
		}
	}

	/**
	 * Validates the HTTP request configuration and parameters.
	 * Ensures required fields are set and checks for conflicts in headers and body configuration.
	 * 
	 * @throws ParameterException if validation fails due to missing or conflicting parameters.
	 */
	void validate() throws ParameterException {
		// Validating HTTP configuration
		if (httpConfigCli == null) {
			throw new ParameterException(spec.commandLine(), "HTTP/HTTPS protocol must be configured: --http, --https.");
		}

		// Validating HTTP methods
		if (method != null && !httpMethods.contains(method.toUpperCase())) {
			throw new ParameterException(spec.commandLine(), String.format("Unknown HTTP request method: %s.", method));
		}

		// Validating URL & Path
		if (url == null && path == null) {
			throw new ParameterException(
				spec.commandLine(),
				"At least one of the parameters must be specified: --http-url or --http-path."
			);
		}

		// Validating headers
		if (headers != null && headerFile != null) {
			throw new ParameterException(
				spec.commandLine(),
				"Conflict - Two headers have been configured: --http-header and --http-header-file."
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
				"Conflict - Two bodies have been configured: --http-body and --http-body-file."
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

		ResultContent.detect(resultContent);
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

		// Execute the command
		final int exitCode = cli.execute(args);

		// Cleanup Windows terminal settings
		AnsiConsole.systemUninstall();

		System.exit(exitCode);
	}

	@Override
	public Integer call() throws Exception {
		final PrintWriter printWriter = spec.commandLine().getOut();
		validate();
		CliExtensionManager
			.getExtensionManagerSingleton()
			.findExtensionByType("http")
			.ifPresent(extension -> {
				try {
					IConfiguration protocol = httpConfigCli.toProtocol(null, null);
					protocol.setHostname(hostname);
					extension.executeQuery(protocol, getQuery(), printWriter);
				} catch (Exception e) {
					printWriter.print("HTTP - Invalid configuration detected.\n");
					printWriter.flush();
					throw new IllegalStateException("Invalid configuration detected.", e);
				}
			});
		return CommandLine.ExitCode.OK;
	}
}
