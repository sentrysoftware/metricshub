package com.sentrysoftware.matrix.converter;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import com.fasterxml.jackson.databind.JsonNode;
import com.sentrysoftware.matrix.connector.parser.ConstantsProcessor;
import com.sentrysoftware.matrix.converter.state.ConversionHelper;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NonNull;

@AllArgsConstructor
@Builder(setterPrefix = "with")
public class EmbeddedFileExternalizer {

	/**
	 * A compiled representation of an EmbeddedFile reference regular expression.
	 * We attempt to match input like "$embedded.EmbeddedFile(1)$"
	 */
	private static final Pattern EMBEDDED_FILE_PATTERN = Pattern.compile(
		"\\$embedded\\.embeddedfile\\(([^\\s]+)\\)\\$",
		Pattern.CASE_INSENSITIVE
	);

	private Map<String, String> embeddedFiles;

	@NonNull
	@Getter
	private Path connectorDirectory;

	@NonNull
	private JsonNode connector;

	@NonNull
	private Set<String> parents;

	/**
	 * Externalize the embedded files then traverse the connector
	 * and replace all the references of the embedded files.
	 * 
	 * @throws IOException
	 */
	public void externalize() throws IOException {
		if (embeddedFiles == null || embeddedFiles.isEmpty()) {
			return;
		}

		// Externalize each embedded file
		for (Entry<String, String> embeddedFileEntry : embeddedFiles.entrySet()) {
			extractEmbeddedFile(embeddedFileEntry.getKey(), embeddedFileEntry.getValue());
		}

		// Traverse the connector node and replace embedded files references
		// E.g. $embedded.EmbeddedFile(1)$ becomes $file("embeddedFile-1")$
		ConstantsProcessor
			.replacePlaceholderValues(
				connector,
				this::transform,
				value -> value.toLowerCase().indexOf("$embedded.embeddedfile") != -1
		);
	}

	/**
	 * If the value matches the embedded file pattern
	 * <code>$embedded.EmbeddedFile(\\d+)$</code>, replace occurrences with
	 * <code>$file("embedded_file_relative_path")$</code>. E.g.
	 * <code>$file("embeddedFile-1")$</code> or
	 * <code>$file("../Connector/embeddedFile-1")$</code>
	 * 
	 * @param value value to transform (input)
	 * @return transformed value as String
	 */
	private String transform(String value) {
		final Matcher matcher = EMBEDDED_FILE_PATTERN.matcher(value);

		while (matcher.find()) {
			value = doReplacement(value, matcher);
		}
		return value;
	}

	/**
	 * Find the embedded file relative path and replace the matching group provided by the matcher.
	 * @param input   Input to replace
	 * @param matcher An engine that performs match operations on a character sequence.<br>
	 *                <code>Matcher.find</code> or <code>Matcher.matches</code> must be called before
	 *                calling this method 
	 * @return String value
	 */
	private String doReplacement(final String input, final Matcher matcher) {

		// Build the new name
		final String embeddedFileName = String.format("embeddedFile-%s", matcher.group(1));

		// Find the path relative to the current connector directory
		final String path = findRelativePath(embeddedFileName);

		// Do replacement
		return input.replace(
			matcher.group(),
			String.format("$file(\"%s\")$", path)
		);
	}

	/**
	 * Find the path relative to the connector directory for the given embedded
	 * file
	 * 
	 * @param embeddedFileName The name of the embedded file we are looking for.
	 */
	private String findRelativePath(final String embeddedFileName) {
		// Check in the current directory
		final Path path = connectorDirectory.resolve(embeddedFileName);

		// The embedded file is in the current connector directory
		if (Files.exists(path)) {
			return embeddedFileName;
		}

		// Find the embedded file from the parent connectors
		for (String parent : parents) {
			parent = ConnectorLibraryConverter.getConnectorFilenameNoExtension(parent);

			final Path parentPath = connectorDirectory.getParent().resolve(parent);

			if (Files.exists(parentPath.resolve(embeddedFileName))) {
				return String.format("../%s/%s", parent, embeddedFileName);
			}
		}

		// Probably declared in HHDF and available in HDFS
		return embeddedFileName;
	}

	/**
	 * Extract the embedded file content into an external file.
	 * 
	 * @param originalName The original name of the embedded file. E.g. EmbeddedFile(1)
	 * @param content The content of the embedded file.
	 * @throws IOException
	 */
	private void extractEmbeddedFile(final String originalName, final String content) throws IOException {

		// Perform HDF to YAML value conversions to make sure that any HDF reference is correctly 
		// converted to the YAML expected reference
		final String updatedContent = ConversionHelper.performValueConversions(content);

		// Build the final name
		final String finalName = originalName
			.toLowerCase()
			.replace("embeddedfile(", "embeddedFile-")
			.replace(")", "");

		// Write the content to the file
		Files.writeString(connectorDirectory.resolve(finalName), updatedContent);
	}

}
