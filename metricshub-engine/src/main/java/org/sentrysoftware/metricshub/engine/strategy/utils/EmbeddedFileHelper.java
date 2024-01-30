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
import java.net.URI;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.regex.Matcher;
import lombok.AccessLevel;
import lombok.NoArgsConstructor;
import lombok.NonNull;
import org.sentrysoftware.metricshub.engine.common.helpers.FileHelper;
import org.sentrysoftware.metricshub.engine.connector.model.common.EmbeddedFile;

/**
 * The {@code EmbeddedFileHelper} class provides utility methods for handling embedded files in various contexts, such as
 * command lines, AWK directives, headers, and bodies. It allows finding and processing file references in a given string.
 * The class is designed to have a private no-argument constructor to prevent instantiation.
 */
@NoArgsConstructor(access = AccessLevel.PRIVATE)
public class EmbeddedFileHelper {

	/**
	 * Finds all the embedded files that are referenced in the given string.
	 *
	 * @param value The value can be a command line, AWK directive, header, body, etc.
	 * @return A map of the file reference to {@link EmbeddedFile} instance.
	 * @throws IOException If an I/O error occurs while processing the embedded files.
	 */
	public static Map<String, EmbeddedFile> findEmbeddedFiles(@NonNull final String value) throws IOException {
		final Map<String, EmbeddedFile> embeddedFiles = new HashMap<>();
		final List<String> alreadyProcessedFiles = new ArrayList<>();

		final Matcher fileMatcher = FILE_PATTERN.matcher(value);

		while (fileMatcher.find()) {
			// The absolute path of the file
			final String fileUri = fileMatcher.group(1);

			// The file reference in the connector. Example: ${file::file-absolute-path} // NOSONAR on comment
			final String fileNameRef = fileMatcher.group();

			// If the embeddedFile has already been processed, no need to continue
			if (!alreadyProcessedFiles.contains(fileNameRef)) {
				embeddedFiles.put(fileNameRef, runNewEmbeddedFileObjectTask(URI.create(fileUri), fileNameRef));
				alreadyProcessedFiles.add(fileNameRef);
			}
		}

		return embeddedFiles;
	}

	/**
	 * Runs a task to create a new instance of an EmbeddedFileObject based on the specified URI and file name reference.
	 * If the URI scheme is "jar," the task is executed within a file system context for JAR files (ZIP).
	 *
	 * @param fileUri      The URI of the file for which the EmbeddedFileObject is created.
	 * @param fileNameRef  The reference to the file name.
	 * @return A new instance of EmbeddedFileObject representing the specified file.
	 * @throws IOException If an error occurs during the task execution or while creating the EmbeddedFileObject.
	 */
	private static EmbeddedFile runNewEmbeddedFileObjectTask(final URI fileUri, final String fileNameRef)
		throws IOException {
		if ("jar".equals(fileUri.getScheme())) {
			try {
				return FileHelper.fileSystemTask(
					fileUri,
					Collections.emptyMap(),
					() -> {
						try {
							return newEmbeddedFileObject(fileUri, fileNameRef);
						} catch (IOException e) {
							throw new IllegalStateException(e);
						}
					}
				);
			} catch (Exception e) {
				if (e instanceof IOException ioException) {
					throw ioException;
				}

				if (e.getCause() instanceof IOException ioException) {
					throw ioException;
				}

				throw new IOException(e);
			}
		}
		return newEmbeddedFileObject(fileUri, fileNameRef);
	}

	/**
	 * Create a new {@link EmbeddedFile} object
	 *
	 * @param fileName    The file name used to get the path
	 * @param fileNameRef The file name reference. E.g. ${file::script.awk}
	 * @return a new {@link EmbeddedFile} instance
	 * @throws IOException If the file cannot be found or parsed.
	 */
	private static EmbeddedFile newEmbeddedFileObject(final URI fileUri, final String fileNameRef) throws IOException {
		final Path filePath = Paths.get(fileUri);
		if (!Files.exists(filePath)) {
			throw new IOException(CANT_FIND_EMBEDDED_FILE + fileUri.getPath());
		}
		return new EmbeddedFile(parseEmbeddedFile(filePath), findExtension(fileUri.toString()), fileNameRef);
	}

	/**
	 * Returns the extension of a file from the given the file name.
	 *
	 * @param uriString URI string representation.
	 * @return File extension as a string.
	 */
	static String findExtension(final String uriString) {
		final int index = uriString.lastIndexOf('.');
		final int separatorIndex = Math.max(uriString.lastIndexOf('/'), uriString.lastIndexOf('\\'));
		// We want to find the index of the last '.' only if it's in the last file of the path
		// in case the file is in an .zip archive (or any kind of archive)
		if (index > separatorIndex) {
			return uriString.substring(index + 1);
		}
		return null;
	}

	/**
	 * Parse an embedded file located in a .zip file given its URI.
	 *
	 * @param filePath The path of the file we want to parse.
	 * @return The content of the file.
	 * @throws IOException When an I/O error occurs opening the file.
	 */
	private static String parseEmbeddedFile(final Path filePath) throws IOException {
		return FileHelper.readFileContent(filePath);
	}
}
