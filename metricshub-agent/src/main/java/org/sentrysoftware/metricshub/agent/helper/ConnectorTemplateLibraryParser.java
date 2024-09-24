package org.sentrysoftware.metricshub.agent.helper;

/*-
 * ╱╲╱╲╱╲╱╲╱╲╱╲╱╲╱╲╱╲╱╲╱╲╱╲╱╲╱╲╱╲╱╲╱╲╱╲╱╲╱╲
 * MetricsHub Agent
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

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.dataformat.yaml.YAMLMapper;
import java.io.IOException;
import java.nio.file.FileVisitResult;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.SimpleFileVisitor;
import java.nio.file.attribute.BasicFileAttributes;
import java.util.HashMap;
import java.util.Map;
import java.util.Map.Entry;
import java.util.TreeMap;
import lombok.NonNull;
import lombok.extern.slf4j.Slf4j;
import org.sentrysoftware.metricshub.engine.configuration.ConnectorVariables;
import org.sentrysoftware.metricshub.engine.connector.model.Connector;
import org.sentrysoftware.metricshub.engine.connector.model.identity.ConnectorDefaultVariable;
import org.sentrysoftware.metricshub.engine.connector.parser.ConnectorParser;

/**
 * Utility class for parsing connector template YAML files and creating a map of custom connectors.
 * This class provides functionality to visit YAML files in a specified directory, read connector data,
 * and create Connector objects based on the parsed data. The resulting connectors are stored in a map
 * with the connector ID as the key and the corresponding Connector object as the value.
 * <p>
 * The parsing process involves checking for YAML files, validating whether the YAML structure defines a
 * final Connector (with a displayName section), and using ConnectorParser to parse the YAML file and create
 * Connector objects.
 * </p>
 */
@Slf4j
public class ConnectorTemplateLibraryParser {

	/**
	 * This inner class allows to visit the files contained within the Yaml directory
	 */
	private static class ConnectorFileVisitor extends SimpleFileVisitor<Path> {

		private final Map<String, ConnectorVariables> connectorVariablesMap;
		private final Map<String, Connector> customConnectorsMap = new TreeMap<>(String.CASE_INSENSITIVE_ORDER);

		public Map<String, Connector> getCustomConnectorsMap() {
			return customConnectorsMap;
		}

		ConnectorFileVisitor(final Map<String, ConnectorVariables> connectorVariablesMap) {
			this.connectorVariablesMap = connectorVariablesMap;
		}

		@Override
		public FileVisitResult visitFile(Path path, BasicFileAttributes attrs) throws IOException {
			// Skip this path if it is a directory or not a YAML file
			if (Files.isDirectory(path) || !isYamlFile(path.toFile().getName())) {
				return FileVisitResult.CONTINUE;
			}
			final ObjectMapper yamlMapper = new YAMLMapper();
			final JsonNode connectorNode = yamlMapper.readTree(path.toFile());
			if (!isConnector(connectorNode)) {
				return FileVisitResult.CONTINUE;
			}

			// Get the connector's file name
			final String filename = path.getFileName().toString();
			final String connectorId = filename.substring(0, filename.lastIndexOf('.'));

			if (!connectorNode.toString().contains("${var::")) {
				return FileVisitResult.CONTINUE;
			}

			// User connector variables
			final ConnectorVariables connectorUserVariables = connectorVariablesMap.computeIfAbsent(
				connectorId,
				id -> new ConnectorVariables()
			);

			// Retrieve the default connector variables that have been specified in this connector.
			Map<String, ConnectorDefaultVariable> connectorDefaultVariables = getConnectorVariables(connectorNode);

			// User didn't configure variables for this connector, and no connector default variables are configured
			if (connectorUserVariables.getVariableValues().isEmpty() && connectorDefaultVariables.isEmpty()) {
				return FileVisitResult.CONTINUE;
			}

			for (final Entry<String, ConnectorDefaultVariable> entry : connectorDefaultVariables.entrySet()) {
				connectorUserVariables.getVariableValues().putIfAbsent(entry.getKey(), entry.getValue().getDefaultValue());
			}

			final ConnectorParser connectorParser = ConnectorParser.withNodeProcessorAndUpdateChain(
				path.getParent(),
				connectorVariablesMap.get(connectorId).getVariableValues()
			);

			// Put in the custom connectorsMap
			try {
				customConnectorsMap.put(connectorId, connectorParser.parse(path.toFile()));
			} catch (Exception e) {
				log.error("Error while parsing connector with template variables {}: {}", filename, e.getMessage());
				log.debug("Exception: ", e);
			}

			return FileVisitResult.CONTINUE;
		}

