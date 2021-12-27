package com.sentrysoftware.hardware.cli.component.cli;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Collection;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Set;
import java.util.concurrent.Callable;
import java.util.function.Function;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import com.sentrysoftware.matrix.connector.model.common.OSType;
import org.apache.logging.log4j.Level;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.ThreadContext;
import org.apache.logging.log4j.core.LoggerContext;
import org.apache.logging.log4j.core.config.Configuration;
import org.apache.logging.log4j.core.config.LoggerConfig;
import org.fusesource.jansi.Ansi;
import org.fusesource.jansi.Ansi.Attribute;

import com.sentrysoftware.hardware.cli.component.cli.converters.TargetTypeConverter;
import com.sentrysoftware.hardware.cli.component.cli.printer.PrettyPrinter;
import com.sentrysoftware.hardware.cli.component.cli.protocols.HttpConfigCli;
import com.sentrysoftware.hardware.cli.component.cli.protocols.IpmiConfigCli;
import com.sentrysoftware.hardware.cli.component.cli.protocols.SnmpConfigCli;
import com.sentrysoftware.hardware.cli.component.cli.protocols.SshConfigCli;
import com.sentrysoftware.hardware.cli.component.cli.protocols.WbemConfigCli;
import com.sentrysoftware.hardware.cli.component.cli.protocols.WmiConfigCli;
import com.sentrysoftware.hardware.cli.service.ConsoleService;
import com.sentrysoftware.hardware.cli.service.VersionService;
import com.sentrysoftware.matrix.connector.ConnectorStore;
import com.sentrysoftware.matrix.connector.model.Connector;
import com.sentrysoftware.matrix.connector.model.monitor.MonitorType;
import com.sentrysoftware.matrix.connector.parser.ConnectorParser;
import com.sentrysoftware.matrix.engine.EngineConfiguration;
import com.sentrysoftware.matrix.engine.EngineResult;
import com.sentrysoftware.matrix.engine.OperationStatus;
import com.sentrysoftware.matrix.engine.protocol.IProtocolConfiguration;
import com.sentrysoftware.matrix.engine.protocol.SNMPProtocol.Privacy;
import com.sentrysoftware.matrix.engine.protocol.SNMPProtocol.SNMPVersion;
import com.sentrysoftware.matrix.engine.strategy.collect.CollectOperation;
import com.sentrysoftware.matrix.engine.strategy.detection.DetectionOperation;
import com.sentrysoftware.matrix.engine.strategy.discovery.DiscoveryOperation;
import com.sentrysoftware.matrix.engine.target.HardwareTarget;
import com.sentrysoftware.matrix.engine.target.TargetType;
import com.sentrysoftware.matrix.model.monitoring.HostMonitoringFactory;
import com.sentrysoftware.matrix.model.monitoring.IHostMonitoring;

import lombok.Data;
import picocli.CommandLine;
import picocli.CommandLine.ArgGroup;
import picocli.CommandLine.Command;
import picocli.CommandLine.Model.CommandSpec;
import picocli.CommandLine.Option;
import picocli.CommandLine.ParameterException;
import picocli.CommandLine.Parameters;
import picocli.CommandLine.Spec;

@Command(
		name = "hws",
		sortOptions = false,
		usageHelpAutoWidth = true,
		versionProvider = VersionService.class,
		headerHeading = "%n",
		header = "Gather hardware-related information from the specified host.",
		synopsisHeading = "%n@|bold,underline Usage|@:%n%n",
		descriptionHeading = "%n@|bold,underline Description|@:%n%n",
		description = "This tool is the CLI version of the @|italic Hardware Sentry|@ engine. " +
				"It leverages various system management protocols to discover the hardware components of a system, " +
				"and report their operational status.%n%n" +
				"Additionally, the power consumption of the system is measured, or estimated if no power sensor is detected.",
		parameterListHeading = "%n@|bold,underline Parameters|@:%n",
		optionListHeading = "%n@|bold,underline Options|@:%n",
		customSynopsis = {
				"@|bold ${ROOT-COMMAND-NAME}|@ " +
						"@|yellow HOSTNAME|@ " +
						"@|yellow -t|@=@|italic TYPE|@ " +
						"<@|yellow --http|@|@|yellow --https|@|@|yellow --ipmi|@|@|yellow --snmp|@=@|italic VERSION|@|@|yellow --ssh|@|@|yellow --wbem|@|@|yellow --wmi|@> " +
						"[@|yellow -u|@=@|italic USER|@ [@|yellow -p|@=@|italic P4SSW0RD|@]] [OPTIONS]..."
		}
)
@Data
public class HardwareSentryCli implements Callable<Integer> {

