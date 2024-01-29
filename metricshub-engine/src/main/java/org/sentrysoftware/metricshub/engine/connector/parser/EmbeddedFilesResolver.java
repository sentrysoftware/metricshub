package org.sentrysoftware.metricshub.engine.connector.parser;

/*-
 * ╱╲╱╲╱╲╱╲╱╲╱╲╱╲╱╲╱╲╱╲╱╲╱╲╱╲╱╲╱╲╱╲╱╲╱╲╱╲╱╲
 * MetricsHub Engine
 * ჻჻჻჻჻჻
 * Copyright 2023 - 2024 Sentry Software
 * ჻჻჻჻჻჻
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Affero General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU Affero General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 * ╲╱╲╱╲╱╲╱╲╱╲╱╲╱╲╱╲╱╲╱╲╱╲╱╲╱╲╱╲╱╲╱╲╱╲╱╲╱╲╱
 */

import static org.sentrysoftware.metricshub.engine.common.helpers.MetricsHubConstants.CANT_FIND_EMBEDDED_FILE;
import static org.sentrysoftware.metricshub.engine.common.helpers.MetricsHubConstants.FILE_PATTERN;

import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.core.JsonToken;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.node.ArrayNode;
import com.fasterxml.jackson.databind.node.ObjectNode;
import com.fasterxml.jackson.databind.node.TextNode;
import java.io.File;
import java.io.IOException;
import java.net.URI;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.function.Predicate;
import java.util.function.UnaryOperator;
import java.util.regex.Matcher;
import java.util.zip.ZipEntry;
import org.sentrysoftware.metricshub.engine.strategy.utils.EmbeddedFileHelper;

/**
 * Resolves and internalizes embedded files within a JsonNode.
 */
public class EmbeddedFilesResolver {

	private final JsonNode connector;
	private final Path connectorDirectory;
	private final Set<URI> parents;
	private final Map<String, URI> alreadyProcessedEmbeddedFiles;

	/**
	 * Constructs an EmbeddedFilesResolver with the given parameters.
	 *
	 * @param connector           The JsonNode representing the connector.
	 * @param connectorDirectory  The directory of the connector.
	 * @param parents             Set of parent directories URIs.
	 */
	public EmbeddedFilesResolver(final JsonNode connector, final Path connectorDirectory, final Set<URI> parents) {
		this.connector = connector;
		this.connectorDirectory = connectorDirectory;
		this.parents = parents;
		alreadyProcessedEmbeddedFiles = new HashMap<>();
	}

	/**
	 * Look for all references of embedded files that look like: $file("...")$,
	 * find the referenced file, add its content in a new node at the end of the connector
	 * and replace the reference to the external file by a reference to the internalized embedded file
	 * @throws IOException If there is an issue finding the embedded file or processing the JSON structure.
	 */
	public void internalize() throws IOException {
		final JsonParser jsonParser = connector.traverse();
		JsonToken token = jsonParser.nextToken();

		while (token != null) {
			final String currentValue = jsonParser.getValueAsString();

			if (currentValue == null) {
				token = jsonParser.nextToken();
				continue;
			}

			final Matcher fileMatcher = FILE_PATTERN.matcher(currentValue);
			while (fileMatcher.find()) {
				final String fileName = fileMatcher.group(1);

				if (!alreadyProcessedEmbeddedFiles.containsKey(fileName)) {
					final URI fileUri = findAbsoluteUri(fileName, connectorDirectory);

					if (fileUri == null) {
						throw new IOException(CANT_FIND_EMBEDDED_FILE + fileName);
					}
					alreadyProcessedEmbeddedFiles.put(fileName, fileUri);
				}
			}

			token = jsonParser.nextToken();
		}

		// If there were no embedded files to process, no need to continue
		if (alreadyProcessedEmbeddedFiles.isEmpty()) {
			return;
		}

		// Traverse the connector node and replace embedded files references
		// E.g. ${file::file-1} becomes ${file::file-absolute-path-1} // NOSONAR on comment
		replacePlaceholderValues(connector, this::performFileRefReplacements, value -> value.indexOf("${file::") != -1);
	}

