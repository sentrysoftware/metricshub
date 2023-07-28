package com.sentrysoftware.matrix.connector.model;

import com.sentrysoftware.matrix.connector.parser.ConnectorLibraryParser;
import lombok.Getter;
import lombok.Setter;
import lombok.extern.slf4j.Slf4j;

import java.io.IOException;
import java.nio.file.Path;
import java.util.HashMap;
import java.util.Map;

@Slf4j
public class ConnectorStore {

	@Getter
	private Map<String, Connector> store;

	@Getter
	private Path connectorDirectory;

	public ConnectorStore(Path connectorDirectory) {
		try {
			this.connectorDirectory = connectorDirectory;
			store = deserializeConnectors();
		} catch (Exception e) {
			log.error("Error while deserializing connectors. The ConnectorStore is empty!");
			log.debug("Error while deserializing connectors. The ConnectorStore is empty!", e);
			store = new HashMap<>();
		}

	}

	/**
	 * This method retrieves Connectors data in a Map from a given connector directory
	 * The key of the Map will be the connector file name and Value will be the Connector Object
	 * @return Map<String, Connector>
	 * @throws IOException
	 */
	private Map<String, Connector> deserializeConnectors() throws IOException {
		final ConnectorLibraryParser connectorLibraryParser = new ConnectorLibraryParser();
		return connectorLibraryParser.parseConnectorsFromAllYamlFiles(connectorDirectory);
	}
}