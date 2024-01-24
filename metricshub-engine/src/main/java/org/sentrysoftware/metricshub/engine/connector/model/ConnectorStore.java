package org.sentrysoftware.metricshub.engine.connector.model;

import java.io.IOException;
import java.nio.file.Path;
import java.util.HashMap;
import java.util.Map;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.NonNull;
import lombok.Setter;
import lombok.extern.slf4j.Slf4j;
import org.sentrysoftware.metricshub.engine.connector.parser.ConnectorLibraryParser;

/**
 * Manages the storage and retrieval of {@link Connector} instances.
 * The instances are stored in a map where the key is the connector file name, and the value is the corresponding {@link Connector} object.
 */
@Slf4j
@NoArgsConstructor
public class ConnectorStore {

	@Getter
	@Setter
	private Map<String, Connector> store;

	@Getter
	private Path connectorDirectory;

	/**
	 * Constructs a {@link ConnectorStore} using the specified connector directory.
	 *
	 * @param connectorDirectory The path to the directory containing connector files.
	 */
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

	/**
	 * Add a new {@link Connector} instance
	 *
	 * @param id        the id of the connector
	 * @param connector the {@link Connector} instance to add
	 */
	public void addOne(@NonNull final String id, @NonNull final Connector connector) {
		store.put(id, connector);
	}
}
