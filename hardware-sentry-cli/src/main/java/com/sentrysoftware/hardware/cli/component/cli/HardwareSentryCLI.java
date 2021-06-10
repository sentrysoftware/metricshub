package com.sentrysoftware.hardware.cli.component.cli;

import java.util.Set;
import java.util.concurrent.Callable;

import com.sentrysoftware.hardware.cli.component.cli.protocols.HTTPCredentials;
import org.apache.logging.log4j.ThreadContext;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import com.sentrysoftware.hardware.cli.component.cli.protocols.SNMPCredentials;
import com.sentrysoftware.hardware.cli.component.cli.protocols.WBEMCredentials;
import com.sentrysoftware.hardware.cli.service.EngineService;
import com.sentrysoftware.matrix.engine.target.TargetType;

import lombok.Data;
import picocli.CommandLine.ArgGroup;
import picocli.CommandLine.Command;
import picocli.CommandLine.Option;

@Component
@Command(name = "hardware-sentry-cli", mixinStandardHelpOptions = true)
@Data
public class HardwareSentryCLI implements Callable<Boolean> {

	@Autowired
	private EngineService engineService;
	
	@Option(names = { "--hostname", "-host" }, required = true, description = "Enter a hostname or an IP Address.")
	private String hostname;

	@Option(names = { "--device-type", "-dt" }, required = true, description = "Enter the Device Type to monitor.")
	private TargetType deviceType;

	@ArgGroup(validate = false)
	private SNMPCredentials snmpCredentials;

	@ArgGroup(validate = false)
	private WBEMCredentials wbemCredentials;

	@ArgGroup(validate = false)
	private HTTPCredentials httpCredentials;

	@Option(names = { "-hdfs", "--connectors" }, split = ",", description = "Enter the hdfs to run.")
	private Set<String> hdfs;
	
	@Option(names = { "-hdfsExcluded", "--connectorsExcluded" }, split = ",", description = "Enter the hdfs to exclude.")
	private Set<String> hdfsExclusion;

	@Option(names = { "--debug", "-d"}, description = "Activate debug mode for logs.")
	private boolean debug;

	@Override
	public Boolean call() {

		configureLoggerContext();
		System.out.println(engineService.call(this)); // NOSONAR

		return true;
	}

	/**
	 * Configure the logger context with the hostname and debugMode.
	 */
	private void configureLoggerContext() {

		ThreadContext.put("targetId", hostname);
		ThreadContext.put("debugMode", String.valueOf(debug));
	}
}