		/**
		 * Whether the JsonNode is a final Connector. It means that this JsonNode defines the displayName section.
		 *
		 * @param connector JsonNode that contains connector's data
		 * @return <code>true</code> if the {@link JsonNode} is a final connector, otherwise false.
		 */
		private boolean isConnector(final JsonNode connector) {
			final JsonNode connectorNode = connector.get("connector");
			if (connectorNode != null && !connectorNode.isNull()) {
				final JsonNode displayName = connectorNode.get("displayName");
				return displayName != null && !displayName.isNull();
			}

			return false;
		}

		/**
		 * Whether the connector is a YAML file or not
		 *
		 * @param name given fileName
		 * @return boolean value
		 */
		private boolean isYamlFile(final String name) {
			return name.toLowerCase().endsWith(".yaml");
		}

		/**
		 * Converts the "variables" section of a {@link JsonNode} into a {@link Map} where each entry consists of
		 * a variable name as the key and a {@link ConnectorDefaultVariable} as the value.
		 * Each {@link ConnectorDefaultVariable} contains a description and a default value extracted from the JSON node.
		 *
		 * @param connectorNode the {@link JsonNode} representing the connector, which includes a "variables" section.
		 * @return a map where the key is the variable name, and the value is a {@link ConnectorDefaultVariable} object
		 *         containing the description and defaultValue for that variable. If the "variables" section is not present,
		 *         an empty map is returned.
		 */
		private static Map<String, ConnectorDefaultVariable> getConnectorVariables(final JsonNode connectorNode) {
			final JsonNode variablesNode = connectorNode.get("connector").get("variables");
			if (variablesNode == null) {
				return new HashMap<>();
			}

			final Map<String, ConnectorDefaultVariable> connectorVariablesMap = new HashMap<>();

			// Iterate over the variables and extract description and defaultValue
			variablesNode
				.fields()
				.forEachRemaining(entry -> {
					final String variableName = entry.getKey();
					final JsonNode variableValue = entry.getValue();

					final String description = variableValue.get("description").asText();
					final String defaultValue = variableValue.get("defaultValue").asText();

					// Create a ConnectorDefaultVariable object and put it into the map
					final ConnectorDefaultVariable connectorDefaultVariable = new ConnectorDefaultVariable(
						description,
						defaultValue
					);
					connectorVariablesMap.put(variableName, connectorDefaultVariable);
				});

			return connectorVariablesMap;
		}
	}

	/**
	 * Parses connector template YAML files in the specified directory and creates a map of custom connectors.
	 *
	 * @param yamlParentDirectory   The directory containing connector YAML files
	 * @param connectorVariablesMap A map of ConnectorVariables for variable substitution
	 * @return Map&lt;String, Connector&gt; (connectors map: key=yamlFileName, value=Connector)
	 * @throws IOException if the file does not exist or an I/O error occurs during processing
	 */
	public Map<String, Connector> parse(
		@NonNull final Path yamlParentDirectory,
		@NonNull final Map<String, ConnectorVariables> connectorVariablesMap
	) throws IOException {
		final long startTime = System.currentTimeMillis();
		final ConnectorFileVisitor connectorFileVisitor = new ConnectorFileVisitor(connectorVariablesMap);
		Files.walkFileTree(yamlParentDirectory, connectorFileVisitor);
		log.info(
			"Connectors with template variables parsing duration: {} seconds",
			(System.currentTimeMillis() - startTime) / 1000
		);
		return connectorFileVisitor.getCustomConnectorsMap();
	}
}
