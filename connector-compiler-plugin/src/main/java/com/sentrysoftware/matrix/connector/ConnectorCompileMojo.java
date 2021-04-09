package com.sentrysoftware.matrix.connector;

import java.io.File;
import java.io.IOException;
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

import com.sentrysoftware.matrix.connector.model.Connector;
import com.sentrysoftware.matrix.connector.parser.ConnectorParser;
import com.sentrysoftware.matrix.connector.serialize.ConnectorSerializer;
import com.sentrysoftware.matrix.utils.Assert;

@Mojo(name = "connector-compile", aggregator = false, executionStrategy = "always", defaultPhase = LifecyclePhase.COMPILE, requiresDependencyResolution = ResolutionScope.RUNTIME, requiresDirectInvocation = false, requiresOnline = false, requiresProject = true, threadSafe = true)
public class ConnectorCompileMojo extends AbstractMojo {

	private static final String HDFS_EXTENSION = ".hdfs";

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

	@Override
	public void execute() throws MojoExecutionException, MojoFailureException {

		// Some info
		logger.info("Matrix - Compiling " + project.getDescription() + " " + project.getVersion() + " from " + connectorDirectory
				+ " to " + outputDirectory);

		// Need to create outputDirectory?
		createOutputDirectoryIfNeeded(logger, outputDirectory);

		// Then, compile the .hdfs file. Compile means parsing and serializing the Connector objects
		final int count = compileHdfsFiles(logger, connectorDirectory, outputDirectory);

		logger.info(
				"Matrix - Successfully compiled " + count + " Hardware Connectors from " + connectorDirectory
						+ " to " + outputDirectory);
	}

	/**
	 * Create the outputDirectory if it is not created yet
	 * @param logger
	 * @param outputDirectory
	 * @throws MojoExecutionException
	 */
	private void createOutputDirectoryIfNeeded(final Log logger, final File outputDirectory) throws MojoExecutionException {
		if (!outputDirectory.exists() && !outputDirectory.mkdirs()) {
			String message = String.format("Matrix - Could not create outputDirectory: %s" , outputDirectory.getAbsolutePath());
			logger.error(message);
			throw new MojoExecutionException(message);
		}
	}

	/**
	 * Get all .hdfs files from the connectorDirectory and serialize them using
	 * ConnectorParser
	 * 
	 * @param logger
	 * @param connectorDirectory
	 * @param outputDirectory
	 * @return number of compiled .hdfs files
	 */
	private int compileHdfsFiles(final Log logger, final File connectorDirectory, final File outputDirectory) {

		Assert.state(connectorDirectory.isDirectory(), "Matrix - connectorDirectory is not a directory");
		Assert.state(outputDirectory.isDirectory(), "Matrix - outputDirectory is not a directory");

		// List all .hdfs files in connectorDirectory
		final File[] hdfFiles = connectorDirectory
				.listFiles((dir, name) -> name.toLowerCase().endsWith(HDFS_EXTENSION));

		Assert.state(null != hdfFiles, "Matrix - hdfFiles cannot be null. Something went wrong");

		int count = 0;
		for (final File hdf : hdfFiles) {

			final Optional<Connector> optionalConnector = parse(hdf);

			if (!optionalConnector.isPresent()) {
				// Should never occur
				logger.error(String.format("Matrix - Received an empty connector for HDF file %s", hdf.getName()));
				continue;
			}

			// Serialize
			try {
				serialize(logger, outputDirectory, optionalConnector.get());
				count++;
			} catch (IOException e) {
				logger.error(String.format("Matrix - Cannot serialize connector %s ",
						optionalConnector.get().getCompiledFilename()), e);
			}
		}

		return count;

	}

	/**
	 * Serialize the given <code>connector</code> under the <code>outputDirectory</code>
	 * @param logger
	 * @param outputDirectory
	 * @param connector
	 * @throws IOException
	 */
	private void serialize(final Log logger, final File outputDirectory, final Connector connector)
			throws IOException {

		ConnectorSerializer.serialize(outputDirectory.getAbsolutePath(), connector);
		logger.info(String.format("Matrix - Compiled %s to %s", connector.getCompiledFilename(),
				outputDirectory.getAbsolutePath()));
	}

	/**
	 * Simply parse the given <code>hdf</code> file
	 * @param hdf
	 * @return {@link Optional} {@link Connector} object
	 */
	private Optional<Connector> parse(final File hdf) {
		return new ConnectorParser(hdf.getAbsolutePath()).parse();
	}

}
