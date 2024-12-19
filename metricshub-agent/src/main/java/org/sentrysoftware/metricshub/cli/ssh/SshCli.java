package org.sentrysoftware.metricshub.cli.ssh;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.node.ArrayNode;
import com.fasterxml.jackson.databind.node.BooleanNode;
import com.fasterxml.jackson.databind.node.IntNode;
import com.fasterxml.jackson.databind.node.JsonNodeFactory;
import com.fasterxml.jackson.databind.node.ObjectNode;
import com.fasterxml.jackson.databind.node.TextNode;
import java.io.PrintWriter;
import java.util.Set;
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
 * A command-line interface (CLI) for executing command lines via SSH.
 * <p>
 * This class provides validation for configurations and query parameters
 * and integrates with the CLI extension framework to execute SSH queries.
 * </p>
 *
 * Implements {@link IQuery} to generate SSH specific query JSON
 * and {@link Callable} to support execution via a command-line tool.
 */
@Data
public class SshCli implements IQuery, Callable<Integer> {

	/**
	 * The identifier for the SSH protocol.
	 */
	public static final String PROTOCOL_IDENTIFIER = "ssh";
	/**
	 * Default timeout in seconds to execute an SSH operation
	 */
	public static final int DEFAULT_TIMEOUT = 30;

	/**
	 * Default SSH port number.
	 */
	public static final int DEFAULT_PORT = 22;

	@Parameters(index = "0", paramLabel = "HOSTNAME", description = "Hostname or IP address of the host to monitor")
	String hostname;

	@Spec
	CommandSpec spec;

	@Option(names = "--username", order = 1, paramLabel = "USER", description = "Username for SSH authentication")
	private String username;

	@Option(
		names = "--password",
		order = 2,
		paramLabel = "P4SSW0RD",
		description = "Password or SSH authentication",
		interactive = true,
		arity = "0..1"
	)
	private char[] password;

	@Option(
		names = "--publickey",
		order = 3,
		paramLabel = "PATH",
		description = "Path to the public key file for SSH authentication"
	)
	private String publicKeyFilePath;

	@Option(
		names = "--timeout",
		order = 4,
		paramLabel = "TIMEOUT",
		description = "Timeout in seconds for SSH operations (default: ${DEFAULT-VALUE} s)",
		defaultValue = "" + DEFAULT_TIMEOUT
	)
	private String timeout;

	@Option(
		names = "--port",
		order = 5,
		paramLabel = "PORT",
		defaultValue = "" + DEFAULT_PORT,
		description = "Port number for SSH connection (default: ${DEFAULT-VALUE})"
	)
	private Integer port;

	@Option(
		names = "--usesudo-commands",
		order = 6,
		paramLabel = "SUDOCOMMANDS",
		description = "List of commands that requires @|italic sudo|@",
		split = ","
	)
	private Set<String> useSudoCommands;

	@Option(
		names = "--sudo-command",
		order = 7,
		paramLabel = "SUDO",
		description = "@|italic sudo|@ command (default: ${DEFAULT-VALUE})",
		defaultValue = "sudo"
	)
	private String sudoCommand;

	@Option(
		names = "--command-line",
		required = true,
		order = 8,
		paramLabel = "COMMANDLINE",
		description = "Command Line to execute",
		defaultValue = "sudo"
	)
	private String commandLine;

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
		queryNode.set("commandLine", new TextNode(commandLine));
		return queryNode;
	}

	/**
	 * Validates the current configuration.
	 *
	 * Ensures that required parameters are correctly specified and that passwords can be requested interactively if needed.
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

		if (commandLine.isBlank()) {
			throw new ParameterException(spec.commandLine(), "SSH command line must not be empty nor blank.");
		}
	}

	/**
	 * Try to start the interactive mode to request and set SSH password
	 *
	 * @param passwordReader password reader which displays the prompt text and wait for user's input
	 */
	void tryInteractivePassword(final CliPasswordReader<char[]> passwordReader) {
		if (username != null && password == null) {
			password = (passwordReader.read("%s password for SSH: ", username));
		}
	}

	/**
	 * Entry point for the SSH CLI application. Initializes necessary configurations,
	 * processes command line arguments, and executes the CLI.
	 *
	 * @param args The command line arguments passed to the application.
	 */
	public static void main(String[] args) {
		System.setProperty("log4j2.configurationFile", "log4j2-cli.xml");

		// Enable colors on Windows terminal
		AnsiConsole.systemInstall();

		final CommandLine cli = new CommandLine(new SshCli());

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
			.findExtensionByType(PROTOCOL_IDENTIFIER)
			.ifPresent(extension -> {
				try {
					final ObjectNode configurationNode = JsonNodeFactory.instance.objectNode();

					if (username != null) {
						configurationNode.set("username", new TextNode(username));
					}

					if (password != null) {
						configurationNode.set("password", new TextNode(String.valueOf(password)));
					}

					if (publicKeyFilePath != null) {
						configurationNode.set("publicKey", new TextNode(publicKeyFilePath));
					}

					if (useSudoCommands != null) {
						final ArrayNode sudoCommands = JsonNodeFactory.instance.arrayNode();
						useSudoCommands.stream().forEach(sudoCommands::add);
						configurationNode.set("useSudoCommands", sudoCommands);
					}

					configurationNode.set("sudoCommand", new TextNode(sudoCommand));
					configurationNode.set("useSudo", BooleanNode.TRUE);
					configurationNode.set("timeout", new TextNode(timeout));
					configurationNode.set("port", new IntNode(getPort()));

					IConfiguration configuration = extension.buildConfiguration(PROTOCOL_IDENTIFIER, configurationNode, null);
					configuration.setHostname(hostname);

					displayQuery(printWriter);
					// Execute the query
					final String result = extension.executeQuery(configuration, getQuery(), printWriter);
					displayResult(printWriter, result);
				} catch (Exception e) {
					throw new IllegalStateException("Failed to execute command line through SSH.\n", e);
				}
			});
		return CommandLine.ExitCode.OK;
	}

	/**
	 * Prints query details.
	 *
	 * @param printWriter the output writer
	 */
	void displayQuery(PrintWriter printWriter) {
		printWriter.println("Executing command line through SSH.");
		printWriter.println(Ansi.ansi().a("Command line: ").fgBrightBlack().a(commandLine).reset().toString());
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
