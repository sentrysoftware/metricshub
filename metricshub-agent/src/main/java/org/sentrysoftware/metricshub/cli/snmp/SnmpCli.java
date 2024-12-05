package org.sentrysoftware.metricshub.cli.snmp;

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

import static org.sentrysoftware.metricshub.cli.service.protocol.SnmpConfigCli.DEFAULT_TIMEOUT;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.node.JsonNodeFactory;
import com.fasterxml.jackson.databind.node.ObjectNode;
import com.fasterxml.jackson.databind.node.TextNode;
import java.io.PrintWriter;
import java.util.Objects;
import java.util.concurrent.Callable;
import java.util.stream.Stream;
import lombok.Data;
import org.fusesource.jansi.AnsiConsole;
import org.sentrysoftware.metricshub.cli.service.CliExtensionManager;
import org.sentrysoftware.metricshub.cli.service.PrintExceptionMessageHandlerService;
import org.sentrysoftware.metricshub.cli.service.protocol.SnmpConfigCli;
import org.sentrysoftware.metricshub.engine.common.IQuery;
import org.sentrysoftware.metricshub.engine.configuration.IConfiguration;
import picocli.CommandLine;
import picocli.CommandLine.ArgGroup;
import picocli.CommandLine.Model.CommandSpec;
import picocli.CommandLine.Option;
import picocli.CommandLine.ParameterException;
import picocli.CommandLine.Parameters;
import picocli.CommandLine.Spec;

/**
 * A command-line interface (CLI) for executing SNMP queries.
 * <p>
 * This class supports SNMP operations such as Get, Get Next, and Walk.
 * It provides validation for configurations and query parameters
 * and integrates with the CLI extension framework to execute SNMP queries.
 * </p>
 *
 * Implements {@link IQuery} to generate SNMP-specific query JSON
 * and {@link Callable} to support execution via a command-line tool.
 */
@Data
public class SnmpCli implements IQuery, Callable<Integer> {

	@Parameters(index = "0", paramLabel = "HOSTNAME", description = "Hostname or IP address of the host to monitor")
	String hostname;

	@Spec
	CommandSpec spec;

	@ArgGroup(exclusive = false, heading = "%n@|bold,underline SNMP Options|@:%n")
	SnmpConfigCli snmpConfigCli;

	@Option(names = "--snmp-get", order = 1, paramLabel = "OID", description = "SNMP Get request")
	String get;

	@Option(names = "--snmp-getnext", order = 2, paramLabel = "OID", description = "SNMP Get Next request")
	String getNext;

	@Option(names = "--snmp-walk", order = 3, paramLabel = "OID", description = "SNMP Walk request")
	String walk;

	@Option(
		names = { "-h", "-?", "--help" },
		order = 4,
		usageHelp = true,
		description = "Shows this help message and exits"
	)
	boolean usageHelpRequested;

	@Option(names = "-v", order = 5, description = "Verbose mode (repeat the option to increase verbosity)")
	boolean[] verbose;

	@Override
	public JsonNode getQuery() {
		final ObjectNode queryNode = JsonNodeFactory.instance.objectNode();
		String action;
		String oid;

		if (get != null) {
			action = "get";
			oid = get;
		} else if (getNext != null) {
			action = "getNext";
			oid = getNext;
		} else {
			action = "walk";
			oid = walk;
		}

		queryNode.set("action", new TextNode(action));
		queryNode.set("oid", new TextNode(oid));

		return queryNode;
	}

	/**
	 * Validates SNMP configuration and ensures exactly one query type (--snmp-get, --snmp-getnext, or --snmp-walk) is specified.
	 *
	 * @throws ParameterException if SNMP is not configured, no query is specified, or multiple queries are specified.
	 */
	void validate() throws ParameterException {
		Stream
			.of(get, getNext, walk)
			.filter(Objects::nonNull)
			.reduce((a, b) -> {
				throw new ParameterException(
					spec.commandLine(),
					"Only one SNMP query can be specified at a time: --snmp-get, --snmp-getnext, --snmp-walk."
				);
			})
			.orElseThrow(() ->
				new ParameterException(
					spec.commandLine(),
					"At least one SNMP query must be specified: --snmp-get, --snmp-getnext, --snmp-walk."
				)
			);
	}

	/**
	 * Builds the default SNMP configuration for the {@code SnmpConfigCli} object.
	 */
	void buildDefaultConfiguration() {
		snmpConfigCli = new SnmpConfigCli();
		snmpConfigCli.setSnmpVersion("v2c");
		snmpConfigCli.setCommunity("public".toCharArray());
		snmpConfigCli.setPort(161);
		snmpConfigCli.setTimeout(String.valueOf(DEFAULT_TIMEOUT));
	}

	/**
	 * Entry point for the SNMP CLI application. Initializes necessary configurations,
	 * processes command line arguments, and executes the CLI.
	 *
	 * @param args The command line arguments passed to the application.
	 */
	public static void main(String[] args) {
		System.setProperty("log4j2.configurationFile", "log4j2-cli.xml");

		// Enable colors on Windows terminal
		AnsiConsole.systemInstall();

		final CommandLine cli = new CommandLine(new SnmpCli());

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
			.findExtensionByType("snmp")
			.ifPresent(extension -> {
				try {
					if (snmpConfigCli == null) {
						buildDefaultConfiguration();
					}
					IConfiguration protocol = snmpConfigCli.toConfiguration(null, null);
					protocol.setHostname(hostname);
					extension.executeQuery(protocol, getQuery(), printWriter);
				} catch (Exception e) {
					printWriter.print("Invalid configuration detected");
					printWriter.flush();
					throw new IllegalStateException("Invalid configuration detected.", e);
				}
			});
		return CommandLine.ExitCode.OK;
	}
}
