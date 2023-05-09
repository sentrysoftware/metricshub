package com.sentrysoftware.matrix.converter;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Objects;
import java.util.regex.Pattern;
import java.util.stream.Stream;

import com.fasterxml.jackson.dataformat.yaml.YAMLGenerator.Feature;
import com.fasterxml.jackson.core.util.DefaultPrettyPrinter;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.ObjectWriter;
import com.fasterxml.jackson.dataformat.yaml.YAMLFactory;
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
	 * Get all .hdfs and .hhdf files from the connectorDirectory and process them
	 * using
	 * ConnectorConverter to build .yaml file
	 * 
	 * @throws IOException
	 *
	 * @throws ConnectorConverterException when anything wrong happens (so this
	 *                                     interrupts the build)
	 */
	void process() throws IOException {
		if (!Files.isDirectory(sourceDirectory)) {
			throw new ConnectorConverterException("sourceDirectory must be a directory");
		}
		validateOutputDirectory(outputDirectory);

		// List all .hdfs and .hhdf files in connectorDirectory
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

			// Is the connector source more recent that the serialized one (if it exists)?
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
			final YAMLFactory factory = (YAMLFactory) mapper.getFactory();

			// For string containing newlines, the literal block styles is used
			factory.configure(Feature.LITERAL_BLOCK_STYLE, true);

			final ObjectWriter writer = mapper.writer(new DefaultPrettyPrinter());

			writer.writeValue(serializePath.toFile(), connector);

			writeComments(serializePath);
		}
	}

	/**
	 * Reads the yaml files and replaces any comment nodes with real comments
	 * 
	 * @param serializePath
	 * @throws IOException
	 */
	private void writeComments(Path serializePath) throws IOException {

		// READ the yaml as text
		List<String> yamlAsText = Files.readAllLines(serializePath);

		// Use a string builder to reconstruct file line by line
		List<String> yamlWithComments = new ArrayList<>();

		final Iterator<String> iterator = yamlAsText.iterator();

		while (iterator.hasNext()) {
			String maybeComment = iterator.next();

			if (maybeComment.contains("_comment: |-")) {
				treatMultiLineComment(yamlWithComments, iterator, maybeComment);
			} else if (maybeComment.contains("_comment: |2-")) {
				treatMultiLineComment(yamlWithComments, iterator, maybeComment);
			} else if (maybeComment.contains("_comment: ")) {
				treatSingleLineComment(yamlWithComments, iterator, maybeComment);
			} else {
				yamlWithComments.add(maybeComment);
			}
		}

		Files.write(serializePath, yamlWithComments);
	}

	/**
	 * processes a single line comment either
	 * _comment:
	 * or
	 * - _comment:
	 * 
	 * @param yamlWithComments
	 * @param iterator
	 * @param maybeComment
	 */
	private void treatSingleLineComment(List<String> yamlWithComments, final Iterator<String> iterator, String maybeComment) {
		// remove yaml formatting and key and replace with #
		String comment = maybeComment.replace("_comment: ", "# ");
		comment = comment.replace("- #", "#");
		comment = comment.replace("# \"", "# ");
		if(comment.lastIndexOf("\"") == comment.length() -1) {
			comment = comment.substring(0, comment.length() -1);
		}

		yamlWithComments.add(comment);

		String node = iterator.next();

		moveDashToPosition(yamlWithComments, maybeComment, node, "- _comment: ");
	}

	/**
	 * processes multi-line comment either
	 * _comment: |-
	 * or
	 * - _comment: |-
	 * 
	 * @param yamlWithComments
	 * @param iterator
	 * @param commentHeader
	 */
	private void treatMultiLineComment(List<String> yamlWithComments, final Iterator<String> iterator,
			String commentHeader) {
		String maybeComment = null;

		// create store
		List<String> lines = new ArrayList<>();

		final boolean dash = commentHeader.contains("- _comment");

		int nodeDepth = commentHeader.indexOf("_");
		int commentDepth = nodeDepth + 2;

		// we know there's no information on this
		if (iterator.hasNext()) {
			maybeComment = iterator.next();
		} else {
			return;
		}

		while (!maybeComment.isEmpty() && checkHeader(maybeComment.substring(0, commentDepth), commentDepth)) {
			// comment confirmed

			String replacementHeader = " ".repeat(nodeDepth) + "# ";
			// remove yaml formatting and key and replace with #
			lines.add(maybeComment.replace(maybeComment.substring(0, commentDepth), replacementHeader));

			if (iterator.hasNext()) {
				maybeComment = iterator.next();
			}
		}

		String node = maybeComment;

		// more blank lines
		while(node.isEmpty() && iterator.hasNext()){
			node = iterator.next();
		}

		for (String line : lines) {
			yamlWithComments.add(line);
		}

		if (dash) {
			int dashIndex = commentHeader.indexOf("-");
			yamlWithComments.add(node.substring(0, dashIndex) + '-' + node.substring(dashIndex + 1));
		} else {
			yamlWithComments.add(node);
		}

	}

	private void moveDashToPosition(List<String> yamlWithComments, String maybeComment, String node, String s) {
		if (maybeComment.contains(s)) {
			int index = maybeComment.indexOf("-");
			yamlWithComments.add(node.substring(0, index) + '-' + node.substring(index + 1));
		} else {
			yamlWithComments.add(node);
		}
	}

	private boolean checkHeader(String commentHeader, int expected) {
		String expectedSpace = " ".repeat(expected);
		return commentHeader.equals(expectedSpace);
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
	 * 
	 * @param dir Directory to be tested
	 * @throws ConnectorConverterException if specified directory is not a directory
	 *                                     or cannot be created
	 * @throws IllegalArgumentException    if specified directory is null
	 */
	public static void validateOutputDirectory(@NonNull final Path dir) {

		// If it already exists
		if (Files.exists(dir)) {
			// But it's not a directory
			if (!Files.isDirectory(dir)) {
				// Throw an exception
				throw new ConnectorConverterException(
						"outputDirectory " + dir.toString() + " must be a directory, not a file");
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
	 * 
	 * @param sourceDirectory The directory to search for files
	 * @return List of matching {@link File} objects
	 * @throws IOException              on directory listing issues
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
					.sorted(String::compareToIgnoreCase)
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
