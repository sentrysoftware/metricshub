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
	public JsonNode process(JsonNode node, boolean callNextProcessor) throws IOException {

		JsonNode result = doMerge(node);

		// Call next processor
		return callNextProcessor ? destination.process(result, true) : result;
	}

	/**
	 * custom merge logic
	 * @param node
	 * @return
	 * @throws IOException
	 */
	private JsonNode doMerge(JsonNode node) throws IOException {
		JsonNode extNode = node.get("extends");

		JsonNode result = node;
		if (extNode != null && extNode.isArray()) {
			ArrayNode extNodeArray = (ArrayNode) extNode;
			Iterator<JsonNode> iter = extNodeArray.iterator();

			JsonNode extended = null;
			if (iter.hasNext()) {
				extended =  process(getJsonNode(iter), false);
				while(iter.hasNext()) {
					JsonNode extendedNext = process(getJsonNode(iter), false);
					merge(extended, extendedNext);
				}
			}

			extNodeArray.removeAll();

			if (extended != null) {
				result = merge(extended, node);
			}
		}
		return result;
	}

	/**
	 * gets the next json node from the iterator
	 * @param iter
	 * @return
	 * @throws IOException
	 */
	private JsonNode getJsonNode(Iterator<JsonNode> iter) throws IOException {
		return mapper
			.readTree(connectorDirectory.resolve(iter.next().asText() + ".yaml").toFile());
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
				mergeJsonArray(updateNode, fieldName, jsonNode);
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

	/**
	 * handles the specific merge logic for arrays
	 * @param updateNode
	 * @param fieldName
	 * @param jsonNode
	 */
	private static void mergeJsonArray(JsonNode updateNode, String fieldName, JsonNode jsonNode) {
		ArrayNode mainArray = (ArrayNode) jsonNode;
		ArrayNode extendedArray = (ArrayNode) updateNode.get(fieldName);

		if (mainArray.size() != 0 && mainArray.get(0).isObject()) {
			// Array of objects gets merged
			for (int i = 0; i < extendedArray.size(); i++) {
				mainArray.add(extendedArray.get(i));
			}

		} else {
			// Simple array gets overwritten
			mainArray.removeAll();
			mainArray.addAll(extendedArray);
		}
	}
}
