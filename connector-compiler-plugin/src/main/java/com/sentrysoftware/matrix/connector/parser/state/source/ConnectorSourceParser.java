package com.sentrysoftware.matrix.connector.parser.state.source;

import java.util.Arrays;
import java.util.Set;
import java.util.stream.Collectors;

import com.sentrysoftware.matrix.connector.model.Connector;
import com.sentrysoftware.matrix.connector.parser.state.IConnectorStateParser;

import lombok.AllArgsConstructor;
import lombok.Getter;

public class ConnectorSourceParser implements IConnectorStateParser {
	@Override
	public boolean detect(String key, String value, Connector connector) {

		return SourceSnmpProperty
				.getSourceSnmpProperties()
				.stream()
				.anyMatch(sourceSnmpProperty -> sourceSnmpProperty.detect(key, value, connector));
	}

	@Override
	public void parse(String key, String value, Connector connector) {

		SourceSnmpProperty
		.getSourceSnmpProperties()
		.stream()
		.filter(sourceSnmpProperty -> sourceSnmpProperty.detect(key, value, connector))
		.forEach(sourceSnmpProperty -> sourceSnmpProperty.parse(key, value, connector));
	}

	@Getter
	@AllArgsConstructor
	public enum SourceSnmpProperty {

		SNMP_TABLE(new SnmpTableProcessor()),
		TABLE_JOIN(new TableJoinProcessor());

		private IConnectorStateParser connectorStateProcessor;

		public boolean detect(final String key, final String value, final Connector connector) {

			return connectorStateProcessor.detect(key, value, connector);
		}

		public void parse(final String key, final String value, final Connector connector) {

			connectorStateProcessor.parse(key, value, connector);
		}

		public static Set<SourceSnmpProperty> getSourceSnmpProperties() {

			return Arrays.stream(SourceSnmpProperty.values()).collect(Collectors.toSet());
		}
	}
}
