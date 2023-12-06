package com.sentrysoftware.metricshub.cli.service;

import com.sentrysoftware.metricshub.agent.helper.ConfigHelper;
import com.sentrysoftware.metricshub.cli.service.converter.DeviceKindConverter;
import com.sentrysoftware.metricshub.cli.service.protocol.HttpConfigCli;
import com.sentrysoftware.metricshub.cli.service.protocol.IpmiConfigCli;
import com.sentrysoftware.metricshub.cli.service.protocol.SnmpConfigCli;
import com.sentrysoftware.metricshub.cli.service.protocol.SshConfigCli;
import com.sentrysoftware.metricshub.cli.service.protocol.WbemConfigCli;
import com.sentrysoftware.metricshub.cli.service.protocol.WinRmConfigCli;
import com.sentrysoftware.metricshub.cli.service.protocol.WmiConfigCli;
import com.sentrysoftware.metricshub.engine.common.helpers.KnownMonitorType;
import com.sentrysoftware.metricshub.engine.configuration.HostConfiguration;
import com.sentrysoftware.metricshub.engine.configuration.IConfiguration;
import com.sentrysoftware.metricshub.engine.connector.model.Connector;
import com.sentrysoftware.metricshub.engine.connector.model.ConnectorStore;
import com.sentrysoftware.metricshub.engine.connector.model.common.DeviceKind;
import com.sentrysoftware.metricshub.engine.matsya.MatsyaClientsExecutor;
import com.sentrysoftware.metricshub.engine.strategy.collect.CollectStrategy;
import com.sentrysoftware.metricshub.engine.strategy.collect.PostCollectStrategy;
import com.sentrysoftware.metricshub.engine.strategy.collect.PrepareCollectStrategy;
import com.sentrysoftware.metricshub.engine.strategy.detection.DetectionStrategy;
import com.sentrysoftware.metricshub.engine.strategy.discovery.DiscoveryStrategy;
import com.sentrysoftware.metricshub.engine.strategy.discovery.PostDiscoveryStrategy;
import com.sentrysoftware.metricshub.engine.strategy.simple.SimpleStrategy;
import com.sentrysoftware.metricshub.engine.telemetry.Monitor;
import com.sentrysoftware.metricshub.engine.telemetry.TelemetryManager;
import com.sentrysoftware.metricshub.hardware.strategy.HardwareStrategy;
import java.io.PrintWriter;
import java.util.Collection;
import java.util.Map;
import java.util.Objects;
import java.util.Set;
import java.util.concurrent.Callable;
import java.util.function.Function;
import java.util.stream.Collectors;
import java.util.stream.Stream;
import lombok.Data;
import org.apache.logging.log4j.Level;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.ThreadContext;
import org.apache.logging.log4j.core.LoggerContext;
import org.apache.logging.log4j.core.config.Configuration;
import org.apache.logging.log4j.core.config.LoggerConfig;
import org.fusesource.jansi.Ansi;
import org.fusesource.jansi.Ansi.Attribute;
import picocli.CommandLine;
import picocli.CommandLine.ArgGroup;
import picocli.CommandLine.Command;
import picocli.CommandLine.Model.CommandSpec;
import picocli.CommandLine.Option;
import picocli.CommandLine.ParameterException;
import picocli.CommandLine.Parameters;
import picocli.CommandLine.Spec;

