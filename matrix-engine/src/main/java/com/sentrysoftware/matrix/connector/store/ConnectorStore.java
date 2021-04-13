package com.sentrysoftware.matrix.connector.store;

import com.sentrysoftware.matrix.connector.model.Connector;
import com.sentrysoftware.matrix.engine.Engine;
import lombok.Builder;
import lombok.Data;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.ObjectInputStream;
import java.net.URI;
import java.net.URISyntaxException;
import java.nio.file.FileSystem;
import java.nio.file.FileSystems;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;
import java.util.stream.Stream;

import static com.sentrysoftware.matrix.utils.Assert.notNull;

@Data
public class ConnectorStore {

	private static ConnectorStore store = new ConnectorStore();

	private Map<String, Connector> connectors;

	@Builder.Default
	private String connectorsRelativePath = File.separator + "matrix" + File.separator + "connector";

	public static ConnectorStore getInstance() {

		return store;
	}

	public ConnectorStore() {

		connectors = new HashMap<>();
	}

	public void deserializeConnectors() throws IOException, URISyntaxException {

		notNull(connectorsRelativePath, "connectorsRelativePath cannot be null.");

		URI uri = Engine.class
				.getResource(connectorsRelativePath)
				.toURI();

		Path connectorsPath;
		try (FileSystem fileSystem = FileSystems.newFileSystem(uri, Collections.emptyMap())) {

			// Going through the connectors directory
			connectorsPath = fileSystem.getPath(connectorsRelativePath);
			try (Stream<Path> stream = Files.walk(connectorsPath, 1)) {

				// Deserializing each connector before storing it in the connectors map
				stream
						.filter(path -> !connectorsRelativePath.equals(path.toString()))
						.forEach(
								path -> {

									try (
											InputStream inputStream = Engine.class.getResourceAsStream(path.toString());
											ObjectInputStream objectInputStream = new ObjectInputStream(inputStream)
									) {

										Connector connector = (Connector) objectInputStream.readObject();
										connectors.put(connector.getCompiledFilename(), connector);

									} catch (ClassNotFoundException | IOException e) {

										e.printStackTrace();
									}
								}
						);
			}
		}
	}
}
