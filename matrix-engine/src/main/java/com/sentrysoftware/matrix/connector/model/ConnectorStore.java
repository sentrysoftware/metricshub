package com.sentrysoftware.matrix.connector.model;

import com.sentrysoftware.matrix.connector.parser.ConnectorLibraryParser;
import lombok.Getter;
import lombok.extern.slf4j.Slf4j;

import java.io.IOException;
import java.net.URISyntaxException;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.HashMap;
import java.util.Map;

@Slf4j
public class ConnectorStore {

	private static final ConnectorStore store = new ConnectorStore();

	@Getter
	private Map<String, Connector> connectors;

	public static ConnectorStore getInstance() {

		return store;
	}

	public ConnectorStore() {

		try {
			connectors = deserializeConnectors();
		} catch (Exception e) {
			log.error("Error while deserializing connectors. The ConnectorStore is empty!");
			log.debug("Error while deserializing connectors. The ConnectorStore is empty!", e);
			connectors = new HashMap<>();
		}

	}

	private Map<String, Connector> deserializeConnectors() throws IOException, URISyntaxException {
		// Connectors Yaml files exist in hdf-to-yaml-converter/src/test/resources/yaml
		final Path yamlParentDirectory = Paths.get("hdf-to-yaml-converter", "src", "test", "resources", "yaml");
		final ConnectorLibraryParser connectorLibraryParser = new ConnectorLibraryParser();
		return connectorLibraryParser.parseConnectorsFromAllYamlFiles(yamlParentDirectory);
	}
}