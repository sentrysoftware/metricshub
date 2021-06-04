package com.sentrysoftware.matrix.connector.parser.state.compute.and;

import java.util.Set;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import com.sentrysoftware.matrix.connector.model.monitor.job.source.compute.And;
import com.sentrysoftware.matrix.connector.parser.state.IConnectorStateParser;
import com.sentrysoftware.matrix.connector.parser.state.compute.common.ColumnProcessor;
import com.sentrysoftware.matrix.connector.parser.state.compute.common.TypeProcessor;

public class ConnectorAndProperty {
	private ConnectorAndProperty() {
	}

	public static Set<IConnectorStateParser> getConnectorProperties() {

		return Stream
				.of(
						new TypeProcessor(And.class, AndProcessor.AND_TYPE_VALUE),
						new ColumnProcessor(And.class, AndProcessor.AND_TYPE_VALUE),
						new AndPropertyProcessor())
				.collect(Collectors.toSet());
	}
}