//CHECKSTYLE:OFF
@Command(
	name = "metricshub",
	sortOptions = false,
	usageHelpAutoWidth = true,
	versionProvider = VersionService.class,
	headerHeading = "%n",
	header = "Gather metrics from the specified host.",
	synopsisHeading = "%n@|bold,underline Usage|@:%n%n",
	descriptionHeading = "%n@|bold,underline Description|@:%n%n",
	description = "This tool is the CLI version of the @|italic MetricsHub|@ engine. " +
	"MetricsHub monitors diverse technologies, encompassing applications, servers, and devices, particularly those without readily available monitoring solutions.%n%n" +
	"It natively leverages various system management protocols to discover the hardware components of a system " +
	"and report their operational status.%n%n" +
	"Additionally, MetricsHub measures the power consumption of the system, or makes an estimation if no power sensor is detected.",
	parameterListHeading = "%n@|bold,underline Parameters|@:%n",
	optionListHeading = "%n@|bold,underline Options|@:%n",
	customSynopsis = {
		"@|bold ${ROOT-COMMAND-NAME}|@ " +
		"@|yellow HOSTNAME|@ " +
		"@|yellow -t|@=@|italic TYPE|@ " +
		"<@|yellow --http|@|@|yellow --https|@|@|yellow --ipmi|@|@|yellow --snmp|@=@|italic VERSION|@|@|yellow --ssh|@|@|yellow --wbem|@|@|yellow --wmi|@|@|yellow --winrm|@> " +
		"[@|yellow -u|@=@|italic USER|@ [@|yellow -p|@=@|italic P4SSW0RD|@]] [OPTIONS]..."
	}
)
//CHECKSTYLE:ON
@Data
public class MetricsHubCliService implements Callable<Integer> {

	@Spec
	CommandSpec spec;

	@Option(names = { "-V", "--version" }, versionHelp = true, description = "Prints version information and exits")
	boolean versionInfoRequested;

	@Option(names = { "-h", "-?", "--help" }, usageHelp = true, description = "Shows this help message and exits")
	boolean usageHelpRequested;

	@Parameters(index = "0", paramLabel = "HOSTNAME", description = "Hostname or IP address of the host to monitor")
	String hostname;

	@Option(
		names = { "-t", "--type" },
		order = 1,
		required = true,
		paramLabel = "TYPE",
		description = "Reports the type of the host to monitor (lin, linux, win, windows, mgmt, management, storage, network, aix, hpux, solaris, tru64, vms)",
		converter = DeviceKindConverter.class
	)
	DeviceKind deviceType;

	@ArgGroup(exclusive = false, heading = "%n@|bold,underline IPMI Options|@:%n")
	IpmiConfigCli ipmiConfigCli;

	@ArgGroup(exclusive = false, heading = "%n@|bold,underline SSH Options|@:%n")
	SshConfigCli sshConfigCli;

	@ArgGroup(exclusive = false, heading = "%n@|bold,underline SNMP Options|@:%n")
	SnmpConfigCli snmpConfigCli;

	@ArgGroup(exclusive = false, heading = "%n@|bold,underline HTTP Options|@:%n")
	HttpConfigCli httpConfigCli;

	@ArgGroup(exclusive = false, heading = "%n@|bold,underline WBEM Options|@:%n")
	WbemConfigCli wbemConfigCli;

	@ArgGroup(exclusive = false, heading = "%n@|bold,underline WMI Options|@:%n")
	WmiConfigCli wmiConfigCli;

	@ArgGroup(exclusive = false, heading = "%n@|bold,underline WinRM Options|@:%n")
	WinRmConfigCli winRmConfigCli;

	@Option(names = { "-u", "--username" }, order = 2, paramLabel = "USER", description = "Username for authentication")
	String username;

	@Option(
		names = { "-p", "--password" },
		order = 3,
		paramLabel = "P4SSW0RD",
		description = "Associated password",
		arity = "0..1",
		interactive = true
	)
	char[] password;

	@Option(
		names = { "-f", "--force" },
		order = 4,
		split = ",",
		paramLabel = "CONNECTOR",
		description = "Forces the selected hardware connectors to connect to the host (use @|bold ${ROOT-COMMAND-NAME} -l|@ to get the list of connectors)"
	)
	Set<String> connectors;

	@Option(
		names = { "-x", "--exclude" },
		order = 5,
		split = ",",
		paramLabel = "CONNECTOR",
		description = "Excludes connectors from the automatic detection process (use @|bold ${ROOT-COMMAND-NAME} -l|@ to get the list of connectors)"
	)
	Set<String> excludedConnectors;

	@Option(
		names = { "-s", "--sequential" },
		order = 6,
		defaultValue = "false",
		description = "Forces all network calls to be executed in sequential order. (default: ${DEFAULT-VALUE})",
		help = true
	)
	boolean sequential;

