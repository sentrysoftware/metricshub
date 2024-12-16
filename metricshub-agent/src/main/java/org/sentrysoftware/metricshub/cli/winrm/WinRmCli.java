package org.sentrysoftware.metricshub.cli.winrm;

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
import com.fasterxml.jackson.databind.node.ArrayNode;
import com.fasterxml.jackson.databind.node.IntNode;
import com.fasterxml.jackson.databind.node.JsonNodeFactory;
import com.fasterxml.jackson.databind.node.ObjectNode;
import com.fasterxml.jackson.databind.node.TextNode;
import java.io.PrintWriter;
import java.util.List;
import java.util.concurrent.Callable;
import lombok.Data;
import org.fusesource.jansi.Ansi;
import org.fusesource.jansi.AnsiConsole;
import org.sentrysoftware.metricshub.cli.service.CliExtensionManager;
import org.sentrysoftware.metricshub.cli.service.ConsoleService;
import org.sentrysoftware.metricshub.cli.service.MetricsHubCliService.CliPasswordReader;
import org.sentrysoftware.metricshub.cli.service.PrintExceptionMessageHandlerService;
import org.sentrysoftware.metricshub.engine.common.IQuery;
import org.sentrysoftware.metricshub.engine.configuration.IConfiguration;
import picocli.CommandLine;
import picocli.CommandLine.Model.CommandSpec;
import picocli.CommandLine.Option;
import picocli.CommandLine.ParameterException;
import picocli.CommandLine.Parameters;
import picocli.CommandLine.Spec;

/**
 * A command-line interface (CLI) for executing WinRm queries.
 * <p>
 * This class supports WinRm operations. It provides validation for configurations
 * and query parameters and integrates with the CLI extension framework to execute WinRm queries.
 * </p>
 *
 * Implements {@link IQuery} to generate WinRm-specific query JSON
 * and {@link Callable} to support execution via a command-line tool.
 */
@Data
public class WinRmCli implements IQuery, Callable<Integer> {

	/**
	 * Default timeout in seconds for a WinRM operation
	 */
	public static final int DEFAULT_TIMEOUT = 30;
	/**
	 * Default Http Port
	 */
	public static final Integer DEFAULT_HTTP_PORT = 5985;
	/**
	 * Default Https port
	 */
	public static final Integer DEFAULT_HTTPS_PORT = 5986;

	@Parameters(index = "0", paramLabel = "HOSTNAME", description = "Hostname or IP address of the host to monitor")
	String hostname;

	@Spec
	CommandSpec spec;

	@Option(
		names = "--winrm-transport",
		order = 1,
		paramLabel = "HTTP|HTTPS",
		defaultValue = "HTTP",
		description = "Transport protocol for WinRM (default: ${DEFAULT-VALUE})"
	)
	private String protocol;

	@Option(
		names = { "--winrm-username" },
		order = 2,
		paramLabel = "USER",
		description = "Username for WinRM authentication"
	)
	private String username;

	@Option(
		names = { "--winrm-password" },
		order = 3,
		paramLabel = "P4SSW0RD",
		description = "Password for the WinRM authentication",
		arity = "0..1",
		interactive = true
	)
	private char[] password;

	@Option(
		names = "--winrm-port",
		order = 4,
		paramLabel = "PORT",
		description = "Port for WinRM service (default: 5985 for HTTP, 5986 for HTTPS)"
	)
	private Integer port;

	@Option(
		names = "--winrm-timeout",
		order = 5,
		paramLabel = "TIMEOUT",
		defaultValue = "" + DEFAULT_TIMEOUT,
		description = "Timeout in seconds for WinRM operations (default: ${DEFAULT-VALUE} s)"
	)
	private String timeout;

	@Option(
		names = "--winrm-auth",
		description = "Comma-separated ordered list of authentication schemes." +
		" Possible values are NTLM and KERBEROS. By default, only NTLM is used",
		order = 6,
		paramLabel = "AUTH",
		split = ","
	)
	private List<String> authentications;

	@Option(
		names = "--winrm-query",
		required = true,
		order = 7,
		paramLabel = "QUERY",
		description = "WinRm query to execute"
	)
	private String query;

	@Option(
		names = { "--winrm-namespace" },
		order = 8,
		paramLabel = "NAMESPACE",
		description = "Forces a specific namespace for connectors that perform namespace auto-detection (advanced)"
	)
	private String namespace;

