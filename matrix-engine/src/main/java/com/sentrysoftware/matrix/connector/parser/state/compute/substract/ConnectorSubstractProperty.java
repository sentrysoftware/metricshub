package com.sentrysoftware.matrix.connector.parser.state.compute.substract;

import com.sentrysoftware.matrix.connector.model.monitor.job.source.compute.Substract;
import com.sentrysoftware.matrix.connector.parser.state.IConnectorStateParser;
import com.sentrysoftware.matrix.connector.parser.state.compute.common.ColumnProcessor;
import com.sentrysoftware.matrix.connector.parser.state.compute.common.TypeProcessor;

import java.util.Set;
import java.util.stream.Collectors;
import java.util.stream.Stream;

public class ConnectorSubstractProperty {

	private ConnectorSubstractProperty() {
	}

	public static Set<IConnectorStateParser> getConnectorProperties() {

		return Stream
			.of(
				new TypeProcessor(Substract.class, SubstractProcessor.SUBSTRACT_TYPE_VALUE),
				new ColumnProcessor(Substract.class, SubstractProcessor.SUBSTRACT_TYPE_VALUE),
				new SubstractPropertyProcessor())
			.collect(Collectors.toSet());
	}
}
