package com.sentrysoftware.matrix.engine.strategy.utils;

import static org.springframework.util.Assert.notNull;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.apache.commons.lang3.SystemUtils;

import com.sentrysoftware.matrix.connector.model.Connector;
import com.sentrysoftware.matrix.connector.model.common.EmbeddedFile;

public class OsCommandHelper {

	private OsCommandHelper() {
	}

	private static final Pattern EMBEDDEDFILE_REPLACEMENT_PATTERN = Pattern.compile("%EmbeddedFile\\((\\d+)\\)%", Pattern.CASE_INSENSITIVE);

	/**
	 * Replace the content of the EmbeddedFiles in the given command line
	 * 
	 * @param commandLine The command line we wish to process
	 * @param connector   The connector instance which defines the embedded files.
	 * @return Updated command line value
	 */
	public static String updateOsCommandEmbeddedFile(final String commandLine, final Connector connector) {

		notNull(commandLine, "commandLine cannot be null.");
		notNull(connector, "connector cannot be null.");

		final Matcher matcher = EMBEDDEDFILE_REPLACEMENT_PATTERN.matcher(commandLine);

		final StringBuilder sb = new StringBuilder();
		while (matcher.find()) {
			// EmbeddedFile(embeddedFileIndex)
			final Integer embeddedFileIndex = Integer.parseInt(matcher.group(1));

			// The embedded file is available in the connector
			final EmbeddedFile embeddedFile = connector.getEmbeddedFiles().get(embeddedFileIndex);

			// This means there is a design problem or the HDF developer indicated a wrong embedded file
			notNull(embeddedFile, () -> "Cannot get the EmbeddedFile from the Connector. EmbeddedFile Index: " + embeddedFileIndex);
			final String embeddedFileContent = embeddedFile.getContent();

			// This means there is a design problem, the content can never be null
			notNull(embeddedFileContent, () -> "EmbeddedFile content is null. EmbeddedFile Index: " + embeddedFileIndex);
			matcher.appendReplacement(sb, embeddedFileContent);
		}

		matcher.appendTail(sb);

		return sb.toString();
	}

	/**
	 * @return <code>true</code> if the local system is Windows otherwise <code>false</code>
	 */
	public static boolean isWindows() {
		return SystemUtils.IS_OS_WINDOWS;
	}

}
