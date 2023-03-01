package com.sentrysoftware.matrix.connector.parser;

import java.io.IOException;
import java.nio.file.Path;
import java.util.Iterator;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
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
	@NonNull
	private ObjectMapper mapper;

	@Override
	public JsonNode process(JsonNode node) throws IOException {

		JsonNode result = doMerge(node, connectorDirectory);

		// Call next processor
		return destination.process(result);
	}

	/**
	 * 
	 * @param node
	 * @param connectorDirectory
	 * @return
	 * @throws IOException
	 */
	private JsonNode doMerge(JsonNode node, Path connectorDirectory) throws IOException {
		JsonNode extNode = node.get("extends");

		JsonNode result = node;
		if (extNode != null && extNode.isArray()) {
			ArrayNode extNodeArray = (ArrayNode) extNode;
			Iterator<JsonNode> iter = extNodeArray.iterator();

			JsonNode extended = null;
			if (iter.hasNext()) {
				extended = mapper
					.readTree(connectorDirectory.resolve(iter.next().asText() + ".yaml").toFile());

				while(iter.hasNext()) {
					JsonNode extendedNext = mapper
							.readTree(connectorDirectory.resolve(iter.next().asText() + ".yaml").toFile());

					merge(extended, extendedNext);
				}
			}

			if (extended != null) {
				result = merge(extended, node);
			}

			extNodeArray.removeAll();

		}
		return result;
	}

	/**
	 * 
	 * @param mainNode
	 * @param updateNode
	 * @return
	 */
	public static JsonNode merge(JsonNode mainNode, JsonNode updateNode) {

		Iterator<String> fieldNames = updateNode.fieldNames();
		while (fieldNames.hasNext()) {
			String fieldName = fieldNames.next();
			JsonNode jsonNode = mainNode.get(fieldName);
			if (jsonNode != null && jsonNode.isArray() && updateNode.get(fieldName).isArray()) {
				// both JSON nodes are arrays
				ArrayNode mainArray = (ArrayNode) jsonNode;
				ArrayNode extendedArray = (ArrayNode) updateNode.get(fieldName);

				if (mainArray.get(0).isObject()) {
					// Array of objects gets merged
					for (int i = 0; i < extendedArray.size(); i++) {
						mainArray.add(extendedArray.get(i));
					}

				} else {
					// Simple array gets overwritten
					mainArray.removeAll();
					mainArray.addAll(extendedArray);
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
