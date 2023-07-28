package com.sentrysoftware.matrix.connector.model;

import com.sentrysoftware.matrix.connector.parser.ConnectorLibraryParser;
import lombok.Getter;
import lombok.extern.slf4j.Slf4j;

import java.io.IOException;
import java.nio.file.Path;
import java.util.HashMap;
import java.util.Map;

@Slf4j
public class ConnectorStore {

	@Getter
	private Map<String, Connector> connectors;

	public ConnectorStore(Path connectorDirectory) {
		try {
			connectors = deserializeConnectors(connectorDirectory);
		} catch (Exception e) {
			log.error("Error while deserializing connectors. The ConnectorStore is empty!");
			log.debug("Error while deserializing connectors. The ConnectorStore is empty!", e);
			connectors = new HashMap<>();
		}

	}

	private Map<String, Connector> deserializeConnectors(Path connectorDirectory) throws IOException {
		final ConnectorLibraryParser connectorLibraryParser = new ConnectorLibraryParser();
		return connectorLibraryParser.parseConnectorsFromAllYamlFiles(connectorDirectory);
	}
}