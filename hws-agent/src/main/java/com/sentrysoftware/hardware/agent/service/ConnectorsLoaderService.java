package com.sentrysoftware.hardware.agent.service;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Set;
import java.util.TreeSet;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.sentrysoftware.hardware.agent.configuration.ConfigHelper;
import com.sentrysoftware.matrix.common.helpers.StringHelper;
import com.sentrysoftware.matrix.connector.ConnectorStore;
import com.sentrysoftware.matrix.connector.model.Connector;
import com.sentrysoftware.matrix.connector.parser.ConnectorParser;

import lombok.NonNull;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@Service
public class ConnectorsLoaderService {

	@Autowired
	private ConnectorParser connectorParser;

	/**
	 * Run the loader to parse and add new connectors from the lib/connectors directory
	 */
	public void load() {

		try {
			loadConnectors(ConfigHelper.getSubDirectory("lib/connectors", false));
		} catch (Exception e) {
			log.error("Connectors loader terminated with an exception. Errors:\n{}\n",
					StringHelper.getStackMessages(e));
		}
	}

	/**
	 * Parse and add the connectors located under the given directory
	 *
	 * @param connectorDir Connector directory
	 *
	 * @throws IOException when the files cannot be read
	 */
	void loadConnectors(@NonNull final Path connectorDir) throws IOException {

		log.info("Loading additional connectors.");

		if (!Files.isDirectory(connectorDir) || !Files.exists(connectorDir)) {
			log.info("The directory {} is not found. End of connectors loader.", connectorDir);
			return;
		}

		// First, build the list of connectors
		final Set<String> parsedConnectorPaths;

		try (Stream<Path> pathStream = Files.list(connectorDir)) {
			parsedConnectorPaths = pathStream
					.map(path -> path.toAbsolutePath().toString())
					.filter(pathString -> pathString.toLowerCase().endsWith(".hdfs"))
					.collect(Collectors.toCollection(TreeSet::new));
		}

		// Get the ConnectorStore
		final ConnectorStore store = ConnectorStore.getInstance();

		// Go through each files
		parsedConnectorPaths.forEach(sourceFilePath -> {
			try {
				parseAndAddConnector(store, sourceFilePath);
			} catch (Exception e) {
				log.error("Exception detected when parsing connector {}. Errors:\n{}\n", sourceFilePath,
						StringHelper.getStackMessages(e));
			}

		});

		log.info("End of connectors loader.");
	}

	/**
	 * Parse and add the {@link Connector} in the {@link ConnectorStore}
	 * 
	 * @param store           The connector store containing all the hardware
	 *                        connectors
	 * @param sourceFilePath  The connector source file path
	 * @throws IOException can be thrown by {@link ConnectorParser}
	 */
	private void parseAndAddConnector(final ConnectorStore store, final String sourceFilePath) throws IOException {

		log.info("Parsing connector {}", sourceFilePath);

		// Parse the connector
		final Connector connector = connectorParser.parse(sourceFilePath);

		// Add the connector to the store
		store.getConnectors().put(connector.getCompiledFilename(), connector);

		log.info("Added connector {}", connector.getCompiledFilename());

		// Is there any parsing problem in this connector?
		if (!connector.getProblemList().isEmpty()) {

			log.debug("Problem(s) detected on connector {}", connector.getCompiledFilename());

			connector.getProblemList().forEach(log::debug);
		}
	}
}
