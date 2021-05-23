package com.sentrysoftware.matrix.connector.parser.state;

import com.sentrysoftware.matrix.connector.model.Connector;
import com.sentrysoftware.matrix.connector.parser.state.compute.ConnectorComputeProperty;
import com.sentrysoftware.matrix.connector.parser.state.detection.ConnectorDetectionProperty;
import com.sentrysoftware.matrix.connector.parser.state.instance.ConnectorInstanceProperty;
import com.sentrysoftware.matrix.connector.parser.state.source.ConnectorSourceProperty;
import com.sentrysoftware.matrix.connector.parser.state.value.table.ConnectorValueTableProperty;
import lombok.AllArgsConstructor;
import lombok.Getter;

import java.util.Arrays;
import java.util.Set;
import java.util.stream.Collectors;

@Getter
@AllArgsConstructor
public enum ConnectorState {

	CONNECTOR_SIMPLE_PROPERTY(new StateParsersParent(ConnectorSimpleProperty.getConnectorProperties())),
	CONNECTOR_DETECTION(new StateParsersParent(ConnectorDetectionProperty.getConnectorProperties())),
	CONNECTOR_INSTANCE_TABLE(new StateParsersParent(ConnectorInstanceProperty.getConnectorProperties())),
	CONNECTOR_SOURCE_TABLE(new StateParsersParent(ConnectorSourceProperty.getConnectorProperties())),
	CONNECTOR_VALUE_TABLE(new StateParsersParent(ConnectorValueTableProperty.getConnectorProperties())),
	CONNECTOR_COMPUTE(new StateParsersParent(ConnectorComputeProperty.getConnectorProperties()));

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
