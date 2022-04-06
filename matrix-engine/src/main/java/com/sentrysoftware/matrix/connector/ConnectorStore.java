package com.sentrysoftware.matrix.connector;

import java.io.IOException;
import java.io.InputStream;
import java.io.ObjectInputStream;
import java.net.URISyntaxException;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;
import java.util.TreeMap;
import java.util.function.Function;
import java.util.stream.Collectors;

import org.springframework.core.io.support.PathMatchingResourcePatternResolver;

import com.sentrysoftware.matrix.common.exception.DeserializationException;
import com.sentrysoftware.matrix.connector.model.Connector;
import com.sentrysoftware.matrix.engine.strategy.StrategyConfig;

import lombok.Getter;
import lombok.extern.slf4j.Slf4j;

@Slf4j
public class ConnectorStore {

	private static final String CONNECTORS_RELATIVE_PATH = "/matrix/connector";

	private static ConnectorStore store = new ConnectorStore();

	@Getter
	private Map<String, Connector> connectors;
	
	private StrategyConfig strategyConfig;

	public static ConnectorStore getInstance() {

		return store;
	}

	public ConnectorStore() {

		try {
			connectors = deserializeConnectors();
		} catch (Exception e) {
			log.error("Hostname {} - Error while deserializing connectors. The ConnectorStore is empty!", 
					strategyConfig.getEngineConfiguration().getTarget().getHostname(), e);
			connectors = new HashMap<>();
		}

	}

	private Map<String, Connector> deserializeConnectors() throws IOException, URISyntaxException {
		final PathMatchingResourcePatternResolver resolver = new PathMatchingResourcePatternResolver(
				this.getClass().getClassLoader());

		return Arrays
				.stream(resolver.getResources(
						ConnectorStore.class.getResource(CONNECTORS_RELATIVE_PATH).toURI().toString() + "/*"))
				.map(resource -> {
					try (InputStream inputStream = resource.getInputStream();
							ObjectInputStream objectInputStream = new ObjectInputStream(inputStream)) {

						return (Connector) objectInputStream.readObject();

					} catch (ClassNotFoundException | IOException e) {
						final String hostname = strategyConfig.getEngineConfiguration().getTarget().getHostname();
						String message = String.format("Hostname %s - Error while deserializing connector %s",
								hostname, resource.getFilename());
						log.error(message);
						log.error("Hostname {} - Exception: ", hostname, e);
						throw new DeserializationException(message, e);
					}
				})
				.collect(Collectors.toMap(Connector::getCompiledFilename,
						Function.identity(),
						(first, second) -> first, 
						() -> new TreeMap<String, Connector>(String.CASE_INSENSITIVE_ORDER)));

	}
}
