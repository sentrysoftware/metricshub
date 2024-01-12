package com.sentrysoftware.metricshub.agent.deserialization;

import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.databind.DeserializationContext;
import com.fasterxml.jackson.databind.JsonDeserializer;
import com.fasterxml.jackson.databind.JsonNode;
import com.sentrysoftware.metricshub.agent.config.ConnectorVariables;
import java.io.IOException;
import java.util.Map;
import java.util.TreeMap;

/**
 * Custom JSON deserializer for deserializing a JSON object into a TreeMap with
 * ConnectorVariables instances and case-insensitive comparator for keys. Each
 * ConnectorVariables instance has a Map for variableValues.
 */
public class ConnectorVariablesDeserializer extends JsonDeserializer<Map<String, ConnectorVariables>> {

	/**
	 * Deserializes a JSON object into a TreeMap with ConnectorVariables
	 * instances and case-insensitive comparator for keys.<br> Each
	 * ConnectorVariables instance has a Map for variableValues.
	 *
	 * @param jsonParser The JsonParser object for reading JSON content.
	 * @param context    The DeserializationContext object.
	 * @return  The deserialized Map with ConnectorVariables instances.
	 * @throws IOException If an I/O error occurs during deserialization.
	 */
	@Override
	public Map<String, ConnectorVariables> deserialize(final JsonParser jsonParser, final DeserializationContext context)
		throws IOException {
		final JsonNode node = jsonParser.getCodec().readTree(jsonParser);

		// TreeMap with case-insensitive comparator for storing key-ConnectorVariables pairs
		final Map<String, ConnectorVariables> treeMap = new TreeMap<>(String.CASE_INSENSITIVE_ORDER);

		// Iterate through the fields of the JSON object
		if (node != null) {
			node
				.fields()
				.forEachRemaining(entry -> {
					final String name = entry.getKey();
					final JsonNode value = entry.getValue();

					if (value != null && !value.isNull()) {
						final ConnectorVariables connectorVariables = new ConnectorVariables();

						// Iterate through the variableValues of the ConnectorVariables instance
						value
							.fields()
							.forEachRemaining(variableValuesEntry -> {
								final String variableName = variableValuesEntry.getKey();
								final JsonNode variableValue = variableValuesEntry.getValue();

								if (variableValue != null && !variableValue.isNull()) {
									connectorVariables.addVariableValue(variableName, variableValue.asText());
								}
							});

						treeMap.put(name, connectorVariables);
					}
				});
		}

		return treeMap;
	}
}
