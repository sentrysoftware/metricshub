package com.sentrysoftware.matrix.connector;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.ObjectOutputStream;
import java.nio.file.Files;
import java.nio.file.LinkOption;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.List;
import java.util.Objects;
import java.util.stream.Collectors;
import java.util.stream.Stream;

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
public class ConnectorLibrarySerializer {

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

		serializeConnectorSources(Paths.get(args[0]), Paths.get(args[1]));

	}

	/**
	 * Get all .hdfs files from the connectorDirectory and serialize them using
	 * ConnectorParser
	 *
	 * @param sourceDirectory Directory with the source connectors (*.hdfs files)
	 * @param outputDirectory Directory where to store serialized connectors
	 * @throws ConnectorSerializationException when anything wrong happens (so this interrupts the build)
	 */
	static void serializeConnectorSources(final Path sourceDirectory, final Path outputDirectory) {

		if (!Files.isDirectory(sourceDirectory)) {
			throw new ConnectorSerializationException("sourceDirectory must be a directory");
		}
		validateOutputDirectory(outputDirectory);

		// Get the connectorParser
		final ConnectorParser connectorParser = new ConnectorParser();

		// List all .hdfs files in connectorDirectory
		List<String> connectorNameList;
		try {
			connectorNameList = getConnectorList(sourceDirectory);
		} catch (IOException e) {
			throw new ConnectorSerializationException("Failed to list directory: " + sourceDirectory.toString(), e);
		}

		int count = 0;
		for (final String connectorName : connectorNameList) {

			// Full path of the connector
			Path connectorPath = sourceDirectory.resolve(connectorName);
			Path serializePath = outputDirectory.resolve(ConnectorParser.normalizeConnectorName(connectorName));

			// Is the connector source more recent that the serialized one (if if exists)?
			if (getLastModifiedTime(connectorPath) < getLastModifiedTime(serializePath)) {
				// In which case, skip
				continue;
			}

			System.out.format("Parsing %s%n", connectorName);

			// Parse
			final Connector connector = connectorParser.parse(connectorPath.toString());

			// Serialize
			try (ObjectOutputStream objectOutputStream = new ObjectOutputStream(new FileOutputStream(serializePath.toFile()))) {
				objectOutputStream.writeObject(connector);
			} catch (IOException e) {
				throw new ConnectorSerializationException("Failed to serialize connector: " + connectorName, e);
			}

			count++;
		}

		System.out.format("Successfully parsed and serialized %d connectors%n", count);

	}

	/**
	 * Make sure the specified directory exists and create it if it doesn't.
	 * <p>
	 * @param dir Directory to be tested
	 * @throws ConnectorSerializationException if specified directory cannot be created
	 * @throws IllegalArgumentException if specified directory is null
	 */
	public static void validateOutputDirectory(@NonNull final Path dir) {

		// If it already exists
		if (Files.exists(dir)) {
			// But it's not a directory
			if (!Files.isDirectory(dir)) {
				// Throw an exception
				throw new ConnectorSerializationException("outputDirectory " + dir.toString() + " must be a directory, not a file");
			}
			// Or else do nothing
			return;
		}

		// Create it!
		try {
			Files.createDirectories(dir);
		} catch (IOException e) {
			throw new ConnectorSerializationException("Could not create outputDirectory: " + dir.toString());
		}

	}

	/**
	 * Get the list of connector source files in the specified directory
	 * <p>
	 * @param sourceDirectory The directory to search for files
	 * @return List of matching {@link File} objects
	 * @throws IOException on directory listing issues
	 * @throws IllegalArgumentException if specified directory is null
	 *
	 */
	public static List<String> getConnectorList(@NonNull final Path sourceDirectory) throws IOException {

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


	/**
	 * Returns the time of last modification of specified Path in milliseconds since
	 * EPOCH.
	 *
	 * @param path Path to the file
	 * @return Milliseconds since EPOCH, or 0 (zero) if file does not exist
	 * @throws IllegalArgumentException if specified path is null
	 */
	public static long getLastModifiedTime(@NonNull Path path) {

		try {
			return Files.getLastModifiedTime(path, LinkOption.NOFOLLOW_LINKS).toMillis();
		} catch (IOException e) {
			return 0;
		}
	}



}
