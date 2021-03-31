package com.sentrysoftware.matrix.connector;

import java.io.File;

import org.apache.maven.plugin.AbstractMojo;
import org.apache.maven.plugin.MojoExecutionException;
import org.apache.maven.plugin.MojoFailureException;
import org.apache.maven.plugin.logging.Log;
import org.apache.maven.plugins.annotations.LifecyclePhase;
import org.apache.maven.plugins.annotations.Mojo;
import org.apache.maven.plugins.annotations.Parameter;
import org.apache.maven.plugins.annotations.ResolutionScope;
import org.apache.maven.project.MavenProject;

@Mojo(name = "connector-compile", aggregator = false, executionStrategy = "always", defaultPhase = LifecyclePhase.COMPILE, requiresDependencyResolution = ResolutionScope.RUNTIME, requiresDirectInvocation = false, requiresOnline = false, requiresProject = true, threadSafe = true)
public class ConnectorCompileMojo extends AbstractMojo {

	@Parameter(defaultValue = "${project}", readonly = true)
	private MavenProject project;

	/**
	 * Directory containing the Hardware Connectors source code (*.hdfs) to be
	 * compiled
	 */
	@Parameter(defaultValue = "${project.basedir}/src/main/hdf", property = "connectorDirectory", required = true)
	private File connectorDirectory;

	/**
	 * Directory where the compiled code (.hdf) will be stored
	 */
	@Parameter(defaultValue = "${project.build.directory}/matrix/connector", property = "compiledConnectorDirectory", required = true)
	private File outputDirectory;

	@Override
	public void execute() throws MojoExecutionException, MojoFailureException {

		Log logger = getLog();

		// Some info
		logger.info("Compiling " + project.getDescription() + " " + project.getVersion() + " from " + connectorDirectory
				+ " to " + outputDirectory);

		// Implementation
		int compiledConnectorCount = 0;

		logger.info(
				"Successfully compiled " + compiledConnectorCount + " Hardware Connectors from " + connectorDirectory
						+ " to " + outputDirectory);
	}

}
