package com.sentrysoftware.matrix.connector;

import java.io.File;
import java.io.IOException;
import java.util.Arrays;
import java.util.List;
import java.util.Optional;

import org.apache.maven.plugin.AbstractMojo;
import org.apache.maven.plugin.MojoExecutionException;
import org.apache.maven.plugin.MojoFailureException;
import org.apache.maven.plugin.logging.Log;
import org.apache.maven.plugins.annotations.LifecyclePhase;
import org.apache.maven.plugins.annotations.Mojo;
import org.apache.maven.plugins.annotations.Parameter;
import org.apache.maven.plugins.annotations.ResolutionScope;
import org.apache.maven.project.MavenProject;

import com.sentrysoftware.matrix.connector.helper.PluginHelper;
import com.sentrysoftware.matrix.connector.model.Connector;
import com.sentrysoftware.matrix.connector.parser.ConnectorParser;
import com.sentrysoftware.matrix.connector.serialize.ConnectorSerializer;
import org.springframework.util.Assert;

@Mojo(name = "connector-compile", aggregator = false, executionStrategy = "always", defaultPhase = LifecyclePhase.COMPILE, requiresDependencyResolution = ResolutionScope.RUNTIME, requiresDirectInvocation = false, requiresOnline = false, requiresProject = true, threadSafe = true)
public class ConnectorCompileMojo extends AbstractMojo {

	private static final String HDFS_CRITERIA = "*.hdfs";

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

	private Log logger = getLog();

	private ConnectorParser connectorParser = new ConnectorParser();

	@Override
	public void execute() throws MojoExecutionException, MojoFailureException {

		// Some info
		final String connectorDirRelativePath = PluginHelper.relativizeToProject(connectorDirectory, project);
		final String outputDirRelativePath  = PluginHelper.relativizeToProject(outputDirectory, project);
		logger.info("Matrix - Compiling " + project.getDescription() + " " + project.getVersion() + " from " + connectorDirRelativePath
				+ " to " + outputDirRelativePath);

		// Need to create outputDirectory?
		PluginHelper.validateOutputDirectory(outputDirectory);

		// Then, compile the .hdfs file. Compile means parsing and serializing the Connector objects
		final int count = compileHdfsFiles(logger, connectorDirectory, outputDirectory, project);

		logger.info(
				"Matrix - Successfully compiled " + count + " Hardware Connectors from " + connectorDirRelativePath
						+ " to " + outputDirRelativePath);
	}

	/**
	 * Get all .hdfs files from the connectorDirectory and serialize them using
	 * ConnectorParser
	 * 
	 * @param logger
	 * @param connectorDirectory
	 * @param outputDirectory
	 * @param project 
	 * @return number of compiled .hdfs files
	 * @throws MojoExecutionException 
	 */
	private int compileHdfsFiles(final Log logger, final File connectorDirectory, final File outputDirectory,
			final MavenProject project) throws MojoExecutionException {

		Assert.state(connectorDirectory.isDirectory(), "Matrix - connectorDirectory is not a directory");
		Assert.state(outputDirectory.isDirectory(), "Matrix - outputDirectory is not a directory");

		// List all .hdfs files in connectorDirectory
		final List<File> hdfFiles = PluginHelper.getFileList(connectorDirectory, Arrays.asList(HDFS_CRITERIA), null);

		Assert.state(null != hdfFiles, "Matrix - hdfFiles cannot be null. Something went wrong");

		int count = 0;
		for (final File hdfs : hdfFiles) {

			final String relativePath = PluginHelper.relativizeToProject(hdfs, project);

			// Parse
			final Optional<Connector> optionalConnector = connectorParser.parse(hdfs.getAbsolutePath());

			checkOptionalConnector(logger, relativePath, optionalConnector);

			// Serialize
			serialize(logger, outputDirectory, relativePath, optionalConnector.get());

			logger.info(String.format("Matrix - Compiled %s to %s", relativePath, PluginHelper.relativizeToProject(outputDirectory, project)));
			count++;
		}

		return count;

	}

	/**
	 * Check if the given {@link Optional} {@link Connector} is present
	 * @param logger
	 * @param hdfsRelativePath
	 * @param optionalConnector
	 * @throws MojoExecutionException
	 */
	private void checkOptionalConnector(final Log logger, final String hdfsRelativePath,
			final Optional<Connector> optionalConnector) throws MojoExecutionException {
		if (!optionalConnector.isPresent()) {
			// Should never occur
			final String message = String.format("Matrix - Received an empty Connector for HDF file %s", hdfsRelativePath);
			logger.error(message);
			throw new MojoExecutionException(message);
		}
	}

	/**
	 * Serialize the given {@link Connector}
	 * @param logger
	 * @param outputDirectory
	 * @param relativePath
	 * @param connector
	 * @throws MojoExecutionException
	 */
	private void serialize(final Log logger, final File outputDirectory, final String relativePath,
			final Connector connector) throws MojoExecutionException {
		try {
			ConnectorSerializer.serialize(outputDirectory.getAbsolutePath(), connector);
		} catch (IOException e) {
			final String message = String.format("Matrix - Cannot serialize connector %s ", relativePath);
			logger.error(message);
			throw new MojoExecutionException(message, e);
		}
	}
}