	@Option(names = "-v", order = 7, description = "Verbose mode (repeat the option to increase verbosity)")
	boolean[] verbose;

	@Option(
		names = { "-l", "--list" },
		help = true,
		order = 7,
		description = "Lists all connectors bundled in the engine that can be selected or excluded"
	)
	boolean listConnectors;

	@Option(
		names = { "-i", "--iterations" },
		help = true,
		order = 8,
		defaultValue = "1",
		description = "Executes the collect strategies N times, where N is the number of iterations"
	)
	int iterations;

	@Option(
		names = { "-si", "--sleep-iteration" },
		help = true,
		order = 9,
		defaultValue = "5",
		description = "Adds a sleep period in seconds between collect iterations"
	)
	long sleepIteration;

	@Override
	public Integer call() throws Exception {
		final ConnectorStore connectorStore = new ConnectorStore(ConfigHelper.getSubDirectory("connectors", false));

		// First, process special "list" option
		if (listConnectors) {
			return listAllConnectors(connectorStore, spec.commandLine().getOut());
		}

		// Validate inputs
		validate(connectorStore);

		// Setup Log4j
		setLogLevel();

		final HostConfiguration hostConfiguration = HostConfiguration
			.builder()
			.hostId(hostname)
			.hostname(hostname)
			.hostType(deviceType)
			.sequential(sequential)
			.build();

		// Connectors
		if (connectors != null) {
			hostConfiguration.setSelectedConnectors(connectors);
		}
		if (excludedConnectors != null) {
			hostConfiguration.setExcludedConnectors(excludedConnectors);
		}

		// Set the configurations
		final Map<Class<? extends IConfiguration>, IConfiguration> configurations = buildConfigurations();
		hostConfiguration.setConfigurations(configurations);

		// Create the TelemetryManager using the connector store and the host configuration created above.
		final TelemetryManager telemetryManager = TelemetryManager
			.builder()
			.connectorStore(connectorStore)
			.hostConfiguration(hostConfiguration)
			.build();

		// Instantiate a new MatsyaClientsExecutor
		final MatsyaClientsExecutor matsyaClientsExecutor = new MatsyaClientsExecutor(telemetryManager);

		final long discoveryTime = System.currentTimeMillis();

		final PrintWriter printWriter = spec.commandLine().getOut();

		// Detection
		if (ConsoleService.hasConsole()) {
			String protocolDisplay = configurations
				.values()
				.stream()
				.map(proto -> Ansi.ansi().bold().a(proto.toString()).boldOff().toString())
				.collect(Collectors.joining(", "));
			printWriter.print("Performing detection on ");
			printWriter.print(Ansi.ansi().bold().a(hostname).boldOff().toString());
			printWriter.print(" using ");
			printWriter.print(protocolDisplay);
			printWriter.println("...");
			printWriter.flush();
		}
		telemetryManager.run(new DetectionStrategy(telemetryManager, discoveryTime, matsyaClientsExecutor));

		// Discovery
		if (ConsoleService.hasConsole()) {
			final Map<String, Monitor> connectorMonitors = telemetryManager.findMonitorsByType(
				KnownMonitorType.CONNECTOR.getKey()
			);

			if (connectorMonitors == null || connectorMonitors.isEmpty()) {
				printWriter.print(Ansi.ansi().fgBrightRed().a("No connector detected. Stopping.").reset().toString());
				printWriter.flush();
				return CommandLine.ExitCode.SOFTWARE;
			}

			int connectorCount = telemetryManager.findMonitorsByType(KnownMonitorType.CONNECTOR.getKey()).size();
			printWriter.print("Performing discovery with ");
			printWriter.print(Ansi.ansi().bold().a(connectorCount).boldOff().toString());
			printWriter.println(connectorCount > 1 ? " connectors..." : " connector...");
			printWriter.flush();
		}
		telemetryManager.run(
			new DiscoveryStrategy(telemetryManager, discoveryTime, matsyaClientsExecutor),
			new SimpleStrategy(telemetryManager, discoveryTime, matsyaClientsExecutor),
			new PostDiscoveryStrategy(telemetryManager, discoveryTime, matsyaClientsExecutor)
		);

		// Check whether iterations is greater than 0. If it's not the case, throw a ParameterException
		validateIterations(iterations);

		// Perform the collect operation "iterations" times
		for (int i = 0; i < iterations; i++) {
			// Collect
			if (ConsoleService.hasConsole()) {
				long monitorCount = telemetryManager
					.getMonitors()
					.values()
					.stream()
					.map(Map::values)
					.mapToLong(Collection::size)
					.sum();
				printWriter.print("Performing collect on ");
				printWriter.print(Ansi.ansi().bold().a(monitorCount).boldOff().toString());
				printWriter.println(monitorCount > 1 ? " monitors..." : " monitor...");
				printWriter.flush();
			}
			final long collectTime = System.currentTimeMillis();
			// One more, run only prepare, collect simple and post strategies
			telemetryManager.run(
				new PrepareCollectStrategy(telemetryManager, collectTime, matsyaClientsExecutor),
				new CollectStrategy(telemetryManager, collectTime, matsyaClientsExecutor),
				new SimpleStrategy(telemetryManager, collectTime, matsyaClientsExecutor),
				new PostCollectStrategy(telemetryManager, collectTime, matsyaClientsExecutor)
			);

			// Run the hardware strategy
			telemetryManager.run(new HardwareStrategy(telemetryManager, collectTime));

			// If iterations > 1, add a sleep time between iterations
			if (i != iterations - 1 && sleepIteration > 0) {
				printWriter.println(String.format("Pausing for %d seconds before the next iteration...", sleepIteration));
				Thread.sleep(sleepIteration * 1000);
			}
		}

		// And now the result
		if (ConsoleService.hasConsole()) {
			printWriter.print("\n");
		}

		// Print the result
		new PrettyPrinterService(telemetryManager, printWriter).print();

		return CommandLine.ExitCode.OK;
	}

