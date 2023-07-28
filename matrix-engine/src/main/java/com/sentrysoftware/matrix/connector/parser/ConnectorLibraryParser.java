package com.sentrysoftware.matrix.connector.parser;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.sentrysoftware.matrix.common.helpers.JsonHelper;
import com.sentrysoftware.matrix.common.helpers.MatrixConstants;
import com.sentrysoftware.matrix.connector.model.Connector;
import lombok.extern.slf4j.Slf4j;

import java.io.IOException;
import java.nio.file.FileVisitResult;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.SimpleFileVisitor;
import java.nio.file.attribute.BasicFileAttributes;
import java.util.Map;
import java.util.TreeMap;

@Slf4j
public class ConnectorLibraryParser {

	public static final ObjectMapper OBJECT_MAPPER = JsonHelper.buildYamlMapper();

	/**
	 * This inner class allows to visit the files contained within the Yaml directory
	 */
	private class ConnectorFileVisitor extends SimpleFileVisitor<Path> {

		private final Map<String, Connector> connectorsMap = new TreeMap<>();

		public Map<String, Connector> getConnectorsMap() {
			return connectorsMap;
		}

		@Override
		public FileVisitResult visitFile(Path file, BasicFileAttributes attrs) throws IOException {
			// Skip this path if it is a directory or not a YAML file
			if (Files.isDirectory(file) || !isYamlFile(file.toFile().getName())) {
				return FileVisitResult.CONTINUE;
			}

			final JsonNode connectorNode = OBJECT_MAPPER.readTree(file.toFile());
			if (!isConnector(connectorNode)) {
				return FileVisitResult.CONTINUE;
			}
			final ConnectorParser connectorParser = ConnectorParser.withNodeProcessor(file.getParent());
			final String fileName = file.getFileName().toString();

			final Connector connector = connectorParser.parse(file.toFile());
			connectorsMap.put(fileName, connector);

			return FileVisitResult.CONTINUE;
		}

		/**
		 * Whether the JsonNode is a final Connector. It means that this JsonNode defines the displayName section.
		 *
		 * @param connector JsonNode that contains connector's data
		 * @return <code>true</code> if the {@link JsonNode} is a final connector, otherwise false.
		 */
		private boolean isConnector(final JsonNode connector) {

			final JsonNode connectorNode = connector.get(MatrixConstants.YAML_CONNECTOR_KEY);
			if (connectorNode != null && !connectorNode.isNull()) {
				final JsonNode displayName = connectorNode.get(MatrixConstants.YAML_DISPLAY_NAME_KEY);
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
	public Map<String, Connector> parseConnectorsFromAllYamlFiles(Path yamlParentDirectory) throws IOException {
		final long startTime = System.currentTimeMillis();
		final ConnectorFileVisitor fileVisitor = new ConnectorFileVisitor();
		Files.walkFileTree(yamlParentDirectory, fileVisitor);
		log.info("Yaml loading duration: {} seconds", (System.currentTimeMillis() - startTime) / 1000);
		return fileVisitor.getConnectorsMap();
	}
}
