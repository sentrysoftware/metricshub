package com.sentrysoftware.matrix.converter;

import java.util.Map;
import java.util.Map.Entry;
import java.util.Optional;
import java.util.Set;
import java.util.regex.Pattern;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.node.ArrayNode;
import com.fasterxml.jackson.databind.node.JsonNodeFactory;
import com.fasterxml.jackson.databind.node.ObjectNode;
import com.fasterxml.jackson.databind.node.TextNode;
import com.sentrysoftware.matrix.converter.state.ConnectorState;
import com.sentrysoftware.matrix.converter.state.ConversionHelper;

import lombok.NonNull;
import lombok.RequiredArgsConstructor;

@RequiredArgsConstructor
public class ConnectorConverter {

	private static final Set<Pattern> IGNORED_KEY_PATTERNS = Set.of(
		// Add key pattern to ignore here
	);

	@NonNull
	private PreConnector preConnector;

	/**
	 * Convert the {@link PreConnector} into a JsonNode connector instance
	 * 
	 * @return new {@link JsonNode} instance
	 */
	public JsonNode convert() {

		final JsonNode connector = JsonNodeFactory.instance.objectNode();

		// Set extended connectors
		setExtendedConnectors(connector);

		// Set constants
		setConstants(connector);

		// Set embedded files
		setEmbeddedFiles(connector);

		// Set translation tables
		setTranslationTables(connector);

		// Go through each key-value entry in the connector
		preConnector.getCodeMap().forEach((key, value) -> convertKeyValue(key, value, connector));

		return connector;
	}

	/**
	 * Set the connector's translation tables.
	 * 
	 * @param connector
	 */
	private void setTranslationTables(final JsonNode connector) {
		final Map<String, Map<String, String>> translationTablesMap = preConnector.getTranslationTables();
		// No need to create an empty node
		if (translationTablesMap.isEmpty()) {
			return;
		}

		final ObjectNode translations = JsonNodeFactory.instance.objectNode();
		((ObjectNode)connector).set("translations", translations);
		for (final Entry<String, Map<String, String>> translationsEntry : translationTablesMap.entrySet()) {
			final String tableKey = translationsEntry.getKey();

			final ObjectNode translationTable = JsonNodeFactory.instance.objectNode();
			translations.set(tableKey, translationTable);
			translationsEntry
				.getValue()
				.forEach(
					(key, value) -> translationTable
						.set(key, JsonNodeFactory.instance.textNode(value))
				);
			
		}

	}

	/**
	 * Set the connector's embedded files.
	 * 
	 * @param connector
	 */
	private void setEmbeddedFiles(final JsonNode connector) {
		final Map<String, String> embeddedFilesMap = preConnector.getEmbeddedFiles();
		// No need to create an empty node
		if (embeddedFilesMap.isEmpty()) {
			return;
		}

		final ObjectNode embeddedFiles = JsonNodeFactory.instance.objectNode();
		embeddedFilesMap
			.entrySet()
			.forEach(entry -> embeddedFiles
				.set(
					entry.getKey(),
					new TextNode(ConversionHelper.performValueConversions(entry.getValue()))
				)
			);
		((ObjectNode) connector).set("embedded", embeddedFiles);
	}

	/**
	 * Detect and convert the given line
	 * 
	 * @param key the Connector key we wish to extract its value
	 * @param value the corresponding value we wish to process
	 * @param connector {@link JsonNod} instance to update
	 */
	private void convertKeyValue(
		final String key,
		final String value,
		final JsonNode connector
	) {

		// Get the detected state
		final Optional<ConnectorState> optionalState = ConnectorState
				.getConnectorStates()
				.stream()
				.filter(state ->  state.detect(key, value, connector))
				.findFirst();

		optionalState.ifPresentOrElse(
			// We've got the key
			state -> state.convert(key, value, connector, preConnector),
			() -> {
				// The key doesn't match any parser, add it to the problem list, except if it's
				// safe to ignore
				if (!isKeySafeToIgnore(key)) {
					preConnector.getProblemList().add("Invalid key: " + key);
				}
			}
		);

	}

	/**
	 * Set the connector's constants
	 * 
	 * @param jsonNode
	 */
	private void setConstants(final JsonNode jsonNode) {
		final Map<String, String> constantsMap = preConnector.getConstants();
		// No need to create an empty node
		if (constantsMap.isEmpty()) {
			return;
		}

		final ObjectNode constants = JsonNodeFactory.instance.objectNode();
		constantsMap.entrySet().forEach(entry -> constants.set(entry.getKey(), new TextNode(entry.getValue())));
		((ObjectNode) jsonNode).set("constants", constants);
	}

	/**
	 * Set the extended connectors
	 * 
	 * @param jsonNode
	 */
	private void setExtendedConnectors(final JsonNode jsonNode) {
		final Set<String> extendedConnectorsSet = preConnector.getExtendedConnectors();
		// No need to create an empty node
		if (extendedConnectorsSet.isEmpty()) {
			return;
		}

		final ArrayNode extendedConnectors = JsonNodeFactory.instance.arrayNode();
		extendedConnectorsSet.forEach(extendedConnectors::add);
		((ObjectNode) jsonNode).set("extends", extendedConnectors);

	}

	/**
	 * Check whether the specified key is in the "safe ignore" list (i.e. there
	 * is no converter matching, but it's still a valid key)
	 * 
	 * @param key Key to check
	 * @return whether the specified key is safe to ignore
	 */
	static boolean isKeySafeToIgnore(final String key) {
		return IGNORED_KEY_PATTERNS.stream().anyMatch(p -> p.matcher(key).find());
	}

}
