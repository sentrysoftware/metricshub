package org.sentrysoftware.metricshub.engine.connector.parser;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ArrayNode;
import com.fasterxml.jackson.databind.node.ObjectNode;
import java.io.IOException;
import java.nio.file.Path;
import java.util.Iterator;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NonNull;

/**
 * Represents a processor that merges extended connectors specified under the 'extends' section of the given JSON node.
 * This processor recursively merges extended connectors, applying the merging logic for arrays and objects.
 * The merged result is then passed to the next processor in the chain.
 */
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
		final JsonNode result = doMerge(node);

		// Call next processor
		return destination.process(result);
	}

	/**
	 * Merge logic:<br>
	 * <ol>
	 *   <li>Merged extended connectors located under the extends section of the given node.</li>
	 *   <li>Once all the extended connectors are merged, merge the given JsonNode (node) with the extended connectors that have been merged.</li>
	 * </ol>
	 * <br>
	 * A recursive merge is applied for each extended connector because it can extend another connector too. That's why doMerge
	 * is called for each extended connector.
	 * @param node The JSON node to merge.
	 * @return The merged JSON node.
	 * @throws IOException If an I/O error occurs during merging.
	 */
	private JsonNode doMerge(JsonNode node) throws IOException {
		JsonNode extNode = node.get("extends");

		JsonNode result = node;
		if (extNode != null && extNode.isArray()) {
			final ArrayNode extNodeArray = (ArrayNode) extNode;
			final Iterator<JsonNode> iter = extNodeArray.iterator();

			JsonNode extended = null;
			if (iter.hasNext()) {
				extended = doMerge(getJsonNode(iter));
				while (iter.hasNext()) {
					final JsonNode extendedNext = doMerge(getJsonNode(iter));
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
	 * Gets the next JSON node from the iterator based on the connector directory.
	 *
	 * @param iter The iterator over a collection of JSON nodes.
	 * @return The next JSON node.
	 * @throws IOException If an I/O error occurs during node retrieval.
	 */
	private JsonNode getJsonNode(Iterator<JsonNode> iter) throws IOException {
		return mapper.readTree(connectorDirectory.resolve(iter.next().asText() + ".yaml").toFile());
	}

	/**
	 * Merge the given mainNode and updateNode.
	 * Merge strategy:<br>
	 * <ol>
	 *   <li>Arrays of objects are appended from <code>updateNode</code> to <code>mainNode</code>.</li>
	 *   <li>Arrays of simple values from <code>updateNode</code> erase the ones in <code>mainNode</code>.</li>
	 *   <li><code>updateNode</code> object values overwrite <code>mainNode</code> object values.<li>
	 * </ol>
	 *
	 * @param mainNode   The main JSON node to merge into.
	 * @param updateNode The update JSON node to merge.
	 * @return The merged JSON node.
	 */
	public static JsonNode merge(JsonNode mainNode, JsonNode updateNode) {
		final Iterator<String> fieldNames = updateNode.fieldNames();
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
	 * Merges JSON arrays based on specific conditions.
	 *
	 * @param updateNode The update JSON node containing the array to merge.
	 * @param fieldName  The name of the field representing the array.
	 * @param jsonNode   The main JSON node containing the array to merge into.
	 */
	private static void mergeJsonArray(JsonNode updateNode, String fieldName, JsonNode jsonNode) {
		ArrayNode mainArray = (ArrayNode) jsonNode;
		ArrayNode extendedArray = (ArrayNode) updateNode.get(fieldName);

		if (mainArray.size() != 0 && mainArray.get(0).isObject()) {
			// Array of objects gets merged (appended)
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
