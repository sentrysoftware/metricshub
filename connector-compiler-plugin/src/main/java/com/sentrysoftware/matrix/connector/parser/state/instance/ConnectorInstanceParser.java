package com.sentrysoftware.matrix.connector.parser.state.instance;

import java.util.regex.Pattern;

import com.sentrysoftware.matrix.connector.model.Connector;
import com.sentrysoftware.matrix.connector.parser.state.IConnectorStateParser;

public class ConnectorInstanceParser implements IConnectorStateParser {

	/**
	 * Pattern to detect discovery instance (instanceTable and instance parameters)
	 */
	private static final Pattern INSTANCE_PATTERN = Pattern
			.compile("^[a-z]+\\.discovery\\.instance(table|\\.[a-z0-9]+)$", Pattern.CASE_INSENSITIVE);

	@Override
	public boolean detect(final String key, final String value, final Connector connector) {
		return null != key && null != value && INSTANCE_PATTERN.matcher(key).matches();
	}

	@Override
	public void parse(final String key, final String value, final Connector connector) {
		InstanceProperty
			.getConnectorProperties()
			.stream()
			.filter(instanceProperty -> instanceProperty.detect(key, value, connector))
			.forEach(instanceProperty -> instanceProperty.parse(key, value, connector));
	}

}
