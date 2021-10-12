package com.sentrysoftware.matrix.connector.parser;

import com.sentrysoftware.matrix.connector.model.Connector;
import com.sentrysoftware.matrix.connector.parser.state.ConnectorState;

import lombok.Data;
import org.springframework.util.Assert;

import static com.sentrysoftware.matrix.common.helpers.HardwareConstants.REMOVE_MS_HW_PATTERN;

import java.io.IOException;
import java.util.Optional;
import java.util.Set;
import java.util.regex.Pattern;

@Data
public class ConnectorParser {

	private static final Set<Pattern> IGNORED_KEY_PATTERNS = Set.of(
			Pattern.compile("^detection\\.criteria\\([0-9]+\\)\\.type$", Pattern.CASE_INSENSITIVE),
			Pattern.compile("^[a-z]+\\.discovery\\.instance\\.parameteractivation\\.[a-z]+$", Pattern.CASE_INSENSITIVE),
			Pattern.compile("^hdf\\.mshwrequiredversion$", Pattern.CASE_INSENSITIVE),
			Pattern.compile("^sudo\\([1-9][0-9]*\\)\\.command$", Pattern.CASE_INSENSITIVE),
			Pattern.compile("\\.(source|criteria)\\([0-9]+\\)\\.step\\([0-9]+\\)\\.", Pattern.CASE_INSENSITIVE),
			Pattern.compile("^detection\\.criteria\\([0-9]+\\)\\.version$", Pattern.CASE_INSENSITIVE)
	);

	/**
	 * Process Connector file
	 *
	 * @param connectorFilePath	The path of the {@link Connector} file
	 * @return a {@link Connector} instance or null in case ParserException and lenient mode is enabled
	 *
	 * @throws IOException when not able to read the specified file
	 */
	public Connector parse(final String connectorFilePath) throws IOException {

		Assert.isTrue(
				connectorFilePath != null && !connectorFilePath.isBlank(),
				"connectorFilePath cannot be null or empty"
		);

		// Load the connector and convert its content into a map
		final ConnectorRefined connectorRefined = new ConnectorRefined();
		connectorRefined.load(connectorFilePath);

		// Interpret all key-value pairs in the connector
		return parseContent(connectorRefined);

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

		// Go through each key-value entry in the connector
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
		Optional<ConnectorState> optionalState = ConnectorState
				.getConnectorStates()
				.stream()
				.filter(state -> state.detect(key, value, connector))
				.findFirst();

		// We got the key
		if (optionalState.isPresent()) {
			optionalState.get().parse(key, value, connector);
			return;
		}

		// The key doesn't match any parser, add it to the problem list, except if it's
		// safe to ignore
		if (!isKeySafeToIgnore(key)) {
			connector.getProblemList().add("Invalid key: " + key);
		}

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

	/**
	 * Check whether the specified key is in the "safe ignore" list
	 * (i.e. there is no parser matching, but it's still a valid key)
	 * @param key Key to check
	 * @return whether the specified key is safe to ignore
	 */
	static boolean isKeySafeToIgnore(String key) {
		return IGNORED_KEY_PATTERNS.stream().anyMatch(p -> p.matcher(key).find());
	}
}
