package com.sentrysoftware.matrix.converter;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;
import java.util.Objects;
import java.util.regex.Pattern;
import java.util.stream.Stream;

import com.fasterxml.jackson.core.util.DefaultPrettyPrinter;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.ObjectWriter;
import com.sentrysoftware.matrix.common.helpers.FileHelper;
import com.sentrysoftware.matrix.common.helpers.JsonHelper;
import com.sentrysoftware.matrix.converter.exception.ConnectorConverterException;

import lombok.Data;
import lombok.NonNull;
import lombok.RequiredArgsConstructor;

@RequiredArgsConstructor
@Data
public class ConnectorLibraryConverter {

	/**
	 * Pattern to remove MS_HW_ from the compiled file name
	 */
	public static final Pattern REMOVE_MS_HW_PATTERN = Pattern.compile("^(MS_HW_)(.*)$", Pattern.CASE_INSENSITIVE);

	@NonNull
	private Path sourceDirectory;

	@NonNull
	private Path outputDirectory;

	/**
	 * Get all .hdfs and .hhdf files from the connectorDirectory and process them using
	 * ConnectorConverter to build .yaml file
	 * @throws IOException 
	 *
	 * @throws ConnectorConverterException when anything wrong happens (so this interrupts the build)
	 */
	void process() throws IOException {
		if (!Files.isDirectory(sourceDirectory)) {
			throw new ConnectorConverterException("sourceDirectory must be a directory");
		}
		validateOutputDirectory(outputDirectory);

		// List all .yaml files in connectorDirectory
		final List<String> connectorNameList;
		try {
			connectorNameList = getConnectorList(sourceDirectory);
		} catch (IOException e) {
			throw new ConnectorConverterException("Failed to list directory: " + sourceDirectory.toString(), e);
		}

		for (final String connectorName : connectorNameList) {

			// Full path of the connector
			Path connectorPath = sourceDirectory.resolve(connectorName);

			// Remove the extension and resolve the future serialized file path (YAML)
			Path serializePath = outputDirectory.resolve(getConnectorFilename(connectorName));

			// Is the connector source more recent that the serialized one (if if exists)?
			if (FileHelper.getLastModifiedTime(connectorPath) < FileHelper.getLastModifiedTime(serializePath)) {
				// In which case, skip
				continue;
			}

			// Create the PreConnector
			final PreConnector preConnector = new PreConnector();

			// Load PreConnector (code, constants, embedded files, extends, ...)
			preConnector.load(connectorPath.toAbsolutePath().toString());

			// Instantiate a new converter in order to convert the .HDFS or .HHDF to a YAML file
			final ConnectorConverter converter = new ConnectorConverter(preConnector);

			// Convert the .HDFS or .HHDF to a YAML file
			final JsonNode connector = converter.convert();

			final ObjectMapper mapper = JsonHelper.buildYamlMapper();
			final ObjectWriter writer = mapper.writer(new DefaultPrettyPrinter());

			writer.writeValue(serializePath.toFile(), connector);
		}

	}

	/**
	 * Remove the extension from the file name and replace MS_HW_ prefix.
	 * Add -header as file suffix for .hhdf files
	 *
	 * @param filename
	 * @return String value
	 */
	public static String getConnectorFilenameNoExtension(final String filename) {
		String header = "";
		if (filename.toLowerCase().endsWith(".hhdf")) {
			header = "-header";
		}
		// remove the extension
		String connectorFilename = filename.substring(0, filename.lastIndexOf('.'));

		// remove MS_HW_
		String baseName = REMOVE_MS_HW_PATTERN.matcher(connectorFilename).replaceFirst("$2");

		// Build final name "%name%-header" or %name%
		return String.format("%s%s", baseName, header);
	}

	/**
	 * Remove the extension from the file name and replace MS_HW_ prefix
	 * Add -header as file suffix for .hhdf files.
	 * Add the .yaml extension
	 * 
	 * @param filename
	 * @return String value
	 */
	public static String getConnectorFilename(final String filename) {
		// Build final name "%name%.yaml
		return String.format("%s.yaml", getConnectorFilenameNoExtension(filename));
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
				throw new ConnectorConverterException("outputDirectory " + dir.toString() + " must be a directory, not a file");
			}
			// Or else do nothing
			return;
		}

		// Create it!
		try {
			Files.createDirectories(dir);
		} catch (IOException e) {
			throw new ConnectorConverterException("Could not create outputDirectory: " + dir.toString());
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
				.filter(ConnectorLibraryConverter::isSource)
				.sorted((c1, c2) -> c1.compareToIgnoreCase(c2))
				.toList();
		}
	}

	/**
	 * Whether the connector is HDFS or HHDF
	 * 
	 * @param name
	 * @return boolean value
	 */
	private static boolean isSource(String name) {
		String lowerCaseName = name.toLowerCase();
		return lowerCaseName.endsWith(".hdfs") || lowerCaseName.endsWith(".hhdf");
	}

}
