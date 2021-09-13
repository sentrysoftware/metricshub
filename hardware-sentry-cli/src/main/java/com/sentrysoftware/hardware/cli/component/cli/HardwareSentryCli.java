package com.sentrysoftware.hardware.cli.component.cli;

import java.util.Set;
import java.util.concurrent.Callable;

import org.apache.logging.log4j.ThreadContext;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import com.sentrysoftware.hardware.cli.component.cli.converters.TargetTypeConverter;
import com.sentrysoftware.hardware.cli.component.cli.protocols.HttpConfig;
import com.sentrysoftware.hardware.cli.component.cli.protocols.IpmiConfig;
import com.sentrysoftware.hardware.cli.component.cli.protocols.SnmpConfig;
import com.sentrysoftware.hardware.cli.component.cli.protocols.WbemConfig;
import com.sentrysoftware.hardware.cli.component.cli.protocols.WmiConfig;
import com.sentrysoftware.hardware.cli.service.EngineService;
import com.sentrysoftware.hardware.cli.service.VersionService;
import com.sentrysoftware.matrix.engine.protocol.SNMPProtocol.Privacy;
import com.sentrysoftware.matrix.engine.target.TargetType;

import lombok.Data;
import picocli.CommandLine;
import picocli.CommandLine.ArgGroup;
import picocli.CommandLine.Command;
import picocli.CommandLine.Option;
import picocli.CommandLine.Parameters;
import picocli.CommandLine.Spec;
import picocli.CommandLine.Model.CommandSpec;
import picocli.CommandLine.ParameterException;

@Component
@Command(
		name = "hws",
		mixinStandardHelpOptions = true,
		abbreviateSynopsis = true,
		sortOptions = false,
		usageHelpAutoWidth = true,
		versionProvider = VersionService.class
)
@Data
public class HardwareSentryCli implements Callable<Integer> {

	@Autowired
	private EngineService engineService;

	@Spec
	CommandSpec spec;

	@Parameters(
			index = "0",
			description = "Hostname of IP address of the target to monitor"
	)
	private String hostname;

	@Option(
			names = { "-t", "--type" },
			order = 1,
			required = true,
			description = "Type of the host to monitor (lin, linux, win, windows, mgmt, management, storage, network, aix, hpux, solaris, tru64, vms)",
			converter = TargetTypeConverter.class
	)
	private TargetType deviceType;

	@ArgGroup(exclusive = false, heading = "@|bold SNMP Options|@%n")
	private SnmpConfig snmpConfig;

	@ArgGroup(exclusive = false, heading = "@|bold WBEM Options|@%n")
	private WbemConfig wbemConfig;

	@ArgGroup(exclusive = false, heading = "@|bold WMI Options|@%n")
	private WmiConfig wmiConfig;

	@ArgGroup(exclusive = false, heading = "@|bold HTTP Options|@%n")
	private HttpConfig httpConfig;

	@ArgGroup(exclusive = false, heading = "@|bold IPMI Options|@%n")
	private IpmiConfig ipmiConfig;

	@Option(
			names = { "-u", "--username" },
			order = 2,
			description = "Username for authentication"
	)
	String username;

	@Option(
			names = { "-p", "--password" },
			order = 3,
			description = "Associated password",
			arity = "0..1",
			interactive = true
	)
	char[] password;

	@Option(
			names = { "-f", "--force" },
			order = 4,
			split = ",",
			description = "Force selected hardware connectors to connect to the target"
	)
	private Set<String> connectors;

	@Option(
			names = { "-x", "--exclude-connectors" },
			order = 5,
			split = ",",
			description = "Specify hardware connectors that must be excluded from the automatic detection process"
	)
	private Set<String> excludedConnectors;

	@Option(
			names = { "-d", "--debug" },
			order = 6,
			description = "Activate debug mode for logs."
	)
	private boolean debug;

	@Option(
			names = { "-o", "--output" },
			order = 7,
			description = "Output directory for logs."
	)
	private String outputDirectory;

	@Override
	public Integer call() {

		validate();

		configureLoggerContext();

		System.out.println(engineService.call(this)); // NOSONAR

		return CommandLine.ExitCode.OK;
	}

	/**
	 * Validate the specified arguments, and ask for passwords if needed.
	 * @throws ParameterException in case of invalid parameter
	 */
	private void validate() {

		// Can we ask for passwords interactively?
		final boolean interactive = System.console() != null;

		// Passwords
		if (interactive) {
			if (username != null && password == null) {
				password = System.console().readPassword("%s password: ", username);
			}
			if (snmpConfig != null) {
				if (snmpConfig.getUsername() != null && snmpConfig.getPassword() == null) {
					snmpConfig.setPassword(System.console().readPassword("%s password for SNMP: ", snmpConfig.getUsername()));
				}
				if (snmpConfig.getPrivacy() == Privacy.AES || snmpConfig.getPrivacy() == Privacy.DES) {
					snmpConfig.setPrivacyPassword(System.console().readPassword("SNMP Privacy password: "));
				}
			}
			if (httpConfig != null) {
				if (httpConfig.getUsername() != null && httpConfig.getPassword() == null) {
					httpConfig.setPassword(System.console().readPassword("%s password for HTTP: ", httpConfig.getUsername()));
				}
			}
			if (ipmiConfig != null) {
				if (ipmiConfig.getUsername() != null && ipmiConfig.getPassword() == null) {
					ipmiConfig.setPassword(System.console().readPassword("%s password for IPMI: ", ipmiConfig.getUsername()));
				}
			}
			if (wbemConfig != null) {
				if (wbemConfig.getUsername() != null && wbemConfig.getPassword() == null) {
					wbemConfig.setPassword(System.console().readPassword("%s password for WBEM: ", wbemConfig.getUsername()));
				}
			}
			if (wmiConfig != null) {
				if (wmiConfig.getUsername() != null && wmiConfig.getPassword() == null) {
					wmiConfig.setPassword(System.console().readPassword("%s password for WMI: ", wmiConfig.getUsername()));
				}
			}
		}

		// No protocol at all?
		if (snmpConfig == null && httpConfig == null && ipmiConfig == null
				&& wbemConfig == null && wmiConfig == null) {
			throw new ParameterException(spec.commandLine(), "At least one protocol must be specified: --http[s], --ipmi, --snmp, --wbem, --wmi.");
		}
	}


	/**
	 * Configure the logger context with the hostname, debugMode and output directory.
	 */
	private void configureLoggerContext() {

		ThreadContext.put("targetId", hostname);
		ThreadContext.put("debugMode", String.valueOf(debug));
		if (outputDirectory != null) {
			ThreadContext.put("outputDirectory", outputDirectory);
		}
	}
}