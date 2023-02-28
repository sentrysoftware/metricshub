package com.sentrysoftware.matrix.connector.parser;

import java.io.IOException;
import java.nio.file.Path;
import java.util.Iterator;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.node.ArrayNode;
import com.fasterxml.jackson.databind.node.ObjectNode;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NonNull;

@AllArgsConstructor
@Builder
@Data
public class ExtendsProcessor implements NodeProcessor {

	@NonNull
	private Path connectorDirectory;
	@NonNull
	private NodeProcessor destination;

	@Override
	public JsonNode process(JsonNode node) throws IOException {

		JsonNode extNode = node.get("extends");

		if (extNode != null && extNode.isArray()) {
			ArrayNode extNodeArray = (ArrayNode) extNode;
			for (JsonNode e : extNodeArray) {
				merge(node, ConnectorParser.withNodeProcessor(connectorDirectory).getDeserializer().getMapper()
						.readTree(connectorDirectory.resolve(e.asText() + ".yaml").toFile()));
			}

			extNodeArray.removeAll();
		}

		// Call next processor
		return destination.process(node);
	}

	public static JsonNode merge(JsonNode mainNode, JsonNode updateNode) {
		Iterator<String> fieldNames = updateNode.fieldNames();
		while (fieldNames.hasNext()) {
			String fieldName = fieldNames.next();
			JsonNode jsonNode = mainNode.get(fieldName);
			if (jsonNode != null && jsonNode.isArray() && updateNode.get(fieldName).isArray()) {
				// both JSON nodes are arrays
				ArrayNode mainArray = (ArrayNode) jsonNode;
				ArrayNode updateArray = (ArrayNode) updateNode.get(fieldName);

				if (mainArray.get(0).isObject()) {
					// Array of objects gets merged
					for (int i = 0; i < updateArray.size(); i++) {
						mainArray.add(updateArray.get(i));
					}
				} else {
					// Simple array gets overwritten
					mainArray.removeAll();
					mainArray.addAll(updateArray);
				}
			} else if (jsonNode != null && jsonNode.isObject()) {
				// both JSON nodes are objects, merge them
				merge(jsonNode, updateNode.get(fieldName));
			} else {
				if (mainNode instanceof ObjectNode objectNode) {
					// overwrite field
					JsonNode value = updateNode.get(fieldName);
					objectNode.set(fieldName, value);
				}
			}
		}
		return mainNode;
	}
}