	/**
	 *  Checks that iterations is greater than 0. Otherwise, throws a ParameterException
	 * @param iterations the number of collect iterations
	 */
	private void validateIterations(int iterations) {
		if (iterations <= 0) {
			throw new ParameterException(spec.commandLine(), "Number of iterations must be greater than 0.");
		}
	}

	/**
	 * @return A {@link Map} associating the input protocol type to its input credentials.
	 */
	private Map<Class<? extends IConfiguration>, IConfiguration> buildConfigurations() {
		return Stream
			.of(ipmiConfigCli, snmpConfigCli, sshConfigCli, httpConfigCli, wmiConfigCli, winRmConfigCli, wbemConfigCli)
			.filter(Objects::nonNull)
			.map(protocolConfig -> protocolConfig.toProtocol(username, password))
			.collect(Collectors.toMap(IConfiguration::getClass, Function.identity()));
	}

	/**
	 * Validate the specified arguments, and ask for passwords if needed.
	 *
	 * @param connectorStore Wraps all the connectors
	 * @throws ParameterException in case of invalid parameter
	 */
	private void validate(final ConnectorStore connectorStore) {
		// Can we ask for passwords interactively?
		final boolean interactive = ConsoleService.hasConsole();

		// Passwords
		if (interactive) {
			tryInteractivePasswords(System.console()::readPassword);
		}

		// No protocol at all?
		final boolean protocolsNotConfigured = Stream
			.of(ipmiConfigCli, snmpConfigCli, sshConfigCli, httpConfigCli, wmiConfigCli, winRmConfigCli, wbemConfigCli)
			.allMatch(Objects::isNull);

		if (protocolsNotConfigured) {
			throw new ParameterException(
				spec.commandLine(),
				"At least one protocol must be specified: --http[s], --ipmi, --snmp, --ssh, --wbem, --wmi, --winrm."
			);
		}

		// Connectors
		final Map<String, Connector> allConnectors = connectorStore.getStore();
		Stream<String> connectorsToCheck = connectors != null ? connectors.stream() : Stream.empty();
		if (excludedConnectors != null) {
			connectorsToCheck = Stream.concat(connectorsToCheck, excludedConnectors.stream());
		}
		String invalidConnectors = connectorsToCheck
			.filter(connectorName -> !allConnectors.containsKey(connectorName))
			.collect(Collectors.joining(", "));
		if (!invalidConnectors.isBlank()) {
			throw new ParameterException(spec.commandLine(), "Unknown connector: " + invalidConnectors);
		}
	}

