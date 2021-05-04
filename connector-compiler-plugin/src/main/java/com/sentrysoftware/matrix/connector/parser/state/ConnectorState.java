package com.sentrysoftware.matrix.connector.parser.state;

import java.util.Arrays;
import java.util.Set;
import java.util.stream.Collectors;

import com.sentrysoftware.matrix.connector.model.Connector;
import com.sentrysoftware.matrix.connector.parser.state.compute.keeponlymatchinglines.ConnectorKeepOnlyMatchingLinesComputeParser;
import com.sentrysoftware.matrix.connector.parser.state.compute.leftconcat.ConnectorLeftConcatComputeParser;
import com.sentrysoftware.matrix.connector.parser.state.detection.snmp.ConnectorSnmpDetectionParser;
import com.sentrysoftware.matrix.connector.parser.state.instance.ConnectorInstanceParser;
import com.sentrysoftware.matrix.connector.parser.state.source.ConnectorSourceParser;
import com.sentrysoftware.matrix.connector.parser.state.value.table.ConnectorValueTableParser;

import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public enum ConnectorState {

	CONNECTOR_SIMPLE_PROPERTY(new ConnectorSimplePropertyParser()),
	CONNECTOR_SNMP_DETECTION(new ConnectorSnmpDetectionParser()),
	CONNECTOR_INSTANCE_TABLE(new ConnectorInstanceParser()),
	CONNECTOR_SOURCE_TABLE(new ConnectorSourceParser()),
	CONNECTOR_KEEP_ONLY_MATCHING_LINES(new ConnectorKeepOnlyMatchingLinesComputeParser()),
	CONNECTOR_LEFT_CONCAT(new ConnectorLeftConcatComputeParser()),
	CONNECTOR_VALUE_TABLE(new ConnectorValueTableParser());

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
