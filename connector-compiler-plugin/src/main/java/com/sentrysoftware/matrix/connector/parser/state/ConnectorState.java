package com.sentrysoftware.matrix.connector.parser.state;

import java.util.Arrays;
import java.util.Set;
import java.util.stream.Collectors;

import com.sentrysoftware.matrix.connector.parser.state.compute.add.ConnectorAddProperty;
import com.sentrysoftware.matrix.connector.parser.state.compute.divide.ConnectorDivideProperty;

import com.sentrysoftware.matrix.connector.model.Connector;
import com.sentrysoftware.matrix.connector.parser.state.compute.duplicatecolumn.ConnectorDuplicateColumnProperty;
import com.sentrysoftware.matrix.connector.parser.state.compute.keeponlymatchinglines.ConnectorKeepOnlyMatchingLinesProperty;
import com.sentrysoftware.matrix.connector.parser.state.compute.leftconcat.ConnectorLeftConcatProperty;
import com.sentrysoftware.matrix.connector.parser.state.compute.multiply.ConnectorMultiplyProperty;
import com.sentrysoftware.matrix.connector.parser.state.compute.perbittranslation.ConnectorPerBitTranslationProperty;
import com.sentrysoftware.matrix.connector.parser.state.compute.replace.ConnectorReplaceProperty;
import com.sentrysoftware.matrix.connector.parser.state.compute.translate.ConnectorTranslateProperty;
import com.sentrysoftware.matrix.connector.parser.state.detection.snmp.ConnectorSnmpProperty;
import com.sentrysoftware.matrix.connector.parser.state.instance.ConnectorInstanceProperty;
import com.sentrysoftware.matrix.connector.parser.state.source.ConnectorSourceProperty;
import com.sentrysoftware.matrix.connector.parser.state.value.table.ConnectorValueTableProperty;

import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public enum ConnectorState {

	CONNECTOR_SIMPLE_PROPERTY(new StateParsersParent(ConnectorSimpleProperty.getConnectorProperties())),
	CONNECTOR_SNMP_DETECTION(new StateParsersParent(ConnectorSnmpProperty.getConnectorProperties())),
	CONNECTOR_INSTANCE_TABLE(new StateParsersParent(ConnectorInstanceProperty.getConnectorProperties())),
	CONNECTOR_SOURCE_TABLE(new StateParsersParent(ConnectorSourceProperty.getConnectorProperties())),
	CONNECTOR_KEEP_ONLY_MATCHING_LINES(new StateParsersParent(ConnectorKeepOnlyMatchingLinesProperty.getConnectorProperties())),
	CONNECTOR_LEFT_CONCAT(new StateParsersParent(ConnectorLeftConcatProperty.getConnectorProperties())),
	CONNECTOR_VALUE_TABLE(new StateParsersParent(ConnectorValueTableProperty.getConnectorProperties())),
	CONNECTOR_DUPLICATE_COLUMN(new StateParsersParent(ConnectorDuplicateColumnProperty.getConnectorProperties())),
	CONNECTOR_TRANSLATE(new StateParsersParent(ConnectorTranslateProperty.getConnectorProperties())),
	CONNECTOR_DIVIDE(new StateParsersParent(ConnectorDivideProperty.getConnectorProperties())),
	CONNECTOR_ADD(new StateParsersParent(ConnectorAddProperty.getConnectorProperties())),
	CONNECTOR_MULTIPLY(new StateParsersParent(ConnectorMultiplyProperty.getConnectorProperties())),
	CONNECTOR_PER_BIT_TRANSLATION(new StateParsersParent(ConnectorPerBitTranslationProperty.getConnectorProperties())),
	CONNECTOR_REPLACE(new StateParsersParent(ConnectorReplaceProperty.getConnectorProperties()));

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
