package org.sentrysoftware.metricshub.cli.wbem;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.node.IntNode;
import com.fasterxml.jackson.databind.node.JsonNodeFactory;
import com.fasterxml.jackson.databind.node.ObjectNode;
import com.fasterxml.jackson.databind.node.TextNode;
import java.io.PrintWriter;
import java.util.concurrent.Callable;
import lombok.Data;
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

/**
 * A command-line interface (CLI) for executing Wbem queries.
 * <p>
 * This class supports Wbem operations. It provides validation for configurations
 * and query parameters and integrates with the CLI extension framework to execute Wbem queries.
 * </p>
 *
 * Implements {@link IQuery} to generate Wbem-specific query JSON
 * and {@link Callable} to support execution via a command-line tool.
 */
@Data
public class WbemCli implements IQuery, Callable<Integer> {

	/**
	 * Default timeout in seconds for a Wbem operation
	 */
	public static final int DEFAULT_TIMEOUT = 30;
	/**
	 * Default HTTPS port number
	 */
	public static final int DEFAULT_HTTPS_PORT_NUMBER = 5989;
	/**
	 * Default HTTP port number
	 */
	public static final int DEFAULT_HTTP_PORT_NUMBER = 5988;

	@Parameters(index = "0", paramLabel = "HOSTNAME", description = "Hostname or IP address of the host to monitor")
	String hostname;

	@Spec
	CommandSpec spec;

	@Option(
		names = "--wbem-query",
		required = true,
		order = 1,
		paramLabel = "QUERY",
		description = "WBEM query to execute"
	)
	private String query;

	@Option(
		names = "--wbem-transport",
		order = 2,
		defaultValue = "HTTPS",
		paramLabel = "HTTP|HTTPS",
		description = "Transport protocol for WBEM (default: ${DEFAULT-VALUE})"
	)
	private String protocol;

	@Option(
		names = "--wbem-port",
		order = 3,
		paramLabel = "PORT",
		description = "Port of the WBEM server (default: 5988 for HTTP, 5989 for HTTPS)"
	)
	private Integer port;

	@Option(names = "--wbem-username", order = 4, paramLabel = "USER", description = "Username for WBEM authentication")
	String username;

	@Option(
		names = "--wbem-password",
		order = 5,
		paramLabel = "P4SSW0RD",
		description = "Password for WBEM authentication",
		interactive = true,
		arity = "0..1"
	)
	private char[] password;

	@Option(
		names = "--wbem-timeout",
		order = 6,
		defaultValue = "" + DEFAULT_TIMEOUT,
		paramLabel = "TIMEOUT",
		description = "Timeout in seconds for WBEM operations (default: ${DEFAULT-VALUE} s)"
	)
	private String timeout;

	@Option(
		names = "--wbem-namespace",
		required = true,
		order = 7,
		paramLabel = "NAMESPACE",
		description = "Forces a specific namespace for connectors that perform namespace auto-detection (advanced)"
	)
	private String namespace;

	@Option(
		names = "--wbem-vcenter",
		order = 8,
		paramLabel = "VCENTER",
		description = "VCenter hostname providing the authentication ticket (if applicable)"
	)
	private String vcenter;

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
			throw new ParameterException(spec.commandLine(), "Wbem query must not be blank.");
		}

		if (namespace.isBlank()) {
			throw new ParameterException(spec.commandLine(), "Wbem namespace must not be blank.");
		}
	}

	/**
	 * Try to start the interactive mode to request and set Wbem password
	 *
	 * @param passwordReader password reader which displays the prompt text and wait for user's input
	 */
	void tryInteractivePassword(final CliPasswordReader<char[]> passwordReader) {
		if (username != null && password == null) {
			password = (passwordReader.read("%s password for Wbem: ", username));
		}
	}

	/**
	 * Get or deduce the port number based on the transport protocol.
	 *
	 * @return the port number as an integer
	 */
	protected int getOrDeducePortNumber() {
		if (port != null) {
			return port;
		} else if ("https".equalsIgnoreCase(protocol)) {
			return DEFAULT_HTTPS_PORT_NUMBER;
		}
		return DEFAULT_HTTP_PORT_NUMBER;
	}

	/**
	 * Entry point for the Wbem CLI application. Initializes necessary configurations,
	 * processes command line arguments, and executes the CLI.
	 *
	 * @param args The command line arguments passed to the application.
	 */
	public static void main(String[] args) {
		System.setProperty("log4j2.configurationFile", "log4j2-cli.xml");

		// Enable colors on Windows terminal
		AnsiConsole.systemInstall();

		final CommandLine cli = new CommandLine(new WbemCli());

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
			.findExtensionByType("wbem")
			.ifPresent(extension -> {
				try {
					final ObjectNode configurationNode = JsonNodeFactory.instance.objectNode();

					// Build configuration with necessary parameters
					configurationNode.set("username", new TextNode(username));
					configurationNode.set("password", new TextNode(String.valueOf(password)));
					configurationNode.set("timeout", new TextNode(timeout));
					configurationNode.set("namespace", new TextNode(namespace));
					configurationNode.set("vcenter", new TextNode(vcenter));
					configurationNode.set("protocol", new TextNode(protocol));
					configurationNode.set("port", new IntNode(getOrDeducePortNumber()));
					IConfiguration configuration = extension.buildConfiguration(hostname, configurationNode, null);
					configuration.setHostname(hostname);

					// Execute the query
					extension.executeQuery(configuration, getQuery(), printWriter);
				} catch (Exception e) {
					printWriter.println("Wbem - Invalid configuration detected.\n");
					printWriter.flush();
					throw new IllegalStateException("Invalid configuration detected.", e);
				}
			});
		return CommandLine.ExitCode.OK;
	}
}
