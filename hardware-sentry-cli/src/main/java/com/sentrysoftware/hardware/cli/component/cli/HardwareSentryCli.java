package com.sentrysoftware.hardware.cli.component.cli;

import java.util.Set;
import java.util.concurrent.Callable;

import org.apache.logging.log4j.ThreadContext;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import com.sentrysoftware.hardware.cli.component.cli.protocols.HttpCredentials;
import com.sentrysoftware.hardware.cli.component.cli.protocols.IpmiCredentials;
import com.sentrysoftware.hardware.cli.component.cli.protocols.SnmpCredentials;
import com.sentrysoftware.hardware.cli.component.cli.protocols.WbemCredentials;
import com.sentrysoftware.hardware.cli.component.cli.protocols.WmiCredentials;
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
	private SnmpCredentials snmpCredentials;

	@ArgGroup(validate = false)
	private WbemCredentials wbemCredentials;

	@ArgGroup(validate = false)
	private WmiCredentials wmiCredentials;

	@ArgGroup(validate = false)
	private HttpCredentials httpCredentials;

	@ArgGroup(validate = false)
	private IpmiCredentials ipmiCredentials;

	@Option(names = { "-hdfs", "--connectors" }, split = ",", description = "Enter the hdfs to run.")
	private Set<String> hdfs;
	
	@Option(names = { "-hdfsExcluded", "--connectorsExcluded" }, split = ",", description = "Enter the hdfs to exclude.")
	private Set<String> hdfsExclusion;

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