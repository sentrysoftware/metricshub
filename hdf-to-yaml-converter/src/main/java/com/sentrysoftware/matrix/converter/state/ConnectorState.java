package com.sentrysoftware.matrix.converter.state;

import java.util.Arrays;
import java.util.Set;
import java.util.stream.Collectors;

import com.fasterxml.jackson.databind.JsonNode;
import com.sentrysoftware.matrix.converter.PreConnector;
import com.sentrysoftware.matrix.converter.state.detection.ConnectorDetectionProperty;
import com.sentrysoftware.matrix.converter.state.source.ConnectorSourceProperty;

import lombok.AllArgsConstructor;

@AllArgsConstructor
public enum ConnectorState implements IConnectorStateConverter {

	CONNECTOR_SIMPLE_PROPERTY(new StateConverterParent(ConnectorSimpleProperty.getConnectorProperties())),
	CONNECTOR_DETECTION(new StateConverterParent(ConnectorDetectionProperty.getConnectorProperties())),
	CONNECTOR_SOURCE(new StateConverterParent(ConnectorSourceProperty.getConnectorProperties()));


	private final IConnectorStateConverter connectorStateConverter;

	@Override
	public boolean detect(final String key, final String value, final JsonNode connector) {
		return connectorStateConverter.detect(key, value, connector);
	}

	@Override
	public void convert(final String key, final String value, final JsonNode connector, final PreConnector preConnector) {
		connectorStateConverter.convert(key, value, connector, preConnector);
	}

	/**
	 * Get state values
	 *
	 * @return Set of {@link ConnectorState}
	 */
	public static Set<ConnectorState> getConnectorStates() {
		return Arrays.stream(ConnectorState.values()).collect(Collectors.toSet());
	}
}
