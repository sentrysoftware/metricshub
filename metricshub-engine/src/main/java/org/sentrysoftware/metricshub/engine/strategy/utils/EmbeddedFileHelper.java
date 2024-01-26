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
import static org.sentrysoftware.metricshub.engine.common.helpers.MetricsHubConstants.NEW_LINE;
import static org.sentrysoftware.metricshub.engine.common.helpers.MetricsHubConstants.ZIP;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.net.URI;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Scanner;
import java.util.regex.Matcher;
import java.util.zip.ZipEntry;
import java.util.zip.ZipFile;
import lombok.AccessLevel;
import lombok.NoArgsConstructor;
import lombok.NonNull;
import lombok.extern.slf4j.Slf4j;
import org.sentrysoftware.metricshub.engine.connector.model.common.EmbeddedFile;

@Slf4j
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
				embeddedFiles.put(fileNameRef, newEmbeddedFileObject(new File(fileName).toURI(), fileNameRef));
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
	static EmbeddedFile newEmbeddedFileObject(final URI fileUri, final String fileNameRef) throws IOException {
		if (!fileExists(fileUri)) {
			throw new IOException(CANT_FIND_EMBEDDED_FILE + fileUri.getPath());
		}
		return new EmbeddedFile(parseEmbeddedFile(fileUri), findExtension(fileUri.getPath()), fileNameRef);
	}

	/**
	 * Check the existence of a file, given its URI.
	 * This method will check if the file is in a ZP container.
	 * @param fileUri The URI of the file we want to find
	 * @return true if the file exists, false if not
	 * @throws IOException
	 */
	public static boolean fileExists(final URI fileUri) throws IOException {
		// If the file is not in a zip container, Files.exists is enough to find it
		if (Files.exists(new File(fileUri).toPath())) {
			return true;
		}

		// If the file is in a zip container, we have to look into the zip container to find the right entry
		return findZipEntry(fileUri.getPath()) != null;
	}

	/**
	 * Find the {@link ZipEntry} within a ZIP file, given the path of the file as a String
	 * @param filePath The path of the file as a String
	 * @return the {@link ZipEntry} if it exists within a Zip file, null if it doesn't.
	 * @throws IOException
	 */
	public static ZipEntry findZipEntry(final String filePath) throws IOException {
		final int zipIndex = filePath.lastIndexOf(ZIP);

		if (zipIndex != -1) {
			// First we need to found the zip in the file system
			try {
				final ZipFile zipFile = new ZipFile(filePath.substring(0, zipIndex + ZIP.length()));

				// Then we try to find the file in the zip
				final ZipEntry zipEntry = zipFile.getEntry(filePath.substring(zipIndex + ZIP.length() + 1).replace("\\", "/"));

				return zipEntry;
			} catch (IOException exception) {
				log.error(
					"Error while reading zipFile {}: {}",
					filePath.substring(0, zipIndex + ZIP.length()),
					exception.getMessage()
				);
			}
		}

		return null;
	}

	/**
	 * Returns the extension of a file from the given the file name
	 *
	 * @param fileName
	 * @return String value
	 */
	static String findExtension(final String fileName) {
		final int index = fileName.lastIndexOf('.');
		final int separatorIndex = Math.max(fileName.lastIndexOf('/'), fileName.lastIndexOf('\\'));
		// We want to find the index of the last '.' only if it's in the last file of the path
		// in case the file is in an .zip archive (or any kind of archive)
		if (index > separatorIndex) {
			return fileName.substring(index + 1);
		}
		return null;
	}

	/**
	 * Parse an embedded file located in a .zip file given its URI
	 * @param fileUri The URI of the file we want to parse
	 * @return The content of the file
	 * @throws IOException When the embedded file can't be read.
	 */
	static String parseEmbeddedFile(final URI fileUri) throws IOException {
		final StringBuilder stringBuilder = new StringBuilder();

		final String filePath = Paths.get(fileUri).toString();
		if (filePath.contains(ZIP)) {
			final int zipIndex = filePath.lastIndexOf(ZIP) + ZIP.length();

			// First we need to found the zip in the file system
			final ZipFile zipFile = new ZipFile(filePath.substring(0, zipIndex));

			// Then we try to find the yaml file in the zip
			final ZipEntry zipEntry = zipFile.getEntry(filePath.substring(zipIndex + 1).replace("\\", "/"));

			if (zipEntry != null) {
				try (Scanner scanner = new Scanner(zipFile.getInputStream(zipEntry), StandardCharsets.UTF_8.name())) {
					// Read the content of the file
					while (scanner.hasNextLine()) {
						stringBuilder.append(scanner.nextLine());
						if (scanner.hasNextLine()) {
							stringBuilder.append(NEW_LINE);
						}
					}
				}
				zipFile.close();
			}
		} else {
			// Open an input stream for the file within the JAR
			try (InputStream inputStream = fileUri.toURL().openStream()) {
				// Use a Scanner to read the content
				try (Scanner scanner = new Scanner(inputStream, StandardCharsets.UTF_8.name())) {
					// Read the content of the file
					while (scanner.hasNextLine()) {
						stringBuilder.append(scanner.nextLine());
						if (scanner.hasNextLine()) {
							stringBuilder.append(NEW_LINE);
						}
					}
				}
			} catch (Exception e) {
				throw new IOException("Could not read embedded file: " + fileUri.getPath());
			}
		}
		return stringBuilder.toString();
	}
}
