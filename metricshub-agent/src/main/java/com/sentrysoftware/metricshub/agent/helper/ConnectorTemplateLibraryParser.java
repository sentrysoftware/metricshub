package com.sentrysoftware.metricshub.agent.helper;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.dataformat.yaml.YAMLMapper;
import com.sentrysoftware.metricshub.agent.config.ConnectorVariables;
import com.sentrysoftware.metricshub.engine.connector.model.Connector;
import com.sentrysoftware.metricshub.engine.connector.parser.ConnectorParser;
import java.io.IOException;
import java.nio.file.FileVisitResult;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.SimpleFileVisitor;
import java.nio.file.attribute.BasicFileAttributes;
import java.util.Map;
import java.util.TreeMap;
import lombok.extern.slf4j.Slf4j;

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

			if (!connectorVariablesMap.containsKey(connectorId)) {
				return FileVisitResult.CONTINUE;
			}

			final ConnectorParser connectorParser = ConnectorParser.withNodeProcessorAndUpdateChain(
				path.getParent(),
				connectorVariablesMap.get(connectorId).getVariableValues()
			);

			// Put in the custom connectorsMap
			customConnectorsMap.put(connectorId, connectorParser.parse(path.toFile()));

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
	}

	/**
	 * @param yamlParentDirectory the directory containing connectors yaml files
	 * @return Map<String, Connector> (connectors map: key=yamlFileName, value=Connector)
	 * @throws IOException if the file does not exist
	 */
	public Map<String, Connector> parse(
		final Path yamlParentDirectory,
		Map<String, ConnectorVariables> connectorVariablesMap
	) throws IOException {
		final long startTime = System.currentTimeMillis();
		final ConnectorFileVisitor connectorFileVisitor = new ConnectorFileVisitor(connectorVariablesMap);
		Files.walkFileTree(yamlParentDirectory, connectorFileVisitor);
		log.info("Yaml loading duration: {} seconds", (System.currentTimeMillis() - startTime) / 1000);
		return connectorFileVisitor.getCustomConnectorsMap();
	}
}
