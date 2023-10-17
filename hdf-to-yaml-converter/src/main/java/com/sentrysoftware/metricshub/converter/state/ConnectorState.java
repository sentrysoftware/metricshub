package com.sentrysoftware.metricshub.converter.state;

import com.fasterxml.jackson.databind.JsonNode;
import com.sentrysoftware.metricshub.converter.PreConnector;
import com.sentrysoftware.metricshub.converter.state.computes.ConnectorComputeProperty;
import com.sentrysoftware.metricshub.converter.state.detection.ConnectorDetectionProperty;
import com.sentrysoftware.metricshub.converter.state.instance.ConnectorInstanceProperty;
import com.sentrysoftware.metricshub.converter.state.source.ConnectorSourceProperty;
import com.sentrysoftware.metricshub.converter.state.sudo.ConnectorSudoProperty;
import com.sentrysoftware.metricshub.converter.state.valuetable.ConnectorValueTableProperty;
import java.util.Arrays;
import java.util.Collections;
import java.util.Set;
import java.util.stream.Collectors;
import lombok.AllArgsConstructor;

@AllArgsConstructor
public enum ConnectorState implements IConnectorStateConverter {
	CONNECTOR_SIMPLE_PROPERTY(new StateConverterParent(ConnectorSimpleProperty.getConnectorProperties())),
	CONNECTOR_DETECTION(new StateConverterParent(ConnectorDetectionProperty.getConnectorProperties())),
	CONNECTOR_SOURCE(new StateConverterParent(ConnectorSourceProperty.getConnectorProperties())),
	CONNECTOR_COMPUTE(new StateConverterParent(ConnectorComputeProperty.getConnectorProperties())),
	COLLECT_TYPE(new StateConverterParent(Collections.singleton(new CollectTypeProcessor()))),
	CONNECTOR_VALUE_TABLE(new StateConverterParent(ConnectorValueTableProperty.getConnectorProperties())),
	CONNECTOR_INSTANCE(new StateConverterParent(ConnectorInstanceProperty.getConnectorProperties())),
	CONNECTOR_SUDO(new StateConverterParent(ConnectorSudoProperty.getConnectorProperties()));

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