	/**
	 * Set Log4j logging level according to the verbose flags
	 */
	void setLogLevel() {
		// Disable ANSI in the logging if we don't have a console
		ThreadContext.put("disableAnsi", Boolean.toString(!ConsoleService.hasConsole()));

		if (verbose != null) {
			Level logLevel;

			switch (verbose.length) {
				case 0:
					logLevel = Level.ERROR;
					break;
				case 1:
					logLevel = Level.WARN;
					break;
				case 2:
					logLevel = Level.INFO;
					break;
				case 3:
					logLevel = Level.DEBUG;
					break;
				default:
					logLevel = Level.ALL;
			}

			// Update the Log level at the root level
			LoggerContext loggerContext = (LoggerContext) LogManager.getContext(false);
			Configuration config = loggerContext.getConfiguration();
			LoggerConfig sentryLoggerConfig = config.getLoggerConfig("com.sentrysoftware");
			sentryLoggerConfig.setLevel(logLevel);
			loggerContext.updateLoggers();
		}
	}

	/**
	 * Try to start the interactive mode to request and set protocol passwords
	 *
	 * @param passwordReader password reader which displays the prompt text and wait for user's input
	 */
	void tryInteractivePasswords(final CliPasswordReader<char[]> passwordReader) {
		tryInteractiveGlobalPassword(passwordReader);

		tryInteractiveIpmiPassword(passwordReader);

		tryInteractiveSshPassword(passwordReader);

		tryInteractiveSnmpPassword(passwordReader);

		tryInteractiveHttpPassword(passwordReader);

		tryInteractiveWmiPassword(passwordReader);

		tryInteractiveWbemPassword(passwordReader);

		tryInteractiveWinRmPassword(passwordReader);
	}

	/**
	 * Try to start the interactive mode to request and set global password
	 *
	 * @param passwordReader password reader which displays the prompt text and wait for user's input
	 */
	void tryInteractiveGlobalPassword(final CliPasswordReader<char[]> passwordReader) {
		if (username != null && password == null) {
			password = passwordReader.read("%s password: ", username);
		}
	}

	/**
	 * Try to start the interactive mode to request and set SNMP password
	 *
	 * @param passwordReader password reader which displays the prompt text and wait for user's input
	 */
	void tryInteractiveSnmpPassword(final CliPasswordReader<char[]> passwordReader) {
		if (snmpConfigCli != null && snmpConfigCli.getUsername() != null && snmpConfigCli.getPassword() == null) {
			snmpConfigCli.setPassword(passwordReader.read("%s password for SNMP: ", snmpConfigCli.getUsername()));
		}
	}

	/**
	 * Try to start the interactive mode to request and set IPMI password
	 *
	 * @param passwordReader password reader which displays the prompt text and wait for user's input
	 */
	void tryInteractiveIpmiPassword(final CliPasswordReader<char[]> passwordReader) {
		if (ipmiConfigCli != null && ipmiConfigCli.getUsername() != null && ipmiConfigCli.getPassword() == null) {
			ipmiConfigCli.setPassword(passwordReader.read("%s password for IPMI: ", ipmiConfigCli.getUsername()));
		}
	}

	/**
	 * Try to start the interactive mode to request and set SSH password
	 *
	 * @param passwordReader password reader which displays the prompt text and wait for user's input
	 */
	void tryInteractiveSshPassword(final CliPasswordReader<char[]> passwordReader) {
		if (sshConfigCli != null && sshConfigCli.getUsername() != null && sshConfigCli.getPassword() == null) {
			sshConfigCli.setPassword(passwordReader.read("%s password for SSH: ", sshConfigCli.getUsername()));
		}
	}