	/**
	 * Find the absolute URI of the file in parameter
	 * @param fileName           The name or relative path of the file
	 * @param connectorDirectory The name of the connector directory where to look for the file
	 * @return The absolute path of the file if found, null otherwise.
	 * @throws IOException when the file can't be found
	 */
	public URI findAbsoluteUri(final String fileName, final Path connectorDirectory) throws IOException {
		// Let's check if the file exists.
		final Path filePath = connectorDirectory.resolve(fileName).normalize();
		String strPath = filePath.toString();

		// If the file is not in a zip, then checking its existence is easy
		if (filePath.toFile().exists()) {
			return filePath.toUri();
		}

		// If the file is in a zip, it gets trickier
		ZipEntry zipEntry = EmbeddedFileHelper.findZipEntry(strPath);

		if (zipEntry != null) {
			return filePath.toUri();
		}

		// If the file doesn't exist in the connectors's directory, lets check the parents
		final Iterator<URI> iterator = parents.iterator();

		while (iterator.hasNext()) {
			Path path = new File(iterator.next()).toPath().resolve(fileName).normalize();

			// We do the same checks with the parents
			if (path.toFile().exists()) {
				return path.toUri();
			}

			zipEntry = EmbeddedFileHelper.findZipEntry(path.toString());
			if (zipEntry != null) {
				return path.toUri();
			}
		}

		// If the file can't be found, return null
		return null;
	}

	/**
	 * Traverse the given node and replace values.
	 *
	 * @param node                The {@link JsonNode} instance to traverse.
	 * @param transformer         The value transformer function.
	 * @param replacementPredicate The replacement predicate.
	 */
	public static void replacePlaceholderValues(
		final JsonNode node,
		final UnaryOperator<String> transformer,
		final Predicate<String> replacementPredicate
	) {
		if (node.isObject()) {
			// Get JsonNode fields
			final List<String> fieldNames = new ArrayList<>(node.size());
			node.fieldNames().forEachRemaining(fieldNames::add);

			// Get the corresponding JsonNode for each field
			for (final String fieldName : fieldNames) {
				final JsonNode child = node.get(fieldName);

				// Means it wrap sub JsonNode(s)
				if (child.isContainerNode()) {
					replacePlaceholderValues(child, transformer, replacementPredicate);
				} else {
					// Perform the replacement
					final String oldValue = child.asText();
					// No need to transform value if it doesn't have the placeholder
					replaceJsonNode(
						() -> ((ObjectNode) node).set(fieldName, new TextNode(transformer.apply(oldValue))),
						oldValue,
						replacementPredicate
					);
				}
			}
		} else if (node.isArray()) {
			// Loop over the array and get each JsonNode element
			for (int i = 0; i < node.size(); i++) {
				final JsonNode child = node.get(i);

				// Means this node is a JsonNode element
				if (child.isContainerNode()) {
					replacePlaceholderValues(child, transformer, replacementPredicate);
				} else {
					// Means this is a simple array node
					final String oldValue = child.asText();
					// No need to transform value if it doesn't have the placeholder
					final int index = i;
					replaceJsonNode(
						() -> ((ArrayNode) node).set(index, new TextNode(transformer.apply(oldValue))),
						oldValue,
						replacementPredicate
					);
				}
			}
		}
	}

	/**
	 * Replace oldValue in {@link JsonNode} only if this oldValue matches the replacement predicate.
	 *
	 * @param replacer            The runnable to perform the replacement.
	 * @param oldValue            The old value to check for replacement.
	 * @param replacementPredicate The replacement predicate.
	 */
	private static void replaceJsonNode(Runnable replacer, String oldValue, Predicate<String> replacementPredicate) {
		if (replacementPredicate.test(oldValue)) {
			replacer.run();
		}
	}

	/**
	 * Perform replacements on the given value using the key-value pairs
	 * provided in the replacements {@link Map}.
	 *
	 * @param value to replace
	 * @return new {@link String} value
	 */
	private String performFileRefReplacements(String value) {
		if (value == null || value.isEmpty()) {
			return value;
		}

		// No need to transform value if it doesn't have the placeholder
		final Matcher matcher = FILE_PATTERN.matcher(value);
		while (matcher.find()) {
			value = replaceFileReference(matcher, value);
		}

		return value;
	}

	/**
	 * Replace the file reference with the corresponding reference located in
	 *
	 * @param matcher The matcher for the file reference.
	 * @param value   The original value containing the file reference.
	 * @return The modified value with the file reference replaced.
	 */
	private String replaceFileReference(final Matcher matcher, final String value) {
		final String replacement = alreadyProcessedEmbeddedFiles.get(matcher.group(1)).getPath();
		return value.replace(matcher.group(1), replacement);
	}
}
