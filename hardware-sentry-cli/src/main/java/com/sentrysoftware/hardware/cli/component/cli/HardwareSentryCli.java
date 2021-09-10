package com.sentrysoftware.hardware.cli.component.cli;

import java.util.Set;
import java.util.concurrent.Callable;

import org.apache.logging.log4j.ThreadContext;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import com.sentrysoftware.hardware.cli.component.cli.protocols.HttpConfig;
import com.sentrysoftware.hardware.cli.component.cli.protocols.IpmiConfig;
import com.sentrysoftware.hardware.cli.component.cli.protocols.SnmpConfig;
import com.sentrysoftware.hardware.cli.component.cli.protocols.WbemConfig;
import com.sentrysoftware.hardware.cli.component.cli.protocols.WmiConfig;
import com.sentrysoftware.hardware.cli.service.EngineService;
import com.sentrysoftware.matrix.engine.target.TargetType;

import lombok.Data;
import picocli.CommandLine.ArgGroup;
import picocli.CommandLine.Command;
import picocli.CommandLine.Option;

@Component
@Command(name = "hardware-sentry-cli", mixinStandardHelpOptions = true)
@Data
public class HardwareSentryCli implements Callable<Boolean> {

	@Autowired
	private EngineService engineService;

	@Option(names = { "--hostname", "-host" }, required = true, description = "Enter a hostname or an IP Address.")
	private String hostname;

	@Option(names = { "--device-type", "-dt" }, required = true, description = "Enter the Device Type to monitor.")
	private TargetType deviceType;

	@ArgGroup(validate = false)
	private SnmpConfig snmpConfig;

	@ArgGroup(validate = false)
	private WbemConfig wbemConfig;

	@ArgGroup(validate = false)
	private WmiConfig wmiConfig;

	@ArgGroup(validate = false)
	private HttpConfig httpConfig;

	@ArgGroup(validate = false)
	private IpmiConfig ipmiConfig;

	@Option(names = { "-hdf", "--connectors" }, split = ",", description = "Force selected hardware connectors to connect to the target")
	private Set<String> connectors;

	@Option(names = { "-exclude", "--exclude-connectors" }, split = ",", description = "Specify hardware connectors that must be excluded from the automatic detection process")
	private Set<String> excludedConnectors;

	@Option(names = { "--debug", "-d" }, description = "Activate debug mode for logs.")
	private boolean debug;

	@Option(names = { "--output", "-o"}, description = "Output directory for logs.")
	private String outputDirectory;

	@Override
	public Boolean call() {

		configureLoggerContext();

		System.out.println(engineService.call(this)); // NOSONAR

		return true;
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