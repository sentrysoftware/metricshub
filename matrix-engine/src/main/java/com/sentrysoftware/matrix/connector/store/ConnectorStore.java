package com.sentrysoftware.matrix.connector.store;

import java.util.Map;
import java.util.function.Function;
import java.util.stream.Collectors;

import com.sentrysoftware.matrix.connector.model.Connector;

import lombok.Data;

@Data
public class ConnectorStore {

	private static ConnectorStore store = new ConnectorStore();
	private Map<String, Connector> connectors;

	public static ConnectorStore getInstance() {

		return store;
	}

	public ConnectorStore() {

		connectors = ConnectorDeserialize.deserialize().stream()
				.collect(Collectors.toMap(Connector::getCompiledFilename, Function.identity(),
						(oldValue, newValue) -> newValue));
	}
}