	@Spec
	CommandSpec spec;

	@Option(
			names = {"-V", "--version"},
			versionHelp = true,
			description = "Prints version information and exits"
	)
	boolean versionInfoRequested;

	@Option(
			names = {"-h", "-?", "--help"},
			usageHelp = true,
			description = "Shows this help message and exits"
	)
	boolean usageHelpRequested;

	@Parameters(
			index = "0",
			paramLabel = "HOSTNAME",
			description = "Hostname of IP address of the target to monitor"
	)
	private String hostname;

	@Option(
			names = { "-t", "--type" },
			order = 1,
			required = true,
			paramLabel = "TYPE",
			description = "Type of the host to monitor (lin, linux, win, windows, mgmt, management, storage, network, aix, hpux, solaris, tru64, vms)",
			converter = TargetTypeConverter.class
	)
	private TargetType deviceType;

	@ArgGroup(exclusive = false, heading = "%n@|bold,underline HTTP Options|@:%n")
	private HttpConfigCli httpConfigCli;

	@ArgGroup(exclusive = false, heading = "%n@|bold,underline IPMI Options|@:%n")
	private IpmiConfigCli ipmiConfigCli;

	@ArgGroup(exclusive = false, heading = "%n@|bold,underline SNMP Options|@:%n")
	private SnmpConfigCli snmpConfigCli;

	@ArgGroup(exclusive = false, heading = "%n@|bold,underline SSH Options|@:%n")
	private SshConfigCli sshConfigCli;

	@ArgGroup(exclusive = false, heading = "%n@|bold,underline WBEM Options|@:%n")
	private WbemConfigCli wbemConfigCli;

	@ArgGroup(exclusive = false, heading = "%n@|bold,underline WMI Options|@:%n")
	private WmiConfigCli wmiConfigCli;

