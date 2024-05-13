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

import static org.sentrysoftware.metricshub.engine.common.helpers.MetricsHubConstants.FILE_PATTERN;

import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Optional;
import java.util.regex.Matcher;
import lombok.AccessLevel;
import lombok.NoArgsConstructor;
import lombok.NonNull;
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
	 * @param value                  The value can be a command line or any value referencing multiple embedded files.
	 * @param connectorEmbeddedFiles All the embedded files referenced in the connector.
	 * @return A map of the file reference to {@link EmbeddedFile} instance.
	 * @throws IOException If an I/O error occurs while processing the embedded files.
	 */
	public static Map<Integer, EmbeddedFile> findEmbeddedFiles(@NonNull final String value, @NonNull final Map<Integer, EmbeddedFile> connectorEmbeddedFiles) throws IOException {
		final Map<Integer, EmbeddedFile> embeddedFiles = new HashMap<>();
		final List<Integer> alreadyProcessedFiles = new ArrayList<>();

		final Matcher fileMatcher = FILE_PATTERN.matcher(value);

		// Verify the embedded file reference. Example: ${file::embedded-file-number} // NOSONAR on comment
		while (fileMatcher.find()) {
			// The number of the embedded file
			final Integer fileNumber = Integer.parseInt(fileMatcher.group(1));

			// If the embeddedFile has already been processed, no need to continue
			if (!alreadyProcessedFiles.contains(fileNumber)) {
				embeddedFiles.put(fileNumber, connectorEmbeddedFiles.get(fileNumber));
				alreadyProcessedFiles.add(fileNumber);
			}
		}

		return embeddedFiles;
	}

	/**
	 * Finds one embedded file that is referenced in the given string.
	 *
	 * @param value                  The value which references the embedded file, it can be an AWK directive, header, body, etc.
	 * @param connectorEmbeddedFiles All the embedded files referenced in the connector.
	 * @return An Optional of {@link EmbeddedFile} instance.
	 * @throws IOException If an I/O error occurs while processing the embedded files.
	 */
	public static Optional<EmbeddedFile> findEmbeddedFile(@NonNull final String value,
			@NonNull final Map<Integer, EmbeddedFile> connectorEmbeddedFiles) throws IOException {
		return findEmbeddedFiles(value, connectorEmbeddedFiles).entrySet().stream().map(Entry::getValue).findFirst();
	}

}
