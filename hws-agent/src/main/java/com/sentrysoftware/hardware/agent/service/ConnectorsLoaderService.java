package com.sentrysoftware.hardware.agent.service;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.ObjectInputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Set;
import java.util.TreeSet;
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

		// First, determine the source and serialized connectors
		final Set<String> connectorSources = new TreeSet<>();
		final Set<String> serializedConnectors = new TreeSet<>();
		try (Stream<Path> pathStream = Files.list(connectorDir)) {

			// Partition the stream
			pathStream
					.map(path -> path.toAbsolutePath().toString())
					.forEach(path -> {
						var pathLowerCase = path.toLowerCase();
						if (pathLowerCase.endsWith(".hdfs")) {
							connectorSources.add(path);
						}
						if (isConnector(pathLowerCase)) {
							serializedConnectors.add(path);
						}
					});
		}

		// Get the ConnectorStore
		final ConnectorStore store = ConnectorStore.getInstance();

		// Go through each connector source
		connectorSources.stream()
			.forEach(sourceFilePath -> {

				try {
					parseAndAddConnector(store, sourceFilePath);
				} catch (Exception e) {
					log.error("Exception detected when parsing connector {}. Errors:\n{}\n", sourceFilePath,
							StringHelper.getStackMessages(e));
				}

		});

		// Go through each serialized connector
		serializedConnectors.stream()
			.forEach(sourceFilePath -> {

				try {
					deserializeAndAddConnector(store, new File(sourceFilePath));
				} catch (Exception e) {
					log.error("Exception detected when deserializing connector {}. Errors:\n{}\n", sourceFilePath,
							StringHelper.getStackMessages(e));
				}

		});

		log.info("End of connectors loader.");
	}

	/**
	 * Parse and add the {@link Connector} in the {@link ConnectorStore}
	 * 
	 * @param store           The connector store containing all the hardware connectors
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

	/**
	 * Deserialize and add the {@link Connector} in the {@link ConnectorStore}
	 *
	 * @param The                 connector store containing all the hardware connectors
	 * @param connectorSerialized The serialized connector
	 *
	 * @throws IOException
	 * @throws ClassNotFoundException
	 */
	void deserializeAndAddConnector(final ConnectorStore store, final File connectorSerialized) throws IOException, ClassNotFoundException {

		log.info("Deserializing connector {}", connectorSerialized);

		try (InputStream inputStream = new FileInputStream(connectorSerialized);
				ObjectInputStream objectInputStream = new ObjectInputStream(inputStream)) {

			final Connector connector = (Connector) objectInputStream.readObject();

			store.getConnectors().put(connector.getCompiledFilename(), connector);

			log.info("Added connector {}", connector.getCompiledFilename());

		}

	}

	/**
	 * Check if the given connector file path is a real serialized connector without
	 * any extension.
	 * 
	 * <pre>
	 * E.g. /opt/hws/lib/connectors/SuperConnector
	 * </pre>
	 * 
	 * @param connectorFilePath The path of the connector file
	 * @return <code>true</code> if the file doens't have an extension means a
	 *         serialized connector otherwise <code>false</code>
	 */
	boolean isConnector(String connectorFilePath) {

		final int extensionPosition = connectorFilePath.lastIndexOf(".");

		// We are sure to handle both LINUX and WINDOWS paths with the maximum last index
		final int lastSeparator = Math.max(connectorFilePath.lastIndexOf("/"), connectorFilePath.lastIndexOf("\\"));

		// The last separator is after the last dot
		return lastSeparator > extensionPosition;
	}
}