	@Option(
		names = { "-h", "-?", "--help" },
		order = 9,
		usageHelp = true,
		description = "Shows this help message and exits"
	)
	boolean usageHelpRequested;

	@Option(names = "-v", order = 10, description = "Verbose mode (repeat the option to increase verbosity)")
	boolean[] verbose;

	@Override
	public JsonNode getQuery() {
		final ObjectNode queryNode = JsonNodeFactory.instance.objectNode();
		queryNode.set("query", new TextNode(query));
		return queryNode;
	}

	/**
	 * Validates the current configuration.
	 *
	 * Ensures that required parameters are not blank and that passwords can be requested interactively if needed.
	 *
	 * @throws ParameterException if required parameters are blank
	 */
	void validate() {
		// Can we ask for passwords interactively?
		final boolean interactive = ConsoleService.hasConsole();

		// Password
		if (interactive) {
			tryInteractivePassword(System.console()::readPassword);
		}

		if (query.isBlank()) {
			throw new ParameterException(spec.commandLine(), "WinRm query must not be empty nor blank.");
		}

		if (namespace.isBlank()) {
			throw new ParameterException(spec.commandLine(), "WinRm namespace must not be empty nor blank.");
		}
	}

	/**
	 * Try to start the interactive mode to request and set WinRm password
	 *
	 * @param passwordReader password reader which displays the prompt text and wait for user's input
	 */
	void tryInteractivePassword(final CliPasswordReader<char[]> passwordReader) {
		if (username != null && password == null) {
			password = (passwordReader.read("%s password for WinRm: ", username));
		}
	}

	/**
	 * Entry point for the WinRm CLI application. Initializes necessary configurations,
	 * processes command line arguments, and executes the CLI.
	 *
	 * @param args The command line arguments passed to the application.
	 */
	public static void main(String[] args) {
		System.setProperty("log4j2.configurationFile", "log4j2-cli.xml");

		// Enable colors on Windows terminal
		AnsiConsole.systemInstall();

		final CommandLine cli = new CommandLine(new WinRmCli());

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
		validate();
		final PrintWriter printWriter = spec.commandLine().getOut();
		CliExtensionManager
			.getExtensionManagerSingleton()
			.findExtensionByType("winrm")
			.ifPresent(extension -> {
				try {
					final ObjectNode configurationNode = JsonNodeFactory.instance.objectNode();

					// Build configuration with necessary parameters
					configurationNode.set("protocol", new TextNode(protocol));
					configurationNode.set("username", new TextNode(username));
					configurationNode.set("password", new TextNode(String.valueOf(password)));
					configurationNode.set("port", new IntNode(getOrDeducePortNumber()));
					configurationNode.set("timeout", new TextNode(timeout));
					configurationNode.set("namespace", new TextNode(namespace));

					if (authentications != null) {
						final ArrayNode authenticationsNode = JsonNodeFactory.instance.arrayNode();
						authentications.stream().forEach(authenticationsNode::add);
						configurationNode.set("authentications", authenticationsNode);
					}

					IConfiguration configuration = extension.buildConfiguration(hostname, configurationNode, null);
					configuration.setHostname(hostname);

					displayQuery(printWriter);
					// Execute the query
					final String result = extension.executeQuery(configuration, getQuery(), printWriter);
					displayResult(printWriter, result);
				} catch (Exception e) {
					throw new IllegalStateException("Failed to execute WinRm query.\n", e);
				}
			});
		return CommandLine.ExitCode.OK;
	}

	protected int getOrDeducePortNumber() {
		if (port != null) {
			return port;
		} else if ("https".equals(protocol)) {
			return DEFAULT_HTTPS_PORT;
		}
		return DEFAULT_HTTP_PORT;
	}

	/**
	 * Prints query details.
	 *
	 * @param printWriter the output writer
	 */
	void displayQuery(PrintWriter printWriter) {
		printWriter.println("Executing WinRm request.");
		printWriter.println(Ansi.ansi().a("Query: ").fgBrightBlack().a(namespace).reset().toString());
		printWriter.println(Ansi.ansi().a("Namespace: ").fgBrightBlack().a(namespace).reset().toString());
		printWriter.flush();
	}

	/**
	 * Prints the query result.
	 *
	 * @param printWriter the output writer
	 * @param result      the query result
	 */
	void displayResult(PrintWriter printWriter, String result) {
		printWriter.println(Ansi.ansi().fgBlue().bold().a("Result: \n").reset().a(result).toString());
		printWriter.flush();
	}
}
