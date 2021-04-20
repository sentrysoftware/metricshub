package com.sentrysoftware.matrix.connector.parser.state;

import java.util.Arrays;
import java.util.Set;
import java.util.stream.Collectors;

import com.sentrysoftware.matrix.connector.model.Connector;

import com.sentrysoftware.matrix.connector.parser.state.detection.snmp.ConnectorSnmpDetectionParser;
import com.sentrysoftware.matrix.connector.parser.state.instance.ConnectorInstanceParser;

import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public enum ConnectorState {

	CONNECTOR_SIMPLE_PROPERTY(new ConnectorSimplePropertyParser()),
	CONNECTOR_SNMP_DETECTION(new ConnectorSnmpDetectionParser()),
	CONNECTOR_INSTANCE_TABLE(new ConnectorInstanceParser());

	private final IConnectorStateParser connectorStateProcessor;

	public boolean detect(final String key, final String value, final Connector connector) {
		return connectorStateProcessor.detect(key, value, connector);
	}

	public void parse(final String key, final String value, final Connector connector) {
		connectorStateProcessor.parse(key, value, connector);
	}

	public static Set<ConnectorState> getConnectorStates() {
		return Arrays.stream(ConnectorState.values()).collect(Collectors.toSet());
	}
}
