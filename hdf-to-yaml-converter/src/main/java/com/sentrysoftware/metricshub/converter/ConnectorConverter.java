package com.sentrysoftware.metricshub.converter;

import static com.sentrysoftware.metricshub.converter.ConverterConstants.CONNECTION_TYPES;
import static com.sentrysoftware.metricshub.converter.ConverterConstants.CONNECTOR;
import static com.sentrysoftware.metricshub.converter.ConverterConstants.DETECTION;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.node.ArrayNode;
import com.fasterxml.jackson.databind.node.JsonNodeFactory;
import com.fasterxml.jackson.databind.node.ObjectNode;
import com.fasterxml.jackson.databind.node.TextNode;
import com.sentrysoftware.metricshub.converter.state.ConnectorState;
import com.sentrysoftware.metricshub.converter.state.ConversionHelper;
import com.sentrysoftware.metricshub.converter.state.mapping.MappingConvertersWrapper;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Optional;
import java.util.Set;
import java.util.regex.Pattern;
import lombok.NonNull;
import lombok.RequiredArgsConstructor;

@RequiredArgsConstructor
public class ConnectorConverter {

	private static final Map<Pattern, String> IGNORED_KEY_PATTERNS = Map.of(
		Pattern.compile(ConversionHelper.buildCriteriaKeyRegex("type")),
		"snmp"
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

		// Go through each key-value entry in the connector
		preConnector.getCodeMap().forEach((key, value) -> convertKeyValue(key, value, connector));

		// Set translation tables
		setTranslationTables(connector);

		// Post conversion for the discovery mapping properties
		MappingConvertersWrapper wrapper = new MappingConvertersWrapper();

		wrapper.removeMonitor(connector, ConverterConstants.YAML_CPU_CORE);
		wrapper.postConvertDiscovery(connector);

		// Handle default value for LocalSupport
		setDefaultLocalSupport(connector, preConnector.getCodeMap().keySet());

		return connector;
	}

	/**
	 * If the HDF doesn't define hdf.localsupport add "local" to the connectionTypes array node
	 *
	 * @param connector Global YAML connector
	 * @param codeKeys code keys. Example: <em>[ hdf.remotesupport, hdf.localsupport, hdf.onlastresort, ... ]</em>
	 */
	private void setDefaultLocalSupport(final JsonNode connector, final Set<String> codeKeys) {
		final JsonNode connectorNode = connector.get(CONNECTOR);
		if (connectorNode != null) {
			final ObjectNode detectionNode = (ObjectNode) connectorNode.get(DETECTION);

			if (detectionNode != null) {
				final ArrayNode connectionTypeNode = (ArrayNode) detectionNode.get(CONNECTION_TYPES);
				if (
					connectionTypeNode != null && codeKeys.stream().noneMatch(s -> s.toLowerCase().startsWith("hdf.localsupport"))
				) {
					connectionTypeNode.add("local");
				}
			}
		}
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
		((ObjectNode) connector).set("translations", translations);
		for (final Entry<String, Map<String, String>> translationsEntry : translationTablesMap.entrySet()) {
			final String tableKey = translationsEntry.getKey();

			final ObjectNode translationTable = JsonNodeFactory.instance.objectNode();
			translations.set(tableKey, translationTable);
			translationsEntry
				.getValue()
				.forEach((key, value) -> translationTable.set(key, JsonNodeFactory.instance.textNode(value)));
		}
	}

	/**
	 * Detect and convert the given line
	 *
	 * @param key the Connector key we wish to extract its value
	 * @param value the corresponding value we wish to process
	 * @param connector {@link JsonNod} instance to update
	 */
	private void convertKeyValue(final String key, final String value, final JsonNode connector) {
		// Get the detected state
		final Optional<ConnectorState> optionalState = ConnectorState
			.getConnectorStates()
			.stream()
			.filter(state -> state.detect(key, value, connector))
			.findFirst();

		optionalState.ifPresentOrElse(
			// We've got the key
			state -> state.convert(key, value, connector, preConnector),
			() -> {
				// The key doesn't match any parser, add it to the problem list, except if it's
				// safe to ignore
				if (!isKeyValueSafeToIgnore(key, value)) {
					preConnector.getProblemList().add(String.format("Invalid key value: %s - %s", key, value));
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
		constantsMap
			.entrySet()
			.forEach(entry ->
				constants.set(entry.getKey(), new TextNode(ConversionHelper.performValueConversions(entry.getValue())))
			);
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

		// Each connector or header is defined in a specific directory having the same name as the connector file without extension.
		extendedConnectorsSet.forEach(parent -> {
			var newName = ConnectorLibraryConverter.getConnectorFilenameNoExtension(parent);
			extendedConnectors.add(String.format("../%s/%s", newName, newName));
		});

		((ObjectNode) jsonNode).set("extends", extendedConnectors);
	}

	/**
	 * Check whether the specified key is in the "safe ignore" list (i.e. there
	 * is no converter matching, but it's still a valid key)
	 *
	 * @param key 	Key to check
	 * @param value value to check
	 * @return whether the specified key is safe to ignore
	 */
	static boolean isKeyValueSafeToIgnore(final String key, final String value) {
		for (Entry<Pattern, String> entry : IGNORED_KEY_PATTERNS.entrySet()) {
			if (entry.getKey().matcher(key).find() && entry.getValue().equals(value.toLowerCase())) {
				return true;
			}
		}
		return false;
	}
}
