package com.sentrysoftware.matrix.connector.parser;

import com.sentrysoftware.matrix.connector.model.Connector;
import com.sentrysoftware.matrix.connector.parser.state.ConnectorState;
import lombok.Data;
import org.springframework.util.Assert;

import static com.sentrysoftware.matrix.common.helpers.HardwareConstants.REMOVE_MS_HW_PATTERN;

import java.util.Optional;
import java.util.Set;
import java.util.stream.Collectors;

@Data
public class ConnectorParser {

	/**
	 * Process Connector file
	 *
	 * @param connectorFilePath	The path of the {@link Connector} file
	 * @return {@link Optional} of {@link Connector} instance
	 */
	public Connector parse(final String connectorFilePath) {

		Assert.isTrue(
				connectorFilePath != null && !connectorFilePath.trim().isEmpty(),
				"connectorFilePath cannot be null or empty"
		);

		try {

			final ConnectorRefined connectorRefined = new ConnectorRefined();

			connectorRefined.load(connectorFilePath);

			return parseContent(connectorRefined);

		} catch (Exception e) {

			throw new IllegalStateException(String.format(
					"Cannot load Connector file %s: %s",
					connectorFilePath,
					e.getMessage()
			));
		}
	}

	/**
	 * From the given {@link ConnectorRefined} object parse the whole Connector content
	 * @param connectorRefined	The refined connector
	 * @return {@link Connector} instance.
	 */
	private static Connector parseContent(final ConnectorRefined connectorRefined) {

		final Connector connector = new Connector();

		connector.setEmbeddedFiles(connectorRefined.getEmbeddedFiles());
		connector.setTranslationTables(connectorRefined.getTranslationTables());
		connector.setCompiledFilename(connectorRefined.getCompiledFilename());

		connectorRefined.getCodeMap().forEach((key, value) -> parseKeyValue(key, value, connector));

		return connector;
	}

	/**
	 * Detect and parse the given line
	 * @param key the Connector key we wish to extract its value
	 * @param value the corresponding value we wish to process
	 * @param connector {@link Connector} instance to update
	 */
	private static void parseKeyValue(final String key, final String value, final Connector connector) {

		// Get the detected state
		final Set<ConnectorState> connectorStates = ConnectorState
				.getConnectorStates()
				.stream()
				.filter(state -> state.detect(key, value, connector))
				.collect(Collectors.toSet());

		Optional<ConnectorState> firstConnectorState = connectorStates.stream().findFirst();
		firstConnectorState.ifPresent(connectorState -> connectorState.parse(key, value, connector));
	}

	/**
	 * Remove the extension from the file name and replace MS_HW_ prefix
	 *
	 * @param filename
	 * @return String value
	 */
	public static String normalizeConnectorName(String filename) {
		// remove the extension
		String compiledFileName = filename.substring(0, filename.lastIndexOf('.'));

		return REMOVE_MS_HW_PATTERN.matcher(compiledFileName).replaceFirst("$2");
	}
}
