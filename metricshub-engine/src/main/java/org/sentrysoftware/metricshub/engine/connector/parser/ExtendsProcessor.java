package org.sentrysoftware.metricshub.engine.connector.parser;

/*-
 * ╱╲╱╲╱╲╱╲╱╲╱╲╱╲╱╲╱╲╱╲╱╲╱╲╱╲╱╲╱╲╱╲╱╲╱╲╱╲╱╲
 * MetricsHub Engine
 * ჻჻჻჻჻჻
 * Copyright 2023 - 2024 Sentry Software
 * ჻჻჻჻჻჻
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Affero General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU Affero General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 * ╲╱╲╱╲╱╲╱╲╱╲╱╲╱╲╱╲╱╲╱╲╱╲╱╲╱╲╱╲╱╲╱╲╱╲╱╲╱╲╱
 */

import static org.sentrysoftware.metricshub.engine.common.helpers.MetricsHubConstants.CONNECTORS;
import static org.sentrysoftware.metricshub.engine.common.helpers.MetricsHubConstants.ZIP;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ArrayNode;
import com.fasterxml.jackson.databind.node.ObjectNode;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Path;
import java.util.Iterator;
import java.util.zip.ZipEntry;
import java.util.zip.ZipFile;
import lombok.Builder;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NonNull;
import org.sentrysoftware.metricshub.engine.common.helpers.FileHelper;

@Data
@EqualsAndHashCode(callSuper = true)
public class ExtendsProcessor extends AbstractNodeProcessor {

	@NonNull
	private Path connectorDirectory;

	@NonNull
	private ObjectMapper mapper;

	@Builder
	public ExtendsProcessor(@NonNull Path connectorDirectory, @NonNull ObjectMapper mapper, AbstractNodeProcessor next) {
		super(next);
		this.connectorDirectory = connectorDirectory;
		this.mapper = mapper;
	}

	@Override
	public JsonNode processNode(JsonNode node) throws IOException {
		return doMerge(node);
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
	 * @param node
	 * @return {@link JsonNode} instance
	 * @throws IOException
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
	 * Gets the next {@link JsonNode} from the iterator
	 *
	 * @param iter {@link Iterator} over a collection of {@link JsonNode}
	 * @return {@link JsonNode} object
	 * @throws IOException
	 */
	private JsonNode getJsonNode(Iterator<JsonNode> iter) throws IOException {
		final String connectorRelativePath = iter.next().asText() + ".yaml";

		// If the path is absolute, it should refer to a path within the "connectors" directory
		if (!connectorRelativePath.startsWith(".")) {
			final Path connectorsDirectoryPath = FileHelper.findConnectorsDirectory(connectorDirectory);
			if (connectorsDirectoryPath != null) {
				final File connectorPathFile = connectorsDirectoryPath.resolve(connectorRelativePath).normalize().toFile();
				if (connectorPathFile != null && connectorPathFile.exists()) {
					return mapper.readTree(connectorPathFile);
				}
			}
		}

		Path connectorPath = connectorDirectory.resolve(connectorRelativePath).normalize();
		final String strPath = connectorPath.normalize().toString();

		final int zipIndex = strPath.lastIndexOf(ZIP);
		if (zipIndex != -1) {
			// In order to check if the yaml file actually exists, we need to look into the zip file and check if there is an entry of that name
			final JsonNode res;

			// First we need to found the zip in the file system
			try (ZipFile zipFile = new ZipFile(strPath.substring(0, zipIndex + ZIP.length()))) {
				// Then we try to find the yaml file in the zip
				final ZipEntry zipEntry = zipFile.getEntry(strPath.substring(zipIndex + ZIP.length() + 1).replace("\\", "/"));
				if (zipEntry != null) {
					try (InputStream inputStream = zipFile.getInputStream(zipEntry)) {
						res = mapper.readTree(inputStream);
					}
				} else { // If we can't find the parent in the zip file, we will try to find it in the connector directory
					final int connectorsIndex = strPath.indexOf(CONNECTORS);

					if (connectorsIndex == -1) {
						throw new IllegalStateException("Cannot find connector directory");
					}

					// We will recreate the path to the file if it's in the connectors directory:
					// <path to the connectors directory> + <path of the file>
					final File fileInConnectorsDirectory = new File(
						strPath.substring(0, connectorsIndex + CONNECTORS.length()) + strPath.substring(zipIndex + ZIP.length())
					);
					res = mapper.readTree(fileInConnectorsDirectory);
				}
			}
			return res;
		}
		return mapper.readTree(connectorPath.toFile());
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
	 * @param mainNode
	 * @param updateNode
	 * @return {@link JsonNode} merged
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
	 * Handles the specific merge logic for arrays
	 *
	 * @param updateNode
	 * @param fieldName
	 * @param jsonNode
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