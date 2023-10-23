package com.sentrysoftware.metricshub.cli.service;

import java.util.concurrent.Callable;
import lombok.Data;
import picocli.CommandLine;
import picocli.CommandLine.Command;
import picocli.CommandLine.Model.CommandSpec;
import picocli.CommandLine.Option;
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
	"It natively leverages various system management protocols to discover the hardware components of a system, " +
	"and report their operational status.%n%n" +
	"Additionally, the power consumption of the system is measured, or estimated if no power sensor is detected.",
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

	@Override
	public Integer call() throws Exception {
		spec.commandLine().getOut().println("Coming soon...");
		spec.commandLine().getOut().flush();
		return CommandLine.ExitCode.OK;
	}
}
