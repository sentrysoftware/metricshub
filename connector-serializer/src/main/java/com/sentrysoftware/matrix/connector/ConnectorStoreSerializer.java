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

import com.sentrysoftware.matrix.common.exception.ConnectorSerializationException;
import com.sentrysoftware.matrix.connector.model.Connector;
import com.sentrysoftware.matrix.connector.parser.ConnectorParser;

import lombok.NonNull;

/**
 * Class designed to be called by the <b>exec-maven-plugin</b>
 * <p>
 * The <i>main(...)</i> method will parse all connector source files
 * and serialize them in a target directory.
 */
public class ConnectorStoreSerializer {

	/**
	 * Main method to be called by the exec-maven-plugin.
	 * <p>
	 * Arguments are expected to be:
	 * <ol>
	 * <li>Path to the source directory (with the *.hdfs files)
	 * <li>Path to the target directory where to store serialized files
	 * </ol>
	 * @param args sourceDirectory and targetDirectory as an array of String
	 */
	public static void main(String[] args) {

		compileHdfsFiles(Paths.get(args[0]), Paths.get(args[1]));

	}

	/**
	 * Get all .hdfs files from the connectorDirectory and serialize them using
	 * ConnectorParser
	 *
	 * @param connectorDirectory Directory with the source connectors (*.hdfs files)
	 * @param outputDirectory Directory where to store serialized connectors
	 * @throws ConnectorSerializationException when anything wrong happens (so this interrupts the build)
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
			throw new ConnectorSerializationException("Failed to list directory: " + connectorDirectory.toString(), e);
		}

		int count = 0;
		for (final String connectorName : connectorNameList) {

			// Full path of the connector
			String connectorPath = connectorDirectory.resolve(connectorName).toString();

			// Parse
			final Optional<Connector> optionalConnector = connectorParser.parse(connectorPath);
			if (optionalConnector.isEmpty()) {
				throw new ConnectorSerializationException("Failed to parse connector: " + connectorName);
			}

			// Serialize
			String serializePath = outputDirectory.resolve(optionalConnector.get().getCompiledFilename()).toString();
			try (ObjectOutputStream objectOutputStream = new ObjectOutputStream(new FileOutputStream(serializePath))) {
				objectOutputStream.writeObject(optionalConnector.get());
			} catch (IOException e) {
				throw new ConnectorSerializationException("Failed to serialize connector: " + connectorName, e);
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
	 * @throws ConnectorSerializationException if specified directory cannot be created
	 * @throws IllegalArgumentException if specified directory is null
	 */
	public static void validateOutputDirectory(@NonNull Path dir) {

		// Do we need to create it?
		if (!Files.isDirectory(dir)) {

			try {
				// Create it!
				Files.createDirectories(dir);
			} catch (IOException e) {
				throw new ConnectorSerializationException("Could not create outputDirectory: " + dir.toString());
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
