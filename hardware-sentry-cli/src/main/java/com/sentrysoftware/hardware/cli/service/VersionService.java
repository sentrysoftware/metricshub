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
				String.format("@|bold %s|@ version @|bold,blue %s|@", projectName, projectVersion),
				String.format("@|faint Build Number:|@ @|blue,faint %s|@ @|faint (on %s)|@", buildNumber, timestamp),
				String.format(
						"Hardware Connector Library version @|cyan %s|@ (@|cyan %d|@ connectors)",
						hcVersion,
						ConnectorStore.getInstance().getConnectors().size()
				),
				"Java version ${java.version} (${java.vendor} ${java.vm.name} ${java.vm.version})",
				"Java Home: ${java.home}",
				"System: ${os.name} ${os.version} ${os.arch}"
		};
	}

}
