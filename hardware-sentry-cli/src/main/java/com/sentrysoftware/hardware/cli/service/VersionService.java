package com.sentrysoftware.hardware.cli.service;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import com.sentrysoftware.matrix.connector.ConnectorStore;

import picocli.CommandLine.IVersionProvider;

@Service
public class VersionService implements IVersionProvider {

	// These properties come from src/main/resources/application.yml
	// which itself is "filtered" by Maven's resources plugin to expose
	// pom.xml's values
	@Value("${project.name}")
	String projectName;

	@Value("${project.version}")
	String projectVersion;

	@Value("${buildNumber}")
	String buildNumber;

	@Value("${timestamp}")
	String timestamp;

	@Value("${hcVersion}")
	String hcVersion;

	@Override
	public String[] getVersion() throws Exception {
		return new String[] {
				"           __   __             __   ___     @|bold,green __|@   ___      ___  __      ",
				"@|bold,green |__||@  /\\  |__) |  \\ @|bold,green |  ||@  /\\  |__) |__     @|bold,green /__`|@ |__  |\\ |  |  |__) \\ / ",
				"@|bold,green |  ||@ /~~\\ |  \\ |__/ @|bold,green |/\\||@ /~~\\ |  \\ |___    @|bold,green .__/|@ |___ | \\|  |  |  \\  |  ",
				"                                        @|faint Copyright (c) Sentry Software|@",
				"",
				String.format("@|bold %s|@ version @|bold,green %s|@", projectName, projectVersion),
				String.format("@|faint - Build Number:|@ @|green %s (on %s)|@", buildNumber, timestamp),
				String.format(
						"- Hardware Connector Library version @|green,bold %s|@ @|green (%d connectors)|@",
						hcVersion,
						ConnectorStore.getInstance().getConnectors().size()
				),
				"",
				"Java version @|green,bold ${java.version}|@ @|faint (${java.vendor} ${java.vm.name} ${java.vm.version})|@",
				"@|faint - Java Home:|@ @|green ${java.home}|@",
				"@|faint - System:|@ @|green ${os.name} ${os.version} ${os.arch}|@"
		};
	}

}
