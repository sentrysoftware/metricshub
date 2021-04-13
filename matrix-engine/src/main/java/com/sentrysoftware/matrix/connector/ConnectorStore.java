package com.sentrysoftware.matrix.connector;

import java.io.IOException;
import java.io.InputStream;
import java.io.ObjectInputStream;
import java.net.URISyntaxException;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;

import org.springframework.core.io.support.PathMatchingResourcePatternResolver;

import com.sentrysoftware.matrix.connector.model.Connector;

import lombok.Getter;
import lombok.extern.slf4j.Slf4j;

@Slf4j
public class ConnectorStore {

	private static final String CONNECTORS_RELATIVE_PATH = "/matrix/connector";

	private static ConnectorStore store = new ConnectorStore();

	@Getter
	private Map<String, Connector> connectors;

	public static ConnectorStore getInstance() {

		return store;
	}

	public ConnectorStore() {

		try {
			connectors = deserializeConnectors();
		} catch (Exception e) {
			log.error("Error while deserializing connectors. The ConnectorStore is empty!", e);
			connectors = new HashMap<>();
		}

	}

	private Map<String, Connector> deserializeConnectors() throws IOException, URISyntaxException {
		final PathMatchingResourcePatternResolver resolver = new PathMatchingResourcePatternResolver(
				this.getClass().getClassLoader());
		final Map<String, Connector> result = new HashMap<>();
		Arrays.stream(resolver.getResources(
				ConnectorStore.class.getResource(CONNECTORS_RELATIVE_PATH).toURI().toString() + "/*.connector"))
				.forEach(resource -> {
					try (InputStream inputStream = resource.getInputStream();
							ObjectInputStream objectInputStream = new ObjectInputStream(inputStream)) {

						Connector connector = (Connector) objectInputStream.readObject();
						result.put(connector.getCompiledFilename(), connector);

					} catch (ClassNotFoundException | IOException e) {
						log.error("Error while deserializing connector {}", resource.getFilename());
						log.error("Exception: ", e);
					}
				});
		return result;
	}

}
