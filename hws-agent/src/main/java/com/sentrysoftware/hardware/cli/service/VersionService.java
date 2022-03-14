package com.sentrysoftware.hardware.cli.service;

import java.util.Properties;

import org.springframework.core.io.ClassPathResource;
import org.springframework.core.io.Resource;
import org.springframework.core.io.support.PropertiesLoaderUtils;

import com.sentrysoftware.matrix.connector.ConnectorStore;

import picocli.CommandLine.IVersionProvider;

public class VersionService implements IVersionProvider {

	@Override
	public String[] getVersion() throws Exception {

		final Resource resource = new ClassPathResource("application-cli.properties");
		final Properties props = PropertiesLoaderUtils.loadProperties(resource);

		final String projectName = props.get("project.name").toString();
		final String projectVersion = props.get("project.version").toString();
		final String buildNumber = props.get("buildNumber").toString();
		final String buildDate = props.get("buildDate").toString();
		final String hcVersion = props.get("hcVersion").toString();

		return new String[] {
				"           __   __             __   ___     @|bold,green __|@   ___      ___  __      ",
				"@|bold,green |__||@  /\\  |__) |  \\ @|bold,green |  ||@  /\\  |__) |__     @|bold,green /__`|@ |__  |\\ |  |  |__) \\ / ",
				"@|bold,green |  ||@ /~~\\ |  \\ |__/ @|bold,green |/\\||@ /~~\\ |  \\ |___    @|bold,green .__/|@ |___ | \\|  |  |  \\  |  ",
				"                                        @|faint Copyright (c) Sentry Software|@",
				"",
				String.format("@|bold %s|@ version @|bold,green %s|@", projectName, projectVersion),
				String.format("@|faint - Build Number:|@ @|green %s (on %s)|@", buildNumber, buildDate),
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