	/**
	 * Try to start the interactive mode to request and set HTTP password
	 *
	 * @param passwordReader password reader which displays the prompt text and wait for user's input
	 */
	void tryInteractiveHttpPassword(final CliPasswordReader<char[]> passwordReader) {
		if (httpConfigCli != null && httpConfigCli.getUsername() != null && httpConfigCli.getPassword() == null) {
			httpConfigCli.setPassword(passwordReader.read("%s password for HTTP: ", httpConfigCli.getUsername()));
		}
	}

	/**
	 * Try to start the interactive mode to request and set WMI password
	 *
	 * @param passwordReader password reader which displays the prompt text and wait for user's input
	 */
	void tryInteractiveWmiPassword(final CliPasswordReader<char[]> passwordReader) {
		if (wmiConfigCli != null && wmiConfigCli.getUsername() != null && wmiConfigCli.getPassword() == null) {
			wmiConfigCli.setPassword(passwordReader.read("%s password for WMI: ", wmiConfigCli.getUsername()));
		}
	}

	/**
	 * Try to start the interactive mode to request and set WBEM password
	 *
	 * @param passwordReader password reader which displays the prompt text and wait for user's input
	 */
	void tryInteractiveWbemPassword(final CliPasswordReader<char[]> passwordReader) {
		if (wbemConfigCli != null && wbemConfigCli.getUsername() != null && wbemConfigCli.getPassword() == null) {
			wbemConfigCli.setPassword(passwordReader.read("%s password for WBEM: ", wbemConfigCli.getUsername()));
		}
	}

	/**
	 * Try to start the interactive mode to request and set WinRM password
	 *
	 * @param passwordReader password reader which displays the prompt text and wait for user's input
	 */
	void tryInteractiveWinRmPassword(final CliPasswordReader<char[]> passwordReader) {
		if (winRmConfigCli != null && winRmConfigCli.getUsername() != null && winRmConfigCli.getPassword() == null) {
			winRmConfigCli.setPassword(passwordReader.read("%s password for WinRM: ", winRmConfigCli.getUsername()));
		}
	}

	/**
	 * Prints the list of connectors embedded in the engine.
	 *
	 * @param connectorStore Wraps all the connectors
	 * @param printWriter    Prints formatted representations of objects to a text-output stream
	 * @return success exit code
	 */
	int listAllConnectors(final ConnectorStore connectorStore, final PrintWriter printWriter) {
		connectorStore
			.getStore()
			.entrySet()
			.stream()
			.filter(Objects::nonNull)
			.filter(e -> e.getValue() != null && e.getValue().getCompiledFilename() != null)
			.sorted((e1, e2) -> e1.getValue().getCompiledFilename().compareToIgnoreCase(e2.getValue().getCompiledFilename()))
			.forEachOrdered(connectorEntry -> {
				final String connectorName = connectorEntry.getKey();
				final Connector connector = connectorEntry.getValue();
				final String osList = connector
					.getConnectorIdentity()
					.getDetection()
					.getAppliesTo()
					.stream()
					.map(DeviceKind::getDisplayName)
					.collect(Collectors.joining(", "));

				printWriter.println(
					Ansi
						.ansi()
						.fgYellow()
						.a(connectorName)
						.fgDefault()
						.a(" ".repeat(30 - connectorName.length()))
						.a(Attribute.ITALIC)
						.fgCyan()
						.a(String.format("%-20s ", osList))
						.fgDefault()
						.a(Attribute.ITALIC_OFF)
						.a(connector.getConnectorIdentity().getDisplayName())
						.toString()
				);
				printWriter.flush();
			});

		return CommandLine.ExitCode.OK;
	}

	@FunctionalInterface
	interface CliPasswordReader<R> {
		/**
		 * Applies this function to the given arguments to read a password
		 *
		 * @param fmt  A format string
		 * @param args Arguments referenced by the format specifiers in the format string
		 * @return the function result
		 */
		R read(String fmt, Object... args);
	}
}