	@Option(
			names = { "-u", "--username" },
			order = 2,
			paramLabel = "USER",
			description = "Username for authentication"
	)
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
			description = "Force selected hardware connectors to connect to the target (use @|bold ${ROOT-COMMAND-NAME} -l|@ to get the list of connectors)"
	)
	private Set<String> connectors;

	@Option(
			names = { "-x", "--exclude" },
			order = 5,
			split = ",",
			paramLabel = "CONNECTOR",
			description = "Exclude connectors from the automatic detection process (use @|bold ${ROOT-COMMAND-NAME} -l|@ to get the list of connectors)"
	)
	private Set<String> excludedConnectors;

	@Option(
			names = { "-a", "--add" },
			order = 6,
			split = ",",
			paramLabel = "FILE",
			description = "Parse a connector source file and update the internal connector library"
	)
	private Set<File> addedSourceFiles;

	@Option(
			names = { "-l", "--list" },
			order = 7,
			description = "Lists all connectors bundled in the engine, that can be selected or excluded",
			help = true
	)
	private boolean listConnectors;

	@Option(
			names = "-v",
			order = 8,
			description = "Verbose mode (repeat the option to increase verbosity)"
	)
	private boolean[] verbose;

	@Override
	public Integer call() throws IOException {

		// First, process special "help" options
		if (listConnectors) {
			return listConnectors();
		}

		// Do we need to update the ConnectorStore?
		if (addedSourceFiles != null && !addedSourceFiles.isEmpty()) {
			loadExtraConnectors(addedSourceFiles);
		}

		// Validate inputs
		validate();

		// Setup Log4j
		setLogLevel();

		// Configure the Matrix engine for the specified host
		EngineConfiguration engineConf = new EngineConfiguration();

		// Target
		engineConf.setTarget(new HardwareTarget(hostname, hostname, deviceType));

		// Protocols
		Map<Class<? extends IProtocolConfiguration>, IProtocolConfiguration> protocols = getProtocols();
		engineConf.setProtocolConfigurations(protocols);

		// Connectors
		if (connectors != null) {
			engineConf.setSelectedConnectors(connectors);
		}
		if (excludedConnectors != null) {
			engineConf.setExcludedConnectors(excludedConnectors);
		}

		// Create a new HostMonitoring
		IHostMonitoring hostMonitoring =
				HostMonitoringFactory.getInstance().createHostMonitoring(hostname, engineConf);

		// Detection
		if (ConsoleService.hasConsole()) {
			String protocolDisplay = protocols.values()
					.stream()
					.map(proto -> Ansi.ansi().bold().a(proto.toString()).boldOff().toString())
					.collect(Collectors.joining(", "));
			spec.commandLine().getOut().print("Performing detection on ");
			spec.commandLine().getOut().print(Ansi.ansi().bold().a(hostname).boldOff().toString());
			spec.commandLine().getOut().print(" using ");
			spec.commandLine().getOut().print(protocolDisplay);
			spec.commandLine().getOut().println("...");
			spec.commandLine().getOut().flush();
		}
		EngineResult engineResult = hostMonitoring.run(new DetectionOperation());
		if (engineResult.getOperationStatus() != OperationStatus.SUCCESS) {
			spec.commandLine().getOut().println(ConsoleService.statusToAnsi(engineResult.getOperationStatus()));
			spec.commandLine().getOut().flush();
			return CommandLine.ExitCode.SOFTWARE;
		}

		// Discovery
		if (ConsoleService.hasConsole()) {
			int connectorCount = hostMonitoring.selectFromType(MonitorType.CONNECTOR).size();
			spec.commandLine().getOut().print("Performing discovery with ");
			spec.commandLine().getOut().print(Ansi.ansi().bold().a(connectorCount).boldOff().toString());
			spec.commandLine().getOut().println(connectorCount > 1 ? " connectors..." : " connector...");
			spec.commandLine().getOut().flush();
		}
		engineResult = hostMonitoring.run(new DiscoveryOperation());
		if (engineResult.getOperationStatus() != OperationStatus.SUCCESS) {
			spec.commandLine().getOut().println(ConsoleService.statusToAnsi(engineResult.getOperationStatus()));
			spec.commandLine().getOut().flush();
			return CommandLine.ExitCode.SOFTWARE;
		}

		// Collect
		if (ConsoleService.hasConsole()) {
			long monitorCount = hostMonitoring.getMonitors()
					.values()
					.stream()
					.map(Map::values)
					.mapToLong(Collection::size)
					.sum();
			spec.commandLine().getOut().print("Performing collect on ");
			spec.commandLine().getOut().print(Ansi.ansi().bold().a(monitorCount).boldOff().toString());
			spec.commandLine().getOut().println(monitorCount > 1 ? " monitors..." : " monitor...");
			spec.commandLine().getOut().flush();
		}
		engineResult = hostMonitoring.run(new CollectOperation());
		if (engineResult.getOperationStatus() != OperationStatus.SUCCESS) {
			spec.commandLine().getOut().println(ConsoleService.statusToAnsi(engineResult.getOperationStatus()));
			spec.commandLine().getOut().flush();
			return CommandLine.ExitCode.SOFTWARE;
		}

		// And now the result
		if (ConsoleService.hasConsole()) {
			spec.commandLine().getOut().print("\n");
		}
		PrettyPrinter.print(spec.commandLine().getOut(), hostMonitoring, true, true);
		// spec.commandLine().getOut().print(jobResultFormatterService.format(hostMonitoring));

		return CommandLine.ExitCode.OK;
	}

	/**
	 * Validate the specified arguments, and ask for passwords if needed.
	 * @throws ParameterException in case of invalid parameter
	 */
	private void validate() {

		// Can we ask for passwords interactively?
		final boolean interactive = ConsoleService.hasConsole();

		// Passwords
		if (interactive) {
			if (username != null && password == null) {
				password = System.console().readPassword("%s password: ", username);
			}
			if (httpConfigCli != null) {
				if (httpConfigCli.getUsername() != null && httpConfigCli.getPassword() == null) {
					httpConfigCli.setPassword(System.console().readPassword("%s password for HTTP: ", httpConfigCli.getUsername()));
				}
			}
			if (ipmiConfigCli != null) {
				if (ipmiConfigCli.getUsername() != null && ipmiConfigCli.getPassword() == null) {
					ipmiConfigCli.setPassword(System.console().readPassword("%s password for IPMI: ", ipmiConfigCli.getUsername()));
				}
			}
			if (snmpConfigCli != null) {
				if (snmpConfigCli.getUsername() != null && snmpConfigCli.getPassword() == null) {
					snmpConfigCli.setPassword(System.console().readPassword("%s password for SNMP: ", snmpConfigCli.getUsername()));
				}
				if (snmpConfigCli.getPrivacy() == Privacy.AES || snmpConfigCli.getPrivacy() == Privacy.DES) {
					snmpConfigCli.setPrivacyPassword(System.console().readPassword("SNMP Privacy password: "));
				}
			}
			if (sshConfigCli != null) {
				if (sshConfigCli.getUsername() != null && sshConfigCli.getPassword() == null) {
					sshConfigCli.setPassword(System.console().readPassword("%s password for SSH: ", sshConfigCli.getUsername()));
				}
			}
			if (wbemConfigCli != null) {
				if (wbemConfigCli.getUsername() != null && wbemConfigCli.getPassword() == null) {
					wbemConfigCli.setPassword(System.console().readPassword("%s password for WBEM: ", wbemConfigCli.getUsername()));
				}
			}
			if (wmiConfigCli != null) {
				if (wmiConfigCli.getUsername() != null && wmiConfigCli.getPassword() == null) {
					wmiConfigCli.setPassword(System.console().readPassword("%s password for WMI: ", wmiConfigCli.getUsername()));
				}
			}
		}

		// No protocol at all?
		if (httpConfigCli == null && ipmiConfigCli == null && snmpConfigCli == null
				&& sshConfigCli == null && wbemConfigCli == null && wmiConfigCli == null) {
			throw new ParameterException(spec.commandLine(), "At least one protocol must be specified: --http[s], --ipmi, --snmp, --ssh, --wbem, --wmi.");
		}

		// SNMP inconsistencies
		if (snmpConfigCli != null) {
			SNMPVersion version = snmpConfigCli.getSnmpVersion();
			if (version == SNMPVersion.V1 || version == SNMPVersion.V2C) {
				if (snmpConfigCli.getCommunity() == null || snmpConfigCli.getCommunity().isBlank()) {
					throw new ParameterException(spec.commandLine(), "Community string is required for SNMP " + version);
				}
				if (snmpConfigCli.getUsername() != null && username == null) {
					throw new ParameterException(spec.commandLine(), "Username/password is not supported in SNMP " + version);
				}
				if (snmpConfigCli.getPrivacy() != null && snmpConfigCli.getPrivacy() != Privacy.NO_ENCRYPTION
						|| snmpConfigCli.getPrivacyPassword() != null) {
					throw new ParameterException(spec.commandLine(), "Privacy (encryption) is not supported in SNMP " + version);
				}
			} else {
				if (version == SNMPVersion.V3_MD5 || version == SNMPVersion.V3_SHA) {
					if ((snmpConfigCli.getUsername() == null && username == null)
							|| (snmpConfigCli.getPassword() == null && password == null)) {
						throw new ParameterException(spec.commandLine(), "Username and password are required for SNMP " + version);
					}
				}
				if (snmpConfigCli.getPrivacy() != null && snmpConfigCli.getPrivacy() != Privacy.NO_ENCRYPTION
						&& snmpConfigCli.getPrivacyPassword() == null) {
					throw new ParameterException(spec.commandLine(), "A privacy password is required for SNMP encryption (--snmp-privacy-password)");
				}
			}
		}

		// Connectors
		Map<String, Connector> allConnectors = ConnectorStore.getInstance().getConnectors();
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
			case 0: logLevel = Level.ERROR; break;
			case 1: logLevel = Level.WARN; break;
			case 2: logLevel = Level.INFO; break;
			case 3: logLevel = Level.DEBUG; break;
			default: logLevel = Level.ALL;
			}

			// Update the Log level at the root level
			LoggerContext loggerContext = (LoggerContext) LogManager.getContext(false);
			Configuration config = loggerContext.getConfiguration();
			LoggerConfig loggerConfig = config.getLoggerConfig(LogManager.ROOT_LOGGER_NAME);
			loggerConfig.setLevel(logLevel);
			loggerContext.updateLoggers();

		}

	}

	/**
	 * @return A {@link Map} associating the input protocol type to its input credentials.
	 */
	private Map<Class< ? extends IProtocolConfiguration>, IProtocolConfiguration> getProtocols() {

		return Stream.of(httpConfigCli, ipmiConfigCli, snmpConfigCli, sshConfigCli, wbemConfigCli, wmiConfigCli)
				.filter(Objects::nonNull)
				.map(protocolConfig -> protocolConfig.toProtocol(username, password))
				.collect(Collectors.toMap(
						IProtocolConfiguration::getClass,
						Function.identity())
		);

	}

	/**
	 * Prints the list of connectors embedded in the engine.
	 * @return success exit code
	 */
	private int listConnectors() {

		ConnectorStore.getInstance().getConnectors()
		.entrySet()
		.stream()
		.filter(Objects::nonNull)
		.filter(e -> e.getValue() != null && e.getValue().getDisplayName() != null)
		.sorted((e1, e2) -> e1.getValue().getDisplayName().compareToIgnoreCase(e2.getValue().getDisplayName()))
		.forEachOrdered(connectorEntry -> {

			String connectorName = connectorEntry.getKey();
			Connector connector = connectorEntry.getValue();
			String osList = connector.getAppliesToOS().stream().map(OSType::getDisplayName).collect(Collectors.joining(", "));

			spec.commandLine().getOut().println(
					Ansi.ansi()
							.fgYellow()
							.a(connectorName)
							.fgDefault()
							.a(" ".repeat(30 - connectorName.length()))
							.a(Attribute.ITALIC)
							.fgCyan()
							.a(String.format("%-20s ", osList))
							.fgDefault()
							.a(Attribute.ITALIC_OFF)
							.a(connectorEntry.getValue().getDisplayName())
							.toString()
			);
		});

		return CommandLine.ExitCode.OK;
	}


	/**
	 * Parse and add the specified connector source files to the ConnectorStore
	 *
	 * @param connectorFiles Set of connector source files, directories, etc.
	 *
	 * @throws IOException when the files cannot be read
	 */
	private void loadExtraConnectors(final Set<File> connectorFiles) throws IOException {

		// First, build the list of connectors
		final Set<String> parsedConnectorPaths = new HashSet<>();
		for (final File connectorFile : connectorFiles) {
			if (connectorFile.isFile()) {
				parsedConnectorPaths.add(connectorFile.getAbsolutePath());
			} else if (connectorFile.isDirectory()) {
				try (Stream<Path> pathStream = Files.list(connectorFile.toPath())) {
					pathStream
							.map(path -> path.toAbsolutePath().toString())
							.filter(pathString -> pathString.toLowerCase().endsWith(".hdfs"))
							.forEach(parsedConnectorPaths::add);
				}
			} else {
				throw new IOException("Could not find connector source file: " + connectorFile.toString());
			}
		}

		// Sort
		List<String> sortedConnectorList = parsedConnectorPaths.stream().sorted().collect(Collectors.toUnmodifiableList());

		// Get the ConnectorStore
		final ConnectorStore store = ConnectorStore.getInstance();

		// Get the connectorParser
		final ConnectorParser connectorParser = new ConnectorParser();

		// Go through each files
		for (String sourceFilePath : sortedConnectorList) {

			if (ConsoleService.hasConsole()) {
				spec.commandLine().getOut().print("Adding connector ");
				spec.commandLine().getOut().print(Ansi.ansi().bold().a(sourceFilePath).boldOff().toString());
				spec.commandLine().getOut().println("...");
				spec.commandLine().getOut().flush();
			}

			Connector connector;

			// Parse the connector
			connector = connectorParser.parse(sourceFilePath);

			// Add it to the map
			store.getConnectors().put(connector.getCompiledFilename(), connector);

			// Did we encounter parsing errors?
			connector.getProblemList().forEach(problem -> {
				spec.commandLine().getErr().println(spec.commandLine().getColorScheme().errorText(" - " + problem));
				spec.commandLine().getErr().flush();
			});

		}
	}

}