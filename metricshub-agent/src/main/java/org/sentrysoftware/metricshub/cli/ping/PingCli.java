package org.sentrysoftware.metricshub.cli.ping;

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
import java.io.PrintWriter;
import java.net.UnknownHostException;
import java.util.concurrent.Callable;
import lombok.Data;
import lombok.extern.slf4j.Slf4j;
import org.fusesource.jansi.Ansi;
import org.fusesource.jansi.AnsiConsole;
import org.sentrysoftware.metricshub.cli.service.CliExtensionManager;
import org.sentrysoftware.metricshub.cli.service.PrintExceptionMessageHandlerService;
import org.sentrysoftware.metricshub.engine.common.IQuery;
import org.sentrysoftware.metricshub.engine.common.helpers.NetworkHelper;
import org.sentrysoftware.metricshub.engine.common.helpers.StringHelper;
import org.sentrysoftware.metricshub.engine.configuration.IConfiguration;
import picocli.CommandLine;
import picocli.CommandLine.Model.CommandSpec;
import picocli.CommandLine.Option;
import picocli.CommandLine.Parameters;
import picocli.CommandLine.Spec;

/**
 * A command-line interface (CLI) for executing ICMP ping requests.
 */
@Data
@Slf4j
public class PingCli implements IQuery, Callable<Integer> {

	/**
	 * The identifier for the ICMP Ping protocol.
	 */
	public static final String PROTOCOL_IDENTIFIER = "ping";
	/**
	 * Default timeout in seconds to execute an ICMP ping requests
	 */
	public static final long DEFAULT_TIMEOUT = 5L;

	@Parameters(index = "0", paramLabel = "HOSTNAME", description = "Hostname or IP address of the host to monitor")
	String hostname;

	@Spec
	CommandSpec spec;

	@Option(
		names = "--timeout",
		order = 1,
		paramLabel = "TIMEOUT",
		description = "Timeout in seconds for ICMP Ping operations (default: ${DEFAULT-VALUE} s)",
		defaultValue = "" + DEFAULT_TIMEOUT
	)
	private String timeout;

	@Override
	public JsonNode getQuery() {
		return null;
	}

	/**
	 * Entry point for the Ping CLI application. Initializes necessary configurations,
	 * processes command line arguments, and executes the CLI.
	 *
	 * @param args The command line arguments passed to the application.
	 */
	public static void main(String[] args) {
		System.setProperty("log4j2.configurationFile", "log4j2-cli.xml");

		// Enable colors on Windows terminal
		AnsiConsole.systemInstall();

		final CommandLine cli = new CommandLine(new PingCli());

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

		CliExtensionManager
			.getExtensionManagerSingleton()
			.findExtensionByType(PROTOCOL_IDENTIFIER)
			.ifPresent(extension -> {
				try {
					final ObjectNode configurationNode = JsonNodeFactory.instance.objectNode();
					configurationNode.set("timeout", new TextNode(timeout));

					IConfiguration configuration = extension.buildConfiguration(PROTOCOL_IDENTIFIER, configurationNode, null);
					configuration.setHostname(hostname);

					displayQuery(printWriter);
					// Execute the query
					final long startTime = System.currentTimeMillis();
					final String result = extension.executeQuery(configuration, getQuery(), printWriter);
					final long responseTime = (System.currentTimeMillis() - startTime);
					displayResult(printWriter, result, responseTime);
				} catch (Exception e) {
					displayResult(printWriter, Boolean.toString(false), 0);
				}
			});
		return CommandLine.ExitCode.OK;
	}

	/**
	 * Prints query details.
	 *
	 * @param printWriter the output writer
	 */
	void displayQuery(final PrintWriter printWriter) {
		String fqdn = hostname;
		String ipAddress = null;
		printWriter.println("Executing an ICMP ping request:");

		try {
			fqdn = NetworkHelper.getFqdn(hostname);
		} catch (Exception e) {
			log.debug("Unable to retrieve FQDN for {}.\n", hostname);
		}

		try {
			ipAddress = NetworkHelper.resolveDns(hostname);
		} catch (Exception e) {
			log.debug("Unable to retrieve IP Address for {}.\n", hostname);
		}

		printWriter.println(
			Ansi.ansi().a("Hostname: ").fgBrightBlack().a(fqdn != null ? fqdn : hostname).reset().toString()
		);

		if (ipAddress != null) {
			printWriter.println(
				Ansi.ansi().a("IP Address: ").fgBrightBlack().a(ipAddress != null ? ipAddress : "N/A").reset().toString()
			);
		}
		printWriter.flush();
	}

	/**
	 * Prints the query result.
	 *
	 * @param printWriter the output writer
	 * @param result      the query result
	 */
	void displayResult(final PrintWriter printWriter, final String result, final long responseTime) {
		if (Boolean.TRUE.toString().equals(result)) {
			printWriter.println(Ansi.ansi().bold().a("Status: ").reset().fgGreen().a("Reachable").reset().toString());
			printWriter.println(Ansi.ansi().a("Response Time: ").fgBrightBlack().a(responseTime).a("ms").reset().toString());
		} else {
			printWriter.println(Ansi.ansi().bold().a("Status: ").reset().fgRed().a("Unreachable").toString());
		}
		printWriter.flush();
	}
}
