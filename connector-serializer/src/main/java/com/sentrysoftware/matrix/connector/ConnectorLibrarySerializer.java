package com.sentrysoftware.matrix.connector;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.sentrysoftware.matrix.common.helpers.FileHelper;
import com.sentrysoftware.matrix.common.helpers.JsonHelper;
import com.sentrysoftware.matrix.connector.model.Connector;
import com.sentrysoftware.matrix.connector.parser.ConnectorParser;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.ObjectOutputStream;
import java.io.PrintStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.List;
import java.util.Locale;
import java.util.Objects;
import java.util.stream.Collectors;
import java.util.stream.Stream;
import lombok.NonNull;

/**
 * Class designed to be called by the <b>exec-maven-plugin</b>
 * <p>
 * The <i>main(...)</i> method will parse all connector source files
 * and serialize them in a target directory.
 */
public class ConnectorLibrarySerializer {

	private static final ObjectMapper MAPPER = JsonHelper.buildYamlMapper();
	private static final PrintStream ERR = System.err; // NOSONAR
	private static final PrintStream OUT = System.out; // NOSONAR

	private static final String[] CONNECTOR_SOURCE_KEY_PATH = new String[] { "connector", "displayName" };

	static {
		Locale.setDefault(Locale.US);
	}

	/**
	 * Main method to be called by the exec-maven-plugin.
	 * <p>
	 * Arguments are expected to be:
	 * <ol>
	 * <li>Path to the source directory (with the *.yaml files)
	 * <li>Path to the target directory where to store serialized files
	 * </ol>
	 * @param args sourceDirectory and targetDirectory as an array of String
	 */
	public static void main(String[] args) {
		serializeConnectorSources(Paths.get(args[0]), Paths.get(args[1]));
	}

	/**
	 * Get all .yaml files from the connectorDirectory and serialize them using
	 * ConnectorParser
	 *
	 * @param sourceDirectory Directory with the source connectors (*.yaml files)
	 * @param outputDirectory Directory where to store serialized connectors
	 * @throws ConnectorSerializationException when anything wrong happens (so this interrupts the build)
	 */
	static void serializeConnectorSources(final Path sourceDirectory, final Path outputDirectory) {
		if (!Files.isDirectory(sourceDirectory)) {
			throw new ConnectorSerializationException("sourceDirectory must be a directory");
		}
		validateOutputDirectory(outputDirectory);

		// Get the connectorParser
		final ConnectorParser connectorParser = ConnectorParser.withNodeProcessorAndUpdateChain(sourceDirectory);

		// List all .yaml files in connectorDirectory
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

			// Remove the extension and resolve the future serialized file path
			Path serializePath = outputDirectory.resolve(connectorName.substring(0, connectorName.lastIndexOf('.')));

			// Is the connector source more recent that the serialized one (if if exists)?
			if (FileHelper.getLastModifiedTime(connectorPath) < FileHelper.getLastModifiedTime(serializePath)) {
				// In which case, skip
				continue;
			}

			OUT.format("Parsing %s%n", connectorName);

			// Parse
			Connector connector;
			try {
				connector = connectorParser.parse(connectorPath.toFile());
			} catch (IOException e) {
				throw new ConnectorSerializationException("Failed to parse connector: " + connectorName, e);
			}

			// Serialize
			try (
				ObjectOutputStream objectOutputStream = new ObjectOutputStream(new FileOutputStream(serializePath.toFile()))
			) {
				objectOutputStream.writeObject(connector);
			} catch (IOException e) {
				throw new ConnectorSerializationException("Failed to serialize connector: " + connectorName, e);
			}

			count++;
		}

		if (count > 0) {
			OUT.format("Successfully parsed and serialized %d connector%s.%n", count, count > 1 ? "s" : "");
		} else {
			OUT.println("No connector to parse.");
		}
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
				throw new ConnectorSerializationException(
					"outputDirectory " + dir.toString() + " must be a directory, not a file"
				);
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
				.filter(path -> path.toString().endsWith(".yaml"))
				.filter(ConnectorLibrarySerializer::isConnectorSource)
				.map(path -> path.getFileName().toString())
				.sorted(String::compareToIgnoreCase)
				.collect(Collectors.toList()); //NOSONAR
		}
	}

	/**
	 * Check if the given file path is a YAML connector source
	 *
	 * @param path
	 * @return boolean value
	 */
	private static boolean isConnectorSource(Path path) {
		final File yamlFile = path.toFile();
		try {
			return keyValuePairExistsByKeys(CONNECTOR_SOURCE_KEY_PATH, MAPPER.readTree(yamlFile));
		} catch (Exception e) {
			ERR.format("Cannot parse YAML file %s.", yamlFile.toString());
			e.printStackTrace();
			return false;
		}
	}

	/**
	 * Check if the keys provided in the given array have values in the {@link JsonNode}
	 *
	 * @param keys
	 * @param jsonNode
	 * @return boolean value
	 */
	public static boolean keyValuePairExistsByKeys(final String[] keys, final JsonNode jsonNode) {
		if (jsonNode == null) {
			return false;
		}

		JsonNode node = jsonNode;

		for (String key : keys) {
			node = node.get(key);

			if (node == null) {
				return false;
			}
		}

		return !node.isMissingNode();
	}
}
