package com.sentrysoftware.matrix.connector.parser.state;

import java.util.Arrays;
import java.util.Set;
import java.util.stream.Collectors;

import com.sentrysoftware.matrix.connector.model.Connector;

import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public enum ConnectorState {

	CONNECTOR_SIMPLE_PROPERTY(new ConnectorSimplePropertyParser());

	private ConnectorStateParser connectorStateProcessor;

	public boolean detect(final String key) {
		return connectorStateProcessor.detect(key);
	}

	public void parse(final String key, final String value, final Connector hdf) {
		connectorStateProcessor.parse(key, value, hdf);
	}

	public static Set<ConnectorState> getConnectorStates() {
		return Arrays.stream(ConnectorState.values()).collect(Collectors.toSet());
	}
}
