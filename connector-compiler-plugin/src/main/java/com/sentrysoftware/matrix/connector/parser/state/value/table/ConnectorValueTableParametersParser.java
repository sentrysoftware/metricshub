package com.sentrysoftware.matrix.connector.parser.state.value.table;

import java.util.Arrays;
import java.util.Set;
import java.util.stream.Collectors;

import com.sentrysoftware.matrix.connector.model.Connector;
import com.sentrysoftware.matrix.connector.parser.state.IConnectorStateParser;

import lombok.AllArgsConstructor;
import lombok.Getter;

public class ConnectorValueTableParametersParser implements IConnectorStateParser {

	@Override
	public boolean detect(String key, String value, Connector connector) {

		return CollectValueTableParametersProperty
				.getCollectParametersProperties()
				.stream()
				.anyMatch(collectParameter -> collectParameter.detect(key, value, connector));
	}

	@Override
	public void parse(String key, String value, Connector connector) {

		CollectValueTableParametersProperty
		.getCollectParametersProperties()
		.stream()
		.filter(collectParameter -> collectParameter.detect(key, value, connector))
		.forEach(collectParameter -> collectParameter.parse(key, value, connector));
	}

	@Getter
	@AllArgsConstructor
	public enum CollectValueTableParametersProperty {

		COLLECT_TYPE(new CollectTypeProcessor()),
		VALUE_TABLE(new ValueTableProcessor()),
		COLLECT_PARAMETER(new CollectParameterProcessor());

		private IConnectorStateParser connectorStateProcessor;

		public boolean detect(final String key, final String value, final Connector connector) {

			return connectorStateProcessor.detect(key, value, connector);
		}

		public void parse(final String key, final String value, final Connector connector) {

			connectorStateProcessor.parse(key, value, connector);
		}

		public static Set<CollectValueTableParametersProperty> getCollectParametersProperties() {

			return Arrays.stream(CollectValueTableParametersProperty.values()).collect(Collectors.toSet());
		}
	}
}
