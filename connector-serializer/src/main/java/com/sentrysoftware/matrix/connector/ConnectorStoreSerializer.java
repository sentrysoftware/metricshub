package com.sentrysoftware.matrix.connector;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.ObjectOutputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.List;
import java.util.Objects;
import java.util.Optional;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import org.springframework.util.Assert;

import com.sentrysoftware.matrix.connector.model.Connector;
import com.sentrysoftware.matrix.connector.parser.ConnectorParser;

import lombok.NonNull;

public class ConnectorStoreSerializer {

	public static void main(String[] args) {

		compileHdfsFiles(Paths.get(args[0]), Paths.get(args[1]));

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
	private static void compileHdfsFiles(final Path connectorDirectory, final Path outputDirectory) {

		Assert.state(Files.isDirectory(connectorDirectory), "connectorDirectory is not a directory");
		validateOutputDirectory(outputDirectory);

		// Get the connectorParser
		final ConnectorParser connectorParser = new ConnectorParser();

		// List all .hdfs files in connectorDirectory
		List<String> connectorNameList;
		try {
			connectorNameList = getConnectorList(connectorDirectory);
		} catch (IOException e) {
			throw new RuntimeException("Failed to list directory: " + connectorDirectory.toString(), e);
		}

		int count = 0;
		for (final String connectorName : connectorNameList) {

			// Full path of the connector
			String connectorPath = connectorDirectory.resolve(connectorName).toString();

			// Parse
			final Optional<Connector> optionalConnector = connectorParser.parse(connectorPath);
			if (optionalConnector.isEmpty()) {
				throw new RuntimeException("Failed to parse connector: " + connectorName);
			}

			// Serialize
			String serializePath = outputDirectory.resolve(optionalConnector.get().getCompiledFilename()).toString();
			try (ObjectOutputStream objectOutputStream = new ObjectOutputStream(new FileOutputStream(serializePath))) {
				objectOutputStream.writeObject(optionalConnector.get());
			} catch (IOException e) {
				throw new RuntimeException("Failed to serialize connector: " + connectorName, e);
			}

			System.out.format("Serialized %s%n", connectorName);
			count++;
		}

		System.out.format("Successfully serialized %d connectors%n", count);

	}

	/**
	 * Make sure the specified directory exists and create it if it doesn't.
	 * <p>
	 * @param dir Directory to be tested
	 * @throws MojoExecutionException if specified directory cannot be created
	 * @throws IllegalArgumentException if specified directory is null
	 */
	public static void validateOutputDirectory(@NonNull Path dir) {

		// Do we need to create it?
		if (!Files.exists(dir)) {

			try {
				// Create it!
				Files.createDirectories(dir);
			} catch (IOException e) {
				throw new RuntimeException("Could not create outputDirectory: " + dir.toString());
			}

		}

	}

	/**
	 * Get the list of connector source files in the specified directory
	 * <p>
	 * @param sourceDirectory The directory to search for files
	 * @return List of matching {@link File} objects
	 * @throws IOException on directory listing issues
	 */
	public static List<String> getConnectorList(final Path sourceDirectory) throws IOException {

		try (Stream<Path> fileStream = Files.list(sourceDirectory)) {
			return fileStream
					.filter(Objects::nonNull)
					.filter(path -> !Files.isDirectory(path))
					.map(path -> path.getFileName().toString())
					.filter(name -> name.toLowerCase().endsWith(".hdfs"))
					.sorted((c1, c2) -> c1.compareToIgnoreCase(c2))
					.collect(Collectors.toUnmodifiableList());
		}
	}


}
