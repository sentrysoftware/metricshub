package org.sentrysoftware.metricshub.engine.strategy.utils;

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

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.stream.Collectors;
import lombok.AccessLevel;
import lombok.NoArgsConstructor;
import lombok.NonNull;
import org.sentrysoftware.metricshub.engine.connector.model.common.EmbeddedFile;

@NoArgsConstructor(access = AccessLevel.PRIVATE)
public class EmbeddedFileHelper {

	/**
	 * Find all the embedded files that are referenced in the given string
	 *
	 * @param value the value can be a command line, AWK directive, header, body, etc.
	 * @return Map of the file reference to {@link EmbeddedFile} instance
	 * @throws IOException
	 */
	public static Map<String, EmbeddedFile> findEmbeddedFiles(@NonNull final String value) throws IOException {
		final Map<String, EmbeddedFile> embeddedFiles = new HashMap<>();
		final List<String> alreadyProcessedFiles = new ArrayList<>();

		final Matcher fileMatcher = FILE_PATTERN.matcher(value);

		while (fileMatcher.find()) {
			// The absolute path of the file
			final String fileName = fileMatcher.group(1);

			// The file reference in the connector. Example: ${file::file-absolute-path} // NOSONAR on comment
			final String fileNameRef = fileMatcher.group();

			// If the embeddedFile has already been processed, no need to continue
			if (!alreadyProcessedFiles.contains(fileNameRef)) {
				embeddedFiles.put(fileNameRef, newEmbeddFileObject(fileName, fileNameRef));
				alreadyProcessedFiles.add(fileNameRef);
			}
		}

		return embeddedFiles;
	}

	/**
	 * Create a new {@link EmbeddedFile} object
	 *
	 * @param fileName    The file name used to get the path
	 * @param fileNameRef The file name reference. E.g. ${file::script.awk}
	 * @return a new {@link EmbeddedFile} instance
	 * @throws IOException
	 */
	static EmbeddedFile newEmbeddFileObject(final String fileName, final String fileNameRef) throws IOException {
		final Path filePath = Path.of(fileName);

		if (!Files.exists(filePath)) {
			throw new IOException(CANT_FIND_EMBEDDED_FILE + fileName);
		}
		return new EmbeddedFile(parseEmbeddedFile(filePath), findExtension(fileName), fileNameRef);
	}

	/**
	 * Returns the extension of a file from the given the file name
	 *
	 * @param fileName
	 * @return String value
	 */
	static String findExtension(final String fileName) {
		final int index = fileName.lastIndexOf('.');
		if (index > 0) {
			return fileName.substring(index + 1);
		}
		return null;
	}

	/**
	 * Read the file at the filePath location, and return its content
	 *
	 * @param filePath The absolute path of the file to read
	 * @return String value
	 * @throws IOException
	 */
	static String parseEmbeddedFile(final Path filePath) throws IOException {
		try {
			return Files.readAllLines(filePath).stream().collect(Collectors.joining("\n"));
		} catch (Exception e) {
			throw new IOException("Could not read embedded file: " + filePath);
		}
	}
}